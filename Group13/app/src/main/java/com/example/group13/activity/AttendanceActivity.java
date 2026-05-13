package com.example.group13.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import com.journeyapps.barcodescanner.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AttendanceActivity extends BaseActivity {

    // ================= UI =================
    private ImageButton btnBack;
    private TextView tvName, tvId, tvStatus, tvTime;
    private FrameLayout cameraPreview;

    // ================= FIREBASE =================
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String userName = "Unknown";
    private String employeeId = "N/A";

    // ================= QR =================
    private DecoratedBarcodeView barcodeView;
    private static final int CAMERA_PERMISSION = 1001;
    private boolean isProcessing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        setupNavigation();
        findViews();
        initFirebase();
        loadUserInfo();
        setupBackButton();
        initQrScanner();
        checkCameraPermission();
    }

    // ================= INIT =================

    private void findViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        tvName = findViewById(R.id.tv_employee_name);
        tvId = findViewById(R.id.tv_employee_id);
        tvStatus = findViewById(R.id.tv_status);
        tvTime = findViewById(R.id.tv_time);
        cameraPreview = findViewById(R.id.qr_camera_preview);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser == null) finish();
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> finish());
    }

    // ================= LOAD USER =================

    private void loadUserInfo() {
        if (currentUser == null) return;

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        userName = doc.getString("employeeName");
                        employeeId = doc.getString("employeeId");

                        if (userName == null) userName = "Unknown";
                        if (employeeId == null) employeeId = "N/A";
                    }

                    tvName.setText("Name: " + userName);
                    tvId.setText("ID: " + employeeId);
                });
    }

    // ================= QR SETUP =================

    private void initQrScanner() {
        barcodeView = new DecoratedBarcodeView(this);
        barcodeView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        barcodeView.setDecoderFactory(new DefaultDecoderFactory());
        cameraPreview.addView(barcodeView);
        barcodeView.decodeContinuous(qrCallback);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION
            );
        } else {
            barcodeView.resume();
        }
    }

    // ================= QR CALLBACK =================

    private final BarcodeCallback qrCallback = result -> {
        if (isProcessing) return;
        if (result == null || result.getText() == null) return;

        isProcessing = true;

        String qr = result.getText()
                .replace("\n", "")
                .replace("\r", "")
                .trim();

        Log.d("QR_DEBUG", "QR = [" + qr + "]");

        // Reject URL QR codes
        if (qr.startsWith("http")) {
            showToast("Invalid QR code", false);
            resetScanner();
            return;
        }

        try {
            String[] parts = qr.split("\\|");

            if (parts.length != 4 ||
                    !parts[0].equals("ATTENDANCE") ||
                    !parts[1].equals("CHECK")) {
                showToast("Invalid QR format", false);
                resetScanner();
                return;
            }

            String qrDate = parts[2];
            String today = getToday();

            if (!today.equals(qrDate)) {
                showToast("QR code is not valid today", false);
                resetScanner();
                return;
            }

            barcodeView.pause();
            processAttendance(parts[3]);

        } catch (Exception e) {
            showToast("QR processing error", false);
            resetScanner();
        }
    };

    private void resetScanner() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isProcessing = false;
            barcodeView.resume();
        }, 1500);
    }

    // ================= ATTENDANCE =================

    private void processAttendance(String locationCode) {
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        String date = getToday();

        DocumentReference ref = db.collection("attendance")
                .document(uid)
                .collection("days")
                .document(date);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snap = transaction.get(ref);

                    Map<String, Object> data = new HashMap<>();
                    data.put("uid", uid);
                    data.put("employeeId", employeeId);
                    data.put("name", userName);
                    data.put("location", locationCode);
                    data.put("timestamp", FieldValue.serverTimestamp());

                    if (!snap.exists() || !snap.contains("checkIn")) {
                        data.put("checkIn", FieldValue.serverTimestamp());
                        data.put("status", "Checked in");
                        transaction.set(ref, data, SetOptions.merge());

                    } else if (!snap.contains("checkOut")) {
                        data.put("checkOut", FieldValue.serverTimestamp());
                        data.put("status", "Checked out");
                        transaction.update(ref, data);

                    } else {
                        throw new FirebaseFirestoreException(
                                "Already checked out",
                                FirebaseFirestoreException.Code.ABORTED
                        );
                    }

                    return null;
                }).addOnSuccessListener(v -> updateUI(ref))
                .addOnFailureListener(e -> {
                    showToast(e.getMessage(), false);
                    resetScanner();
                });
    }

    private void updateUI(DocumentReference ref) {
        ref.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvStatus.setText("Status: " + doc.getString("status"));

                Timestamp t = doc.getTimestamp(
                        doc.contains("checkOut") ? "checkOut" : "checkIn"
                );

                if (t != null) {
                    tvTime.setText("Time: " +
                            new SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm",
                                    Locale.getDefault()
                            ).format(t.toDate()));
                }
            }

            showToast("Attendance recorded successfully", true);
            resetScanner();
        });
    }

    // ================= UTIL =================

    private String getToday() {
        return new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());
    }

    private void showToast(String msg, boolean success) {
        Toast toast = new Toast(this);
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        tv.setPadding(30, 20, 30, 20);
        tv.setGravity(Gravity.CENTER);
        tv.setBackgroundResource(
                success ? R.drawable.toast_success_bg
                        : R.drawable.toast_error_bg
        );
        toast.setView(tv);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    // ================= LIFECYCLE =================

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Camera permission is required to scan QR codes.")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}