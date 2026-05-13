package com.example.group13.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.EmployeeAdapter;
import com.example.group13.base.BaseActivity;
import com.example.group13.model.Employee;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

public class EmployeeListActivity extends BaseActivity {

    private ImageButton btnBack, btnAddEmployee;
    private RecyclerView recyclerEmployee;
    private EditText searchEmployee;
    private EmployeeAdapter employeeAdapter;
    private final List<Employee> employeeList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);

        setupNavigation();

        db = FirebaseFirestore.getInstance();

        bindViews();

        searchEmployee.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEmployees(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        setupRecycler();
        setupActions();

        loadEmployeesFromFirebase();
    }

    private void filterEmployees(String query) {
        List<Employee> filteredList = new ArrayList<>();
        for (Employee employee : employeeList) {
            if (employee.getEmployeeName() != null &&
                    employee.getEmployeeName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(employee);
            }
        }
        employeeAdapter.updateList(filteredList);
    }

    private void bindViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);
        recyclerEmployee = findViewById(R.id.recyclerEmployee);
        searchEmployee = findViewById(R.id.searchEmployee);
    }

    private void setupRecycler() {
        recyclerEmployee.setLayoutManager(new LinearLayoutManager(this));

        employeeAdapter = new EmployeeAdapter(this, employeeList);
        recyclerEmployee.setAdapter(employeeAdapter);

        employeeAdapter.setOnEmployeeActionListener(
                new EmployeeAdapter.OnEmployeeActionListener() {

                    @Override
                    public void onView(Employee employee) {
                        Intent intent = new Intent(
                                EmployeeListActivity.this,
                                EmployeeDetailActivity.class
                        );
                        intent.putExtra("EMPLOYEE_ID", employee.getId());
                        startActivity(intent);
                    }

                    @Override
                    public void onEdit(Employee employee) {
                        if (!canManageEmployee()) {
                            showNoPermission();
                            return;
                        }

                        Intent intent = new Intent(
                                EmployeeListActivity.this,
                                EmployeeAddEditActivity.class
                        );
                        intent.putExtra("EMPLOYEE_ID", employee.getId());
                        intent.putExtra("IS_EDIT", true);
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(Employee employee) {
                        if (!canManageEmployee()) {
                            showNoPermission();
                            return;
                        }

                        new AlertDialog.Builder(EmployeeListActivity.this)
                                .setTitle("Remove this employee?")
                                .setMessage(
                                        "Once you delete this employee, the information cannot be recovered. Proceed?"
                                )
                                .setPositiveButton("Delete", (dialog, which) -> {

                                    db.collection("users")
                                            .document(employee.getId())
                                            .delete()
                                            .addOnSuccessListener(v ->
                                                    Toast.makeText(
                                                            EmployeeListActivity.this,
                                                            "Employee removed",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            )
                                            .addOnFailureListener(err ->
                                                    Toast.makeText(
                                                            EmployeeListActivity.this,
                                                            "Failed to remove employee",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                }
        );
    }

    private void setupActions() {

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        btnAddEmployee.setOnClickListener(v -> {
            if (!canManageEmployee()) {
                showNoPermission();
                return;
            }

            Intent intent = new Intent(
                    EmployeeListActivity.this,
                    EmployeeAddEditActivity.class
            );
            intent.putExtra("IS_EDIT", false);
            startActivity(intent);
        });
    }

    private void loadEmployeesFromFirebase() {
        db.collection("users")
                .whereEqualTo("profileCompleted", true)
                .get(Source.SERVER)
                .addOnSuccessListener(querySnapshot -> {
                    employeeList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Employee employee = doc.toObject(Employee.class);
                        if (employee != null) {
                            employee.setId(doc.getId());
                            employeeList.add(employee);
                        }
                    }

                    employeeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEmployeesFromFirebase();
    }

    private void showNoPermission() {
        Toast.makeText(
                this,
                "You do not have permission to access this function",
                Toast.LENGTH_SHORT
        ).show();
    }
}