package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.example.group13.R;
import com.example.group13.adapter.HistoryAdapter;
import com.example.group13.model.HistoryItem;
import com.example.group13.base.BaseActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

import com.google.firebase.Timestamp;

public class AttendanceHistoryActivity extends BaseActivity {

    private ImageButton btnBack;
    private ListView lvHistory;
    private EditText etSearch;
    private ArrayList<HistoryItem> historyList;
    private HistoryAdapter adapter;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private ListenerRegistration attendanceListener;
    private ListenerRegistration leaveListener;
    private ListenerRegistration overtimeListener;

    private final SimpleDateFormat sdfAttendance = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfDay = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_history);

        setupNavigation();
        findViews();
        initFirebase();
        setupAdapter();
        loadHistoryRealtime();
        setupSearch();
        setupBackButton();
    }

    private void findViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        lvHistory = findViewById(R.id.lvHistory);
        etSearch = findViewById(R.id.etSearch);
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();
    }

    private void setupAdapter() {
        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(this, historyList);
        lvHistory.setAdapter(adapter);
    }

    private void loadHistoryRealtime() {
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        // Attendance
        attendanceListener = db.collection("attendance")
                .document(uid)
                .collection("days")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        updateAttendance(value);
                        sortAndUpdate();
                    }
                });

        // Leave Requests
        leaveListener = db.collection("leaverequest")
                .whereEqualTo("uid", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        updateLeaveRequest(value);
                        sortAndUpdate();
                    }
                });

        // Overtime
        overtimeListener = db.collection("overtime")
                .document(uid)
                .collection("requests")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        updateOvertime(value);
                        sortAndUpdate();
                    }
                });
    }

    // ===== UPDATE ATTENDANCE =====
    private void updateAttendance(QuerySnapshot snapshot) {
        historyList.removeIf(item -> item.getType().equals("Check In") || item.getType().equals("Check Out"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            // CheckIn
            if (doc.contains("checkIn")) {
                Object checkInObj = doc.get("checkIn");
                Date checkInDate = parseDateObject(checkInObj);
                if (checkInDate != null) {
                    historyList.add(new HistoryItem("Check In", sdfAttendance.format(checkInDate)));
                }
            }

            // CheckOut
            if (doc.contains("checkOut")) {
                Object checkOutObj = doc.get("checkOut");
                Date checkOutDate = parseDateObject(checkOutObj);
                if (checkOutDate != null) {
                    historyList.add(new HistoryItem("Check Out", sdfAttendance.format(checkOutDate)));
                }
            }
        }
    }

    // ===== UPDATE LEAVE REQUEST =====
    private void updateLeaveRequest(QuerySnapshot snapshot) {
        historyList.removeIf(item -> item.getType().equals("Leave Request"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Object leaveObj = doc.get("submitDate"); // hoặc ngày nghỉ nếu bạn lưu field khác
            String reason = doc.getString("reason");
            Date leaveDate = parseDateObject(leaveObj);

            if (leaveDate != null) {
                historyList.add(new HistoryItem(
                        "Leave Request",
                        sdfDay.format(leaveDate) + " - " + (reason != null ? reason : "Leave Request")
                ));
            }
        }
    }

    // ===== UPDATE OVERTIME =====
    private void updateOvertime(QuerySnapshot snapshot) {
        historyList.removeIf(item -> item.getType().equals("Overtime"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String timeRange = doc.getString("time"); // format: HH:mm - HH:mm
            String dateStr = doc.getString("date"); // yyyy-MM-dd
            String reason = doc.getString("reason");

            if (dateStr != null) {
                String display = "";
                try {
                    Date dateObj = sdfDate.parse(dateStr);
                    String dateDisplay = sdfDay.format(dateObj);
                    display = (timeRange != null ? timeRange : "") + " " + dateDisplay;
                    if (reason != null && !reason.isEmpty()) {
                        display += " - " + reason;
                    }
                    historyList.add(new HistoryItem("Overtime", display));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ===== PARSE DATE/TIMESTAMP =====
    private Date parseDateObject(Object obj) {
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toDate();
        } else if (obj instanceof String) {
            String str = (String) obj;
            try {
                if (str.length() == 10) { // yyyy-MM-dd
                    return sdfDate.parse(str);
                } else if (str.length() == 16) { // yyyy-MM-dd HH:mm
                    return sdfFull.parse(str);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void sortAndUpdate() {
        Collections.sort(historyList, (o1, o2) -> {
            try {
                Date d1 = parseSortDate(o1);
                Date d2 = parseSortDate(o2);
                return d2.compareTo(d1);
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        });
        adapter.updateData(historyList);
    }

    private Date parseSortDate(HistoryItem item) throws Exception {
        String type = item.getType();
        String val = item.getDate();
        if (type.equals("Check In") || type.equals("Check Out")) {
            return sdfFull.parse(sdfFull.format(parseDateObject(val)));
        } else if (type.equals("Leave Request")) {
            return sdfDate.parse(val.split(" - ")[0]);
        } else if (type.equals("Overtime")) {
            return sdfDate.parse(val.split(" ")[1]); // dd-MM-yyyy
        }
        return new Date();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                if (query.isEmpty()) adapter.updateData(historyList);
                else {
                    ArrayList<HistoryItem> filtered = new ArrayList<>();
                    for (HistoryItem item : historyList) {
                        if (item.getType().toLowerCase().contains(query) ||
                                item.getDate().toLowerCase().contains(query)) {
                            filtered.add(item);
                        }
                    }
                    adapter.updateData(filtered);
                }
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(AttendanceHistoryActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attendanceListener != null) attendanceListener.remove();
        if (leaveListener != null) leaveListener.remove();
        if (overtimeListener != null) overtimeListener.remove();
    }
}
