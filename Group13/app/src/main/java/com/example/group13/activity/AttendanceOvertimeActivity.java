package com.example.group13.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AttendanceOvertimeActivity extends BaseActivity {

    private ImageButton btnBack;
    private TextView txtDateRange, txtTimeRange;
    private EditText etReasonOvertime; // thêm ô lý do
    private Button btnSubmitOvertime;

    private Calendar selectedDate;
    private int startHour, startMinute, endHour, endMinute;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_overtime);

        setupNavigation();
        initViews();
        initFirebase();
        setListeners();

        selectedDate = Calendar.getInstance();
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        txtDateRange = findViewById(R.id.txtDateRange);
        txtTimeRange = findViewById(R.id.txtTimeRange);
        etReasonOvertime = findViewById(R.id.etReasonOvertime); // bind EditText
        btnSubmitOvertime = findViewById(R.id.btnSubmitOvertime);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
        if (currentUser == null) finish();
    }

    private void setListeners() {
        setupBackButton();
        txtDateRange.setOnClickListener(v -> showDatePicker());
        txtTimeRange.setOnClickListener(v -> showTimePicker());
        btnSubmitOvertime.setOnClickListener(v -> submitOvertime());
    }

    // ===== BACK BUTTON =====
    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AttendanceOvertimeActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    // ===== DATE PICKER =====
    private void showDatePicker() {
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener listener = (view, y, m, d) -> {
            selectedDate.set(y, m, d);
            txtDateRange.setText(new SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                    .format(selectedDate.getTime()));
        };

        new DatePickerDialog(this, listener, year, month, day).show();
    }

    // ===== TIME PICKER =====
    private void showTimePicker() {
        TimePickerDialog.OnTimeSetListener startListener = (view, hourOfDay, minute) -> {
            startHour = hourOfDay;
            startMinute = minute;

            TimePickerDialog.OnTimeSetListener endListener = (view1, hourEnd, minuteEnd) -> {
                endHour = hourEnd;
                endMinute = minuteEnd;
                txtTimeRange.setText(String.format(Locale.getDefault(),
                        "%02d:%02d - %02d:%02d", startHour, startMinute, endHour, endMinute));
            };

            new TimePickerDialog(this, endListener, startHour, startMinute, true).show();
        };

        new TimePickerDialog(this, startListener, 9, 0, true).show();
    }

    // ===== SUBMIT =====
    private void submitOvertime() {
        String dateDisplay = txtDateRange.getText().toString();
        String timeRange = txtTimeRange.getText().toString();
        String reason = etReasonOvertime.getText().toString().trim(); // lấy lý do

        if (dateDisplay.isEmpty() || timeRange.isEmpty() || reason.isEmpty()) {
            showCustomToast("Please select date, time and enter reason", false);
            return;
        }

        if (currentUser == null) return;

        String uid = currentUser.getUid();
        String submitDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        SimpleDateFormat sdfFirestore = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date dateObj;
        try {
            dateObj = new SimpleDateFormat("dd/MM/yy", Locale.getDefault()).parse(dateDisplay);
        } catch (Exception e) {
            e.printStackTrace();
            showCustomToast("Invalid date format", false);
            return;
        }
        String dateFirestore = sdfFirestore.format(dateObj);

        CollectionReference ref = db.collection("overtime").document(uid).collection("requests");
        OvertimeRequest request = new OvertimeRequest(uid, dateFirestore, timeRange, reason, submitDate);

        ref.add(request)
                .addOnSuccessListener(aVoid -> {
                    showCustomToast("Overtime submitted!", true);
                    // Reset UI
                    txtDateRange.setText("Select date");
                    txtTimeRange.setText("Select time");
                    etReasonOvertime.setText(""); // reset lý do
                    selectedDate = Calendar.getInstance();
                    startHour = startMinute = endHour = endMinute = 0;
                })
                .addOnFailureListener(e -> showCustomToast("Failed to submit", false));
    }

    // ===== MODEL =====
    public static class OvertimeRequest {
        public String uid;
        public String date;
        public String time;
        public String reason;
        public String submitDate;

        public OvertimeRequest() { }

        public OvertimeRequest(String uid, String date, String time, String reason, String submitDate) {
            this.uid = uid;
            this.date = date;
            this.time = time;
            this.reason = reason;
            this.submitDate = submitDate;
        }
    }

    // ================= CUSTOM TOAST =================
    private void showCustomToast(String message, boolean isSuccess) {
        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_SHORT);

        TextView text = new TextView(this);
        text.setText(message);
        text.setTextColor(Color.WHITE);
        text.setTextSize(16f);
        text.setGravity(Gravity.CENTER);
        text.setPadding(30, 20, 30, 20);

        int bgRes = isSuccess ? R.drawable.toast_success_bg : R.drawable.toast_error_bg;
        text.setBackgroundResource(bgRes);

        toast.setView(text);
        toast.show();
    }
}