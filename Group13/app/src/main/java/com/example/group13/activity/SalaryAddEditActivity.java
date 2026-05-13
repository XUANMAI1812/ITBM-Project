package com.example.group13.activity;

import android.os.Bundle;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.SalaryItemAdapter;
import com.example.group13.model.Salary;
import com.example.group13.model.SalaryItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalaryAddEditActivity extends AppCompatActivity {

    EditText etEmployeeId, etEmployeeName, etBaseSalary, etWorkHours;
    AutoCompleteTextView autoPosition;
    TextView tvTotal;
    ImageButton btnBack, btnAddItem;
    Button btnSave, btnCancel, btnDelete;

    RecyclerView recyclerSalary;

    FirebaseFirestore db;
    String salaryId;
    boolean isEdit;

    List<SalaryItem> items = new ArrayList<>();
    SalaryItemAdapter itemAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_addedit);

        db = FirebaseFirestore.getInstance();
        salaryId = getIntent().getStringExtra("salaryId");
        isEdit = salaryId != null;

        bindViews();

        ArrayAdapter<String> positionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        POSITION_LIST
                );

        autoPosition.setAdapter(positionAdapter);
        autoPosition.setThreshold(0);

        setupRecycler();
        setupActions();

        if (isEdit) loadDetail();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);

        etEmployeeId = findViewById(R.id.etEmployeeId);
        etEmployeeName = findViewById(R.id.etEmployeeName);
        autoPosition = findViewById(R.id.autoPosition);
        etBaseSalary = findViewById(R.id.etBaseSalary);
        etWorkHours = findViewById(R.id.etWorkHours);
        tvTotal = findViewById(R.id.tvTotal);

        recyclerSalary = findViewById(R.id.recyclerSalary);
    }

    private void setupRecycler() {
        itemAdapter = new SalaryItemAdapter(items, this::updateTotal);
        recyclerSalary.setLayoutManager(new LinearLayoutManager(this));
        recyclerSalary.setAdapter(itemAdapter);
    }

    private void setupActions() {

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnAddItem.setOnClickListener(v -> {
            items.add(new SalaryItem("", 0));
            itemAdapter.notifyItemInserted(items.size() - 1);
        });
        autoPosition.setOnClickListener(v -> autoPosition.showDropDown());
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> delete());
    }

    private void loadDetail() {
        db.collection("salary")
                .document(salaryId)
                .get()
                .addOnSuccessListener(doc -> {
                    Salary s = doc.toObject(Salary.class);
                    if (s == null) return;

                    etEmployeeId.setText(s.getEmployeeId());
                    etEmployeeName.setText(s.getEmployeeName());
                    autoPosition.setText(s.getPosition());
                    etBaseSalary.setText(String.valueOf(s.getBaseSalary()));
                    etWorkHours.setText(String.valueOf(s.getWorkHours()));

                    items.clear();
                    if (s.getItems() != null)
                        items.addAll(s.getItems());

                    itemAdapter.notifyDataSetChanged();
                    updateTotal();
                });
    }

    private static final String[] POSITION_LIST = {
            "Engineering Manager",
            "Tech Lead",
            "Senior Software Engineer",
            "Software Engineer",
            "Junior Software Engineer",
            "Mobile Developer",
            "Backend Developer",
            "Frontend Developer",
            "Fullstack Developer",
            "QA Engineer",
            "DevOps Engineer",
            "UI/UX Designer",
            "Product Manager",
            "Business Analyst",
            "HR Executive",
            "Accountant"
    };



    private void save() {

        long base = parseLong(etBaseSalary);
        int hours = parseInt(etWorkHours);

        Salary salary = new Salary(
                etEmployeeId.getText().toString().trim(),
                etEmployeeName.getText().toString().trim(),
                autoPosition.getText().toString().trim(),
                base,
                hours,
                items
        );

        if (isEdit) {
            db.collection("salary")
                    .document(salaryId)
                    .set(salary)
                    .addOnSuccessListener(v -> finish());
        } else {
            db.collection("salary")
                    .add(salary)
                    .addOnSuccessListener(v -> finish());
        }
    }

    private void delete() {
        if (!isEdit) return;
        db.collection("salary")
                .document(salaryId)
                .delete()
                .addOnSuccessListener(v -> finish());
    }

    private void updateTotal() {
        long total = parseLong(etBaseSalary) * parseInt(etWorkHours);
        for (SalaryItem i : items) total += i.getAmount();

        tvTotal.setText("Total: " +
                NumberFormat.getInstance(new Locale("vi","VN"))
                        .format(total) + " ₫");
    }

    private long parseLong(EditText e) {
        try { return Long.parseLong(e.getText().toString()); }
        catch (Exception ex) { return 0; }
    }

    private int parseInt(EditText e) {
        try { return Integer.parseInt(e.getText().toString()); }
        catch (Exception ex) { return 0; }
    }
}