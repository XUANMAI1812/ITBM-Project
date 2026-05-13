package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.example.group13.model.Employee;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmployeeDetailActivity extends BaseActivity {

    ImageButton btnBack;
    ImageView btnEdit, btnDelete;

    TextView tvName, tvPosition, tvPhone, tvEmail,
            tvId, tvBirthday, tvGender, tvDepartment, tvStartDate;

    FirebaseFirestore db;
    String employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        setupNavigation();

        db = FirebaseFirestore.getInstance();
        employeeId = getIntent().getStringExtra("EMPLOYEE_ID");

        if (employeeId == null) {
            Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupActions();
        applyPermission();
        loadEmployeeDetail();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnEdit = findViewById(R.id.btnEditDetail);
        btnDelete = findViewById(R.id.btnDeleteDetail);

        tvName = findViewById(R.id.tvNameDetail);
        tvPosition = findViewById(R.id.tvPositionDetail);
        tvPhone = findViewById(R.id.tvPhoneDetail);
        tvEmail = findViewById(R.id.tvEmailDetail);

        tvId = findViewById(R.id.tvIdDetail);
        tvBirthday = findViewById(R.id.tvBirthday);
        tvGender = findViewById(R.id.tvGender);
        tvDepartment = findViewById(R.id.tvDepartment);
        tvStartDate = findViewById(R.id.tvStartDate);
    }

    private void setupActions() {

        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            if (!canManageEmployee()) {
                showNoPermission();
                return;
            }

            Intent intent = new Intent(
                    EmployeeDetailActivity.this,
                    EmployeeAddEditActivity.class
            );
            intent.putExtra("EMPLOYEE_ID", employeeId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            if (!canManageEmployee()) {
                showNoPermission();
                return;
            }
            showDeleteDialog();
        });
    }

    private void applyPermission() {
        boolean canManage = canManageEmployee();
        btnEdit.setVisibility(canManage ? ImageView.VISIBLE : ImageView.GONE);
        btnDelete.setVisibility(canManage ? ImageView.VISIBLE : ImageView.GONE);
    }

    private void loadEmployeeDetail() {

        db.collection("users")
                .document(employeeId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    Employee e = doc.toObject(Employee.class);
                    if (e == null) return;

                    tvName.setText(e.getEmployeeName());
                    tvPosition.setText(e.getPosition());
                    tvPhone.setText(e.getPhone());
                    tvEmail.setText(e.getEmail());

                    tvId.setText(e.getEmployeeId());
                    tvBirthday.setText(e.getDateOfBirth());
                    tvGender.setText(e.getGender());
                    tvDepartment.setText(e.getDepartment());
                    tvStartDate.setText(e.getStartDate());
                })
                .addOnFailureListener(err ->
                        Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete employee")
                .setMessage("Are you sure you want to delete this employee?")
                .setPositiveButton("Delete", (d, w) -> deleteEmployee())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEmployee() {
        db.collection("users")
                .document(employeeId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Employee deleted", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(
                            EmployeeDetailActivity.this,
                            EmployeeListActivity.class
                    );
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showNoPermission() {
        Toast.makeText(
                this,
                "You do not have permission to access this function",
                Toast.LENGTH_SHORT
        ).show();
    }
}