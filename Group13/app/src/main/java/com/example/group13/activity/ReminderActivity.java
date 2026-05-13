package com.example.group13.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.example.group13.adapter.ReminderAdapter;
import com.example.group13.model.Reminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReminderActivity extends BaseActivity {

    // UI
    private ImageButton btnBack, btnAdd;
    private RecyclerView recyclerView;
    private TextView tabPending, tabDone, tabAll;

    // Data
    private FirebaseFirestore db;
    private ReminderAdapter adapter;

    private final List<Reminder> allReminders = new ArrayList<>();
    private final List<Reminder> displayReminders = new ArrayList<>();

    private enum Filter { PENDING, DONE, ALL }
    private Filter currentFilter = Filter.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_reminder);

        db = FirebaseFirestore.getInstance();

        setupNavigation();
        initViews();
        setupRecyclerView();
        setupListeners();

        loadReminders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnAdd = findViewById(R.id.buttonAdd);
        recyclerView = findViewById(R.id.listTasks);

        tabPending = findViewById(R.id.tabPending);
        tabDone = findViewById(R.id.tabDone);
        tabAll = findViewById(R.id.tabAll);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderAdapter(this, displayReminders);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAdd.setOnClickListener(v ->
                new AddReminderDialog(this, this::loadReminders).show()
        );

        tabPending.setOnClickListener(v -> {
            currentFilter = Filter.PENDING;
            applyFilter();
        });

        tabDone.setOnClickListener(v -> {
            currentFilter = Filter.DONE;
            applyFilter();
        });

        tabAll.setOnClickListener(v -> {
            currentFilter = Filter.ALL;
            applyFilter();
        });
    }

    // ================= FIREBASE =================
    private void loadReminders() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("reminders")
                .whereEqualTo("userId", uid)
                .orderBy("deadlineMillis")
                .get()
                .addOnSuccessListener(snapshot -> {

                    allReminders.clear();

                    for (var doc : snapshot) {
                        Reminder r = doc.toObject(Reminder.class);
                        if (r == null) continue;

                        r.setId(doc.getId());
                        allReminders.add(r);
                    }

                    applyFilter();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Load failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // ================= FILTER =================
    private void applyFilter() {

        displayReminders.clear();

        for (Reminder r : allReminders) {
            if (currentFilter == Filter.ALL ||
                    (currentFilter == Filter.PENDING && !r.isDone()) ||
                    (currentFilter == Filter.DONE && r.isDone())) {

                displayReminders.add(r);
            }
        }

        // 🔽 SORT THEO PRIORITY
        displayReminders.sort((r1, r2) ->
                getPriorityValue(r1.getPriority()) - getPriorityValue(r2.getPriority())
        );

        adapter.notifyDataSetChanged();
        updateTabUI();
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 3;
        switch (priority) {
            case "High": return 0;
            case "Medium": return 1;
            case "Low": return 2;
            default: return 3;
        }
    }

    private void updateTabUI() {
        tabPending.setAlpha(currentFilter == Filter.PENDING ? 1f : 0.5f);
        tabDone.setAlpha(currentFilter == Filter.DONE ? 1f : 0.5f);
        tabAll.setAlpha(currentFilter == Filter.ALL ? 1f : 0.5f);
    }

    // ================= SAVE REMINDER =================
    public void saveReminder(Reminder reminder) {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        reminder.setUserId(uid);

        CollectionReference ref = db.collection("reminders");

        if (reminder.getId() != null && !reminder.getId().isEmpty()) {
            // Cập nhật Reminder
            ref.document(reminder.getId())
                    .set(reminder, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show();
                        loadReminders(); // chỉ load lại, không tạo notification
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // Thêm Reminder mới
            ref.add(reminder)
                    .addOnSuccessListener(docRef -> {
                        reminder.setId(docRef.getId());
                        Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                        loadReminders(); // chỉ load lại, không tạo notification
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Add failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}