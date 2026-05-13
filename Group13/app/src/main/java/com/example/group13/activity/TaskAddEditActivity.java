package com.example.group13.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.model.Task;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class TaskAddEditActivity extends AppCompatActivity {

    ImageButton btnBack;
    Button btnSave, btnCancel;

    EditText etTaskName, etDescription;
    TextView txtEmployee, txtProject, txtDeadline;
    LinearLayout layoutDeadline, layoutChooseEmployee;

    FirebaseFirestore db;

    long deadlineMillis = 0;
    String taskId;
    boolean isEditMode = false;

    String projectId;
    String projectName;

    // employee
    List<String> employeeIds = new ArrayList<>();
    List<String> employeeNames = new ArrayList<>();
    String selectedEmployeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_add_edit);

        db = FirebaseFirestore.getInstance();
        findViewsByIds();

        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProjectInfo();
        setOnClickListeners();
        checkEditMode();
    }

    private void findViewsByIds() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        etTaskName = findViewById(R.id.etTaskName);
        etDescription = findViewById(R.id.etDescription);

        txtEmployee = findViewById(R.id.txtEmployee);
        txtProject = findViewById(R.id.txtProject);
        txtDeadline = findViewById(R.id.txtDeadline);

        layoutDeadline = findViewById(R.id.layoutDeadline);
        layoutChooseEmployee = findViewById(R.id.layoutChooseEmployee);
    }

    private void loadProjectInfo() {
        db.collection("projects")
                .document(projectId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    projectName = doc.getString("name");
                    txtProject.setText(projectName);
                });
    }

    private void setOnClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        layoutDeadline.setOnClickListener(v -> showDatePicker());
        layoutChooseEmployee.setOnClickListener(v -> loadEmployeesInProject());
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void checkEditMode() {
        taskId = getIntent().getStringExtra("taskId");
        if (taskId == null) return;

        isEditMode = true;

        db.collection("tasks")
                .document(taskId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Task task = doc.toObject(Task.class);
                    if (task == null) return;

                    etTaskName.setText(task.getTitle());
                    etDescription.setText(task.getDescription());
                    deadlineMillis = task.getDeadline();

                    SimpleDateFormat sdf =
                            new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    txtDeadline.setText(sdf.format(deadlineMillis));

                    selectedEmployeeId = task.getEmployeeId();
                    db.collection("users")
                            .document(selectedEmployeeId)
                            .get()
                            .addOnSuccessListener(u ->
                                    txtEmployee.setText(u.getString("employeeName"))
                            );

                });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(y, m, d);
            showTimePicker(selected);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(Calendar selected) {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (v, h, m) -> {
            selected.set(Calendar.HOUR_OF_DAY, h);
            selected.set(Calendar.MINUTE, m);
            deadlineMillis = selected.getTimeInMillis();

            SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            txtDeadline.setText(sdf.format(selected.getTime()));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void loadEmployeesInProject() {

        db.collection("projects")
                .document(projectId)
                .get()
                .addOnSuccessListener(projectDoc -> {

                    List<String> memberIds =
                            (List<String>) projectDoc.get("memberIds");

                    if (memberIds == null || memberIds.isEmpty()) {
                        Toast.makeText(this,
                                "Project has no members",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    employeeIds.clear();
                    employeeNames.clear();

                    db.collection("users")
                            .whereIn(FieldPath.documentId(), memberIds)
                            .get()
                            .addOnSuccessListener(qs -> {
                                for (DocumentSnapshot d : qs) {
                                    employeeIds.add(d.getId());
                                    employeeNames.add(d.getString("employeeName"));
                                }
                                showEmployeeDialog();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Failed to load employees",
                                            Toast.LENGTH_SHORT).show()
                            );
                });
    }

    private void showEmployeeDialog() {
        String[] names = employeeNames.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Choose employee")
                .setItems(names, (d, i) -> {
                    txtEmployee.setText(names[i]);
                    selectedEmployeeId = employeeIds.get(i);
                })
                .show();
    }
    private void saveTask() {
        String title = etTaskName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTaskName.setError("Required");
            etTaskName.requestFocus();
            return;
        }

        if (selectedEmployeeId == null) {
            Toast.makeText(this, "Choose employee", Toast.LENGTH_SHORT).show();
            return;
        }

        if (deadlineMillis == 0) {
            Toast.makeText(this, "Choose deadline", Toast.LENGTH_SHORT).show();
            return;
        }

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setEmployeeId(selectedEmployeeId);
        task.setProjectId(projectId);
        task.setDeadline(deadlineMillis);

        if (!isEditMode) task.setDone(false);

        if (isEditMode) {
            db.collection("tasks")
                    .document(taskId)
                    .set(task)
                    .addOnSuccessListener(unused -> {
                        NotificationHelper.pushNotification(
                                "Task Updated",
                                "Task \"" + title + "\" has been updated"
                        );
                        Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update task", Toast.LENGTH_SHORT).show()
                    );
        } else {
            db.collection("tasks")
                    .add(task)
                    .addOnSuccessListener(unused -> {
                        NotificationHelper.pushNotification(
                                "New Task",
                                "Task \"" + title + "\" assigned to " + txtEmployee.getText().toString()
                        );
                        Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show()
                    );
        }
    }
}