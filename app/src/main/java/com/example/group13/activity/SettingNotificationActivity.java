package com.example.group13.activity;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.NotificationAdapter;
import com.example.group13.base.BaseActivity;
import com.example.group13.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingNotificationActivity extends BaseActivity {

    private ImageButton btn_back;
    private RecyclerView recyclerNotification;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private ListenerRegistration attendanceListener;
    private ListenerRegistration leaveListener;
    private ListenerRegistration overtimeListener;
    private ListenerRegistration reminderListener;
    private ListenerRegistration projectListener;

    private ListenerRegistration taskListener;
    private ListenerRegistration employeeListener;
    private final SimpleDateFormat sdfAttendance = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfDay = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_notification);

        setupNavigation();
        findViewsByIds();
        setOnClickListeners();

        recyclerNotification.setLayoutManager(new LinearLayoutManager(this));
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerNotification.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = auth.getCurrentUser();

        loadNotificationsRealtime();
    }

    private void findViewsByIds() {
        btn_back = findViewById(R.id.imageButtonBack);
        recyclerNotification = findViewById(R.id.recyclerNotification);
    }

    private void setOnClickListeners() {
        btn_back.setOnClickListener(v -> finish());
    }

    private void loadNotificationsRealtime() {
        if (currentUser == null) return;
        String uid = currentUser.getUid();

        // Attendance
        attendanceListener = db.collection("attendance")
                .document(uid)
                .collection("days")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateAttendance(value);
                    sortAndUpdate();
                });

        // Leave Request
        leaveListener = db.collection("leaverequest")
                .whereEqualTo("uid", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateLeaveRequest(value);
                    sortAndUpdate();
                });

        // Overtime
        overtimeListener = db.collection("overtime")
                .document(uid)
                .collection("requests")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateOvertime(value);
                    sortAndUpdate();
                });

        // Reminder
        reminderListener = db.collection("reminders")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateReminder(value);
                    sortAndUpdate();
                });
        // Project
        projectListener = db.collection("users")
                .document(uid)
                .collection("projects")
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateProjectNotifications(value);
                    sortAndUpdate();
                });

        // Employee
        employeeListener = db.collection("users")
                .document(uid)
                .collection("employees") // hoặc collection notifications riêng cho employee
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateEmployeeNotifications(value);
                    sortAndUpdate();
                });

        // Tasks
        taskListener = db.collection("tasks")
                .whereEqualTo("employeeId", currentUser.getUid()) // chỉ lấy tasks của user hiện tại
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;
                    updateTaskNotifications(value);
                    sortAndUpdate();
                });
    }

    private void updateTaskNotifications(QuerySnapshot snapshot) {
        // Xóa các task cũ trong danh sách
        notificationList.removeIf(n -> n.getTitle().equals("New Task") || n.getTitle().equals("Task Updated"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String taskTitle = doc.getString("title");
            String projectId = doc.getString("projectId");
            Long deadlineMillis = doc.getLong("deadline");

            if (projectId != null) {
                db.collection("projects").document(projectId)
                        .get()
                        .addOnSuccessListener(projectDoc -> {
                            String projectName = projectDoc.getString("name");
                            String content = taskTitle != null ? taskTitle : "Task";

                            if (projectName != null) {
                                content += " | Project: " + projectName;
                            }

                            if (deadlineMillis != null) {
                                content += " | Deadline: " + sdfDay.format(new Date(deadlineMillis));
                            }

                            notificationList.add(new Notification(
                                    "New Task",
                                    content,
                                    sdfDay.format(new Date(deadlineMillis != null ? deadlineMillis : System.currentTimeMillis()))
                            ));

                            sortAndUpdate();
                        });
            } else {

                String content = taskTitle != null ? taskTitle : "Task";

                if (deadlineMillis != null) {
                    content += " | Deadline: " + sdfDay.format(new Date(deadlineMillis));
                }

                notificationList.add(new Notification(
                        "New Task",
                        content,
                        sdfDay.format(new Date(deadlineMillis != null ? deadlineMillis : System.currentTimeMillis()))
                ));

                sortAndUpdate();
            }
        }
    }

    private void updateEmployeeNotifications(QuerySnapshot snapshot) {
        notificationList.removeIf(n -> n.getTitle().equals("Employee Created") || n.getTitle().equals("Employee Updated"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String title = doc.getString("title");
            String content = doc.getString("content");
            Long timeMillis = doc.getLong("timeMillis");

            if (timeMillis != null) {
                Date date = new Date(timeMillis);
                notificationList.add(new Notification(
                        title != null ? title : "Employee Notification",
                        content != null ? content : "Employee action",
                        sdfDay.format(date) // hiển thị dd/MM/yyyy
                ));
            }
        }
    }

    private void updateProjectNotifications(QuerySnapshot snapshot) {
        notificationList.removeIf(n -> n.getTitle().equals("Project Created") || n.getTitle().equals("Project Updated"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String title = doc.getString("title");
            String content = doc.getString("content");
            Long timeMillis = doc.getLong("timeMillis");

            if (timeMillis != null) {
                Date date = new Date(timeMillis);
                // Hiển thị ngày dd/MM/yyyy
                notificationList.add(new Notification(
                        title != null ? title : "Project Notification",
                        content != null ? content : "Project action",
                        sdfDay.format(date)
                ));
            }
        }
    }
    private void updateAttendance(QuerySnapshot snapshot) {
        notificationList.removeIf(n -> n.getTitle().equals("Check In") || n.getTitle().equals("Check Out"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            if (doc.contains("checkIn")) {
                Date date = parseDateObject(doc.get("checkIn"));
                if (date != null) {
                    notificationList.add(new Notification("Check In", "You have checked in", sdfAttendance.format(date)));
                }
            }
            if (doc.contains("checkOut")) {
                Date date = parseDateObject(doc.get("checkOut"));
                if (date != null) {
                    notificationList.add(new Notification("Check Out", "You have checked out", sdfAttendance.format(date)));
                }
            }
        }
    }

    private void updateLeaveRequest(QuerySnapshot snapshot) {
        notificationList.removeIf(n -> n.getTitle().equals("Leave Request"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            Object startObj = doc.get("startDate");
            Object endObj = doc.get("endDate");
            String reason = doc.getString("reason");

            Date startDate = parseDateObject(startObj);
            Date endDate = parseDateObject(endObj);

            if (startDate != null && endDate != null) {
                String dateRange = sdfDay.format(startDate) + " → " + sdfDay.format(endDate);
                String content = (reason != null && !reason.isEmpty()) ? dateRange + " - " + reason : dateRange;
                notificationList.add(new Notification("Leave Request", content, sdfDay.format(startDate)));
            }
        }
    }
    private void updateOvertime(QuerySnapshot snapshot) {
        notificationList.removeIf(n -> n.getTitle().equals("Overtime"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String dateStr = doc.getString("date");
            String timeRange = doc.getString("time");
            String reason = doc.getString("reason");

            if (dateStr != null) {
                try {
                    Date date = sdfDate.parse(dateStr);
                    String content = (timeRange != null ? timeRange : "") + (reason != null && !reason.isEmpty() ? " → " + reason : "");
                    notificationList.add(new Notification("Overtime", content, sdfDay.format(date)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateReminder(QuerySnapshot snapshot) {
        notificationList.removeIf(n -> n.getTitle().equals("Reminder"));

        for (DocumentSnapshot doc : snapshot.getDocuments()) {
            String content = doc.getString("content");
            Long deadlineMillis = doc.getLong("deadlineMillis");

            if (deadlineMillis != null) {
                Date date = new Date(deadlineMillis);
                notificationList.add(new Notification(
                        "Reminder",
                        content != null ? content : "Reminder task",
                        sdfDay.format(date)
                ));
            }
        }
    }

    private Date parseDateObject(Object obj) {
        if (obj instanceof Timestamp) {
            return ((Timestamp) obj).toDate();
        } else if (obj instanceof String) {
            try {
                String str = (String) obj;
                if (str.length() == 10) return sdfDate.parse(str);
                else if (str.length() == 16) return sdfFull.parse(str);
            } catch (Exception e) { e.printStackTrace(); }
        }
        return null;
    }

    private void sortAndUpdate() {
        Collections.sort(notificationList, (o1, o2) -> {
            try {
                Date d1 = parseNotificationDate(o1.getTime());
                Date d2 = parseNotificationDate(o2.getTime());
                return d2.compareTo(d1);
            } catch (Exception e) {
                return 0;
            }
        });
        adapter.updateData(notificationList);
    }

    private Date parseNotificationDate(String timeStr) {
        try {
            if (timeStr == null) return new Date(0);
            try { return sdfFull.parse(timeStr); } catch (Exception ignored) {}
            try { return sdfDay.parse(timeStr); } catch (Exception ignored) {}
        } catch (Exception e) { e.printStackTrace(); }
        return new Date(0);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (attendanceListener != null) attendanceListener.remove();
        if (leaveListener != null) leaveListener.remove();
        if (overtimeListener != null) overtimeListener.remove();
        if (reminderListener != null) reminderListener.remove();
        if (projectListener != null) projectListener.remove();
        if (employeeListener != null) employeeListener.remove();
    }
}