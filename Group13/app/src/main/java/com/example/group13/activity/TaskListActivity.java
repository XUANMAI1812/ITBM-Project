package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.TaskAdapter;
import com.example.group13.base.BaseActivity;
import com.example.group13.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends BaseActivity {

    private ImageButton btnAddTask, btnBack;
    private RecyclerView recyclerTask;
    private EditText searchTask;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private List<String> taskIdList;

    private FirebaseFirestore db;

    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupNavigation();
        findViewsByIds();

        searchTask = findViewById(R.id.searchTask);
        searchTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setOnClickListeners();

        db = FirebaseFirestore.getInstance();

        taskList = new ArrayList<>();
        taskIdList = new ArrayList<>();

        taskAdapter = new TaskAdapter(this, taskList, taskIdList);
        recyclerTask.setLayoutManager(new LinearLayoutManager(this));
        recyclerTask.setAdapter(taskAdapter);

        loadTasksFromFirestore();
    }

    private void filterTasks(String query) {
        List<Task> filteredList = new ArrayList<>();
        List<String> filteredIdList = new ArrayList<>();
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            if (task.getTitle() != null &&
                    task.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(task);
                filteredIdList.add(taskIdList.get(i));
            }
        }
        taskAdapter.update(filteredList, filteredIdList);
    }

    private void findViewsByIds() {
        btnAddTask = findViewById(R.id.btn_addtask);
        btnBack = findViewById(R.id.imageButtonBack);
        recyclerTask = findViewById(R.id.recyclerTask);
    }

    private void setOnClickListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskAddEditActivity.class);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });
    }

    private void loadTasksFromFirestore() {

        db.collection("tasks")
                .whereEqualTo("projectId", projectId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    taskList.clear();
                    taskIdList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Task task = doc.toObject(Task.class);
                        taskList.add(task);
                        taskIdList.add(doc.getId());
                    }

                    taskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load tasks",
                                Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromFirestore();
    }
}