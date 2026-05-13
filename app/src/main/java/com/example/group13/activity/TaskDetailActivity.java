package com.example.group13.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.model.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    ImageButton btnBack;
    ImageView btnEdit, btnDelete;

    TextView tvNameDetail, tvProject, tvEmployee,
            tvDeadline, tvDescription, tvStatus;

    FirebaseFirestore db;

    String taskId;
    Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        db = FirebaseFirestore.getInstance();

        taskId = getIntent().getStringExtra("taskId");
        if (taskId == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewsByIds();
        setOnClickListeners();
        loadTaskDetail();
    }

    private void findViewsByIds() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnEdit = findViewById(R.id.btnEditDetail);
        btnDelete = findViewById(R.id.btnDeleteDetail);

        tvNameDetail = findViewById(R.id.tvNameDetail);
        tvProject = findViewById(R.id.tvProject);
        tvEmployee = findViewById(R.id.tvEmployee);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setOnClickListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskAddEditActivity.class);
            intent.putExtra("taskId", taskId);
            intent.putExtra("projectId", currentTask.getProjectId());
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void loadTaskDetail() {

        db.collection("tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        finish();
                        return;
                    }

                    currentTask = doc.toObject(Task.class);
                    if (currentTask == null) return;

                    displayTask(currentTask);
                    loadEmployeeName(currentTask.getEmployeeId());
                    loadProjectName(currentTask.getProjectId());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed", Toast.LENGTH_SHORT).show()
                );
    }

    private void displayTask(Task task) {

        tvNameDetail.setText(task.getTitle());
        tvDescription.setText(task.getDescription());

        if (task.getDeadline() > 0) {
            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDeadline.setText(sdf.format(new Date(task.getDeadline())));
        } else {
            tvDeadline.setText("No deadline");
        }

        tvStatus.setText(task.isDone() ? "Completed" : "In progress");
        tvStatus.setTextColor(
                getColor(task.isDone() ? R.color.green : R.color.orange)
        );
    }

    private void loadEmployeeName(String employeeId) {
        db.collection("users")
                .document(employeeId)
                .get()
                .addOnSuccessListener(doc ->
                        tvEmployee.setText(doc.getString("employeeName"))
                );
    }

    private void loadProjectName(String projectId) {
        db.collection("projects")
                .document(projectId)
                .get()
                .addOnSuccessListener(doc ->
                        tvProject.setText(doc.getString("name"))
                );
    }

    private void showDeleteConfirmDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (d, w) -> deleteTask())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask() {

        db.collection("tasks")
                .document(taskId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                );
    }
}