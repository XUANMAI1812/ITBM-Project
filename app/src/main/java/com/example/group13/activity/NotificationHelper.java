package com.example.group13.activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NotificationHelper {
    public static void pushNotification(String title, String content) {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("timestamp", System.currentTimeMillis());
        data.put("time", new SimpleDateFormat(
                "dd/MM HH:mm", Locale.getDefault()
        ).format(new Date()));

        db.collection("notifications")
                .document(uid)
                .collection("items")
                .add(data);
    }
    public static void notifyEmployeeAdded(String employeeName) {
        pushNotification("New Employee", "You have added employee " + employeeName);
    }

    public static void notifyEmployeeUpdated(String employeeName) {
        pushNotification("Employee Updated", "You have updated employee " + employeeName);
    }

    public static void notifyProjectCreated(String projectName) {
        pushNotification("New Project", "Project \"" + projectName + "\" has been created");
    }

    public static void notifyProjectUpdated(String projectName) {
        pushNotification("Project Updated", "Project \"" + projectName + "\" has been updated");
    }

    public static void notifyTaskAssigned(String taskName) {
        pushNotification("Task Assigned", "You have been assigned task \"" + taskName + "\"");
    }

    public static void notifyTaskCompleted(String taskName) {
        pushNotification("Task Completed", "Task \"" + taskName + "\" has been completed");
    }

    public static void notifyReminderSet(String reminderTitle) {
        pushNotification("Reminder Set", "Reminder \"" + reminderTitle + "\" has been set");
    }

    public static void notifyReminderDue(String reminderTitle) {
        pushNotification("Reminder Due", "Reminder \"" + reminderTitle + "\" is due now");
    }
}