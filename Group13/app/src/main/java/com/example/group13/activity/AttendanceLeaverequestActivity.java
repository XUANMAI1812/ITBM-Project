package com.example.group13.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;

import androidx.annotation.Nullable;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceLeaverequestActivity extends BaseActivity {

    // Views
    private ImageButton btnBack;
    private EditText etReasonLeave;
    private Button btnSubmit;
    private LinearLayout layoutSelectDateRange;
    private TextView tvSelectedDateRange;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String uid;

    // Dates
    private String startDate = "";
    private String endDate = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_leave_request);

        setupNavigation();
        initViews();
        initFirebase();
        setListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        etReasonLeave = findViewById(R.id.etReasonLeave);
        btnSubmit = findViewById(R.id.btnSubmitLeave);
        layoutSelectDateRange = findViewById(R.id.layoutSelectDateRange);
        tvSelectedDateRange = findViewById(R.id.tvSelectedDateRange);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            uid = currentUser.getUid();
        } else {
            finish();
        }
    }

    private void setListeners() {
        setupBackButton();
        layoutSelectDateRange.setOnClickListener(v -> selectStartDate());
        btnSubmit.setOnClickListener(v -> submitLeaveRequest());
    }

    // ===== BACK BUTTON =====
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AttendanceLeaverequestActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    // ===== DATE PICKER =====
    private void selectStartDate() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            startDate = formatDate(year, month, dayOfMonth);
            selectEndDate();
        };
        new DatePickerDialog(this, listener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void selectEndDate() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            endDate = formatDate(year, month, dayOfMonth);
            tvSelectedDateRange.setText(formatDisplayDate(startDate) + " → " + formatDisplayDate(endDate));
        };
        new DatePickerDialog(this, listener,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private String formatDate(int y, int m, int d) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
    }

    private String formatDisplayDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date d = sdf.parse(date);
            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
            return out.format(d);
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    // ===== SUBMIT =====
    private void submitLeaveRequest() {
        String reason = etReasonLeave.getText().toString().trim();
        if (reason.isEmpty()) {
            showCustomToast("Please enter reason", false);
            return;
        }
        if (startDate.isEmpty() || endDate.isEmpty()) {
            showCustomToast("Please select date range", false);
            return;
        }

        String submitDate = formatDate(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("reason", reason);
        data.put("startDate", startDate);
        data.put("endDate", endDate);
        data.put("dateRange", tvSelectedDateRange.getText().toString());
        data.put("submitDate", submitDate);
        data.put("timestamp", System.currentTimeMillis());
        data.put("status", "Pending");

        db.collection("leaverequest")
                .add(data)
                .addOnSuccessListener(doc -> {
                    showCustomToast("Leave request submitted", true);
                    etReasonLeave.setText("");
                    tvSelectedDateRange.setText("Select date range");
                    startDate = endDate = "";
                })
                .addOnFailureListener(e -> showCustomToast("Submit failed", false));
    }

    private void showCustomToast(String message, boolean isSuccess) {
        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);

        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(0xFFFFFFFF);
        text.setTextSize(16f);
        text.setGravity(Gravity.CENTER);
        text.setPadding(30, 20, 30, 20);

        int bgRes = isSuccess ? R.drawable.toast_success_bg : R.drawable.toast_error_bg;
        text.setBackgroundResource(bgRes);

        toast.setView(text);
        toast.show();
    }
}