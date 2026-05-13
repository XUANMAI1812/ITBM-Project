package com.example.group13.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.CostAdapter;
import com.example.group13.model.Cost;
import com.example.group13.model.Project;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CostActivity extends AppCompatActivity {

    TextView tvProjectName, tvTotal, tvProjectId;
    ImageButton btnAddPending;
    Button btnSave, btnBack;
    RecyclerView rvPending, rvApproved;

    ArrayList<Cost> pendingList = new ArrayList<>();
    ArrayList<Cost> approvedList = new ArrayList<>();

    CostAdapter pendingAdapter, approvedAdapter;

    FirebaseFirestore db;
    String projectDocId;
    String projectID;

    AlertDialog currentDialog = null; // lưu dialog đang mở

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost);

        projectDocId = getIntent().getStringExtra("project_doc_id");
        if (projectDocId == null) {
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        loadProjectInfo();
        setupActions();
    }

    private void initViews() {
        tvProjectId = findViewById(R.id.tvProjectId);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvTotal = findViewById(R.id.tvTotal);
        btnAddPending = findViewById(R.id.btnAddPendingCost);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
        rvPending = findViewById(R.id.rvPendingCost);
        rvApproved = findViewById(R.id.rvApprovedCost);
    }

    private void setupRecyclerView() {
        pendingAdapter = new CostAdapter(pendingList, this::showEditDialog);
        approvedAdapter = new CostAdapter(approvedList, this::showEditDialog);

        rvPending.setLayoutManager(new LinearLayoutManager(this));
        rvApproved.setLayoutManager(new LinearLayoutManager(this));

        rvPending.setAdapter(pendingAdapter);
        rvApproved.setAdapter(approvedAdapter);
    }

    private void setupActions() {
        btnBack.setOnClickListener(v -> finish());
        btnAddPending.setOnClickListener(v -> addNewPendingCost());
        btnSave.setOnClickListener(v -> {
            saveAllCosts();
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss(); // đóng dialog nếu đang mở
            }
        });
    }

    private void loadProjectInfo() {
        db.collection("projects")
                .document(projectDocId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    Project p = doc.toObject(Project.class);
                    if (p == null) return;

                    projectID = p.getProjectId();

                    tvProjectId.setText("Project ID: " + projectID);
                    tvProjectName.setText("Project Name: " + p.getName());

                    loadCostData(projectID);
                });
    }

    private void loadCostData(String projectID) {
        db.collection("costs")
                .whereEqualTo("projectId", projectID)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    pendingList.clear();
                    approvedList.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Cost c = d.toObject(Cost.class);
                        if (c != null) {
                            c.setId(d.getId());
                            if (c.getStatus() == Cost.APPROVED)
                                approvedList.add(c);
                            else
                                pendingList.add(c);
                        }
                    }

                    pendingAdapter.notifyDataSetChanged();
                    approvedAdapter.notifyDataSetChanged();
                    calcTotal();
                });
    }

    private void addNewPendingCost() {
        if (projectID == null) return;

        Cost c = new Cost("New cost", 0, projectID, Cost.PENDING);
        pendingList.add(c);
        pendingAdapter.notifyItemInserted(pendingList.size() - 1);

        // mở dialog ngay
        showEditDialog(c);
    }

    private void saveAllCosts() {
        for (Cost c : pendingList) saveCostToFirestore(c);
        for (Cost c : approvedList) saveCostToFirestore(c);
        Toast.makeText(this, "Saved to Firestore", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void saveCostToFirestore(Cost cost) {
        if (cost.getId() != null) {
            db.collection("costs").document(cost.getId()).set(cost);
        } else {
            db.collection("costs").add(cost).addOnSuccessListener(docRef -> cost.setId(docRef.getId()));
        }
    }

    private void calcTotal() {
        long total = 0;
        for (Cost c : approvedList) total += c.getAmount();
        tvTotal.setText("Total approved: " + total + "đ");
    }

    private void showEditDialog(Cost cost) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        android.view.LayoutInflater inflater = this.getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_edit_cost, null);
        builder.setView(dialogView);

        EditText edtName = dialogView.findViewById(R.id.edtCostName);
        EditText edtAmount = dialogView.findViewById(R.id.edtCostAmount);
        RadioButton rbPending = dialogView.findViewById(R.id.rbPending);
        RadioButton rbApproved = dialogView.findViewById(R.id.rbApproved);
        Button btnSaveDialog = dialogView.findViewById(R.id.btnSave);
        Button btnDeleteDialog = dialogView.findViewById(R.id.btnDelete);
        Button btnBackDialog = dialogView.findViewById(R.id.btnBack);

        edtName.setText(cost.getName());
        edtAmount.setText(String.valueOf(cost.getAmount()));
        if (cost.getStatus() == Cost.APPROVED)
            rbApproved.setChecked(true);
        else
            rbPending.setChecked(true);

        currentDialog = builder.create();
        currentDialog.show();

        btnSaveDialog.setOnClickListener(v -> {
            cost.setName(edtName.getText().toString().trim());
            try {
                cost.setAmount(Long.parseLong(edtAmount.getText().toString().trim()));
            } catch (NumberFormatException e) {
                cost.setAmount(0);
            }
            cost.setStatus(rbApproved.isChecked() ? Cost.APPROVED : Cost.PENDING);

            // di chuyển giữa Pending / Approved
            pendingList.remove(cost);
            approvedList.remove(cost);
            if (cost.getStatus() == Cost.APPROVED)
                approvedList.add(cost);
            else
                pendingList.add(cost);

            pendingAdapter.notifyDataSetChanged();
            approvedAdapter.notifyDataSetChanged();
            calcTotal();

            currentDialog.dismiss();
            currentDialog = null;
        });

        btnDeleteDialog.setOnClickListener(v -> {
            pendingList.remove(cost);
            approvedList.remove(cost);
            pendingAdapter.notifyDataSetChanged();
            approvedAdapter.notifyDataSetChanged();
            if (cost.getId() != null)
                db.collection("costs").document(cost.getId()).delete();
            currentDialog.dismiss();
            currentDialog = null;
        });

        btnBackDialog.setOnClickListener(v -> {
            currentDialog.dismiss();
            currentDialog = null;
        });
    }
}