package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.SalaryAdapter;
import com.example.group13.base.BaseActivity;
import com.example.group13.model.Salary;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SalaryListActivity extends BaseActivity {

    ImageButton btnBack, btnAddSalary;
    RecyclerView recyclerSalary;
    EditText searchEmployee;

    SalaryAdapter adapter;
    List<Salary> salaryList = new ArrayList<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_list);

        db = FirebaseFirestore.getInstance();

        setupNavigation();
        bindViews();

        searchEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSalaries(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupRecycler();
        setupActions();
        applyPermissionUI();

        loadSalaries();
    }

    private void filterSalaries(String query) {
        List<Salary> filteredList = new ArrayList<>();
        for (Salary salary : salaryList) {
            if (salary.getEmployeeName() != null &&
                    salary.getEmployeeName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(salary);
            }
        }
        adapter.update(filteredList);
    }

    private void bindViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnAddSalary = findViewById(R.id.btn_addpayment);
        recyclerSalary = findViewById(R.id.recyclerSalary);
        searchEmployee = findViewById(R.id.searchEmployee);
    }

    private void applyPermissionUI() {
        if (!canManageEmployee()) {
            // user thường → chỉ xem list
            btnAddSalary.setEnabled(false);
            btnAddSalary.setAlpha(0.3f); // mờ cho biết là bị khóa
        }
    }

    private void setupRecycler() {
        adapter = new SalaryAdapter(this, salaryList, new SalaryAdapter.OnItemClickListener() {

            @Override
            public void onClick(Salary salary) {
                if (!canManageEmployee()) {
                    showNoPermission();
                    return;
                }

                Intent i = new Intent(
                        SalaryListActivity.this,
                        SalaryAddEditActivity.class
                );
                i.putExtra("salaryId", salary.getId());
                startActivity(i);
            }

            @Override
            public void onEdit(Salary salary) {
                if (!canManageEmployee()) {
                    showNoPermission();
                    return;
                }

                Intent i = new Intent(
                        SalaryListActivity.this,
                        SalaryAddEditActivity.class
                );
                i.putExtra("salaryId", salary.getId());
                startActivity(i);
            }

            @Override
            public void onDelete(Salary salary) {
                if (!canManageEmployee()) {
                    showNoPermission();
                    return;
                }

                deleteSalary(salary);
            }
        });

        recyclerSalary.setLayoutManager(new LinearLayoutManager(this));
        recyclerSalary.setAdapter(adapter);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());

        btnAddSalary.setOnClickListener(v -> {
            if (!canManageEmployee()) {
                showNoPermission();
                return;
            }

            startActivity(
                    new Intent(
                            SalaryListActivity.this,
                            SalaryAddEditActivity.class
                    )
            );
        });
    }

    private void loadSalaries() {
        db.collection("salary")
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        Toast.makeText(
                                this,
                                "Load salary failed",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    salaryList.clear();

                    if (snap != null) {
                        for (DocumentSnapshot doc : snap.getDocuments()) {
                            Salary s = doc.toObject(Salary.class);
                            if (s != null) {
                                s.setId(doc.getId());
                                salaryList.add(s);
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void deleteSalary(Salary salary) {
        db.collection("salary")
                .document(salary.getId())
                .delete()
                .addOnSuccessListener(v ->
                        Toast.makeText(
                                this,
                                "Deleted",
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Delete failed",
                                Toast.LENGTH_SHORT
                        ).show()
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