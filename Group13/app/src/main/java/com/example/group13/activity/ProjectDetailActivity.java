package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.model.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectDetailActivity extends AppCompatActivity {

    ImageButton btnBack;
    Button btnTaskList;
    ImageView btnEdit, btnDelete;

    TextView tvNameDetail, tvProjectName, tvManager, tvStartDate, tvEndDate,
            tvDescription, tvStatus, tvTeamMembers, tvProjectCost;

    ImageView imgProjectDetail;

    FirebaseFirestore db;
    String projectId;
    String currentUserId;

    Map<String, String> userNameCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            Toast.makeText(this, "Project not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewsByIds();
        setOnClickListeners();
        loadUsersThenProject();
    }

    private void findViewsByIds() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnTaskList = findViewById(R.id.btnViewTask);
        btnEdit = findViewById(R.id.btnEditDetail);
        btnDelete = findViewById(R.id.btnDeleteDetail);

        imgProjectDetail = findViewById(R.id.imgEmployeeDetail);

        tvNameDetail = findViewById(R.id.tvNameDetail);
        tvProjectName = findViewById(R.id.tvProjectName);
        tvManager = findViewById(R.id.tvManager);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvDescription = findViewById(R.id.tvDescription);
        tvStatus = findViewById(R.id.tvStatus);
        tvTeamMembers = findViewById(R.id.tvTeamMembers);
        tvProjectCost = findViewById(R.id.tvProjectcost);
    }

    private void setOnClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnTaskList.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskListActivity.class);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProjectAddEditActivity.class);
            intent.putExtra("projectId", projectId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> deleteProject());
    }

    private void loadUsersThenProject() {
        db.collection("users")
                .get()
                .addOnSuccessListener(qs -> {
                    for (var doc : qs.getDocuments()) {
                        String name = doc.getString("employeeName");
                        if (name != null) {
                            userNameCache.put(doc.getId(), name);
                        }
                    }
                    listenProjectDetail();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }

    private void listenProjectDetail() {
        DocumentReference projectRef = db.collection("projects").document(projectId);

        projectRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ProjectDetailActivity.this, "Failed to load project", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Project p = snapshot.toObject(Project.class);
                    if (p != null) updateUI(p);
                }
            }
        });
    }

    private void updateUI(Project p) {
        String projectName = p.getName() != null ? p.getName() : "-";
        String managerName = userNameCache.getOrDefault(p.getManagerId(), "Unknown");

        tvNameDetail.setText(projectName);
        tvProjectName.setText(projectName);
        tvManager.setText(managerName);
        tvStartDate.setText(p.getStartDate() != null ? p.getStartDate() : "-");
        tvEndDate.setText(p.getEndDate() != null ? p.getEndDate() : "-");
        tvDescription.setText(p.getDescription() != null ? p.getDescription() : "-");
        tvStatus.setText(p.getStatus() != null ? p.getStatus() : "-");
        tvProjectCost.setText(p.getCost() > 0 ? String.valueOf(p.getCost()) : "0");

        List<String> memberNames = p.getMemberIds() != null ?
                p.getMemberIds().stream()
                        .map(id -> userNameCache.getOrDefault(id, "Unknown"))
                        .collect(Collectors.toList())
                : List.of();

        tvTeamMembers.setText(memberNames.isEmpty() ? "-" : String.join(", ", memberNames));

        boolean canEditOrDelete = isAdmin() || (p.getManagerId() != null && p.getManagerId().equals(currentUserId));
        btnEdit.setVisibility(canEditOrDelete ? ImageView.VISIBLE : ImageView.GONE);
        btnDelete.setVisibility(canEditOrDelete ? ImageView.VISIBLE : ImageView.GONE);
    }

    private void deleteProject() {
        db.collection("projects")
                .document(projectId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isAdmin() {
        String adminUid = "r5tpV2Ubv0XVE6y8V1IY8VdqcrF3";
        return adminUid.equals(currentUserId);
    }
}