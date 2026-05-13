package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.CostListAdapter;
import com.example.group13.model.Project;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CostProjectListActivity extends AppCompatActivity {

    ImageButton btnBack;
    RecyclerView recyclerProject;
    EditText searchProject;

    ArrayList<Project> projectList = new ArrayList<>();
    ArrayList<Project> projectListFull = new ArrayList<>();

    CostListAdapter adapter;
    FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cost_project_list);

        btnBack = findViewById(R.id.btnBack);
        recyclerProject = findViewById(R.id.recyclerProject);
        searchProject = findViewById(R.id.searchProject);

        db = FirebaseFirestore.getInstance();

        adapter = new CostListAdapter(this, projectList, project -> {
            Intent intent = new Intent(
                    CostProjectListActivity.this,
                    CostActivity.class
            );

            intent.putExtra("project_doc_id", project.getProjectId());
            startActivity(intent);
        });

        recyclerProject.setLayoutManager(new LinearLayoutManager(this));
        recyclerProject.setAdapter(adapter);

        loadProjectsFromFirestore();
        setupSearch();

        btnBack.setOnClickListener(v -> finish());
    }
    private void loadProjectsFromFirestore() {
        db.collection("projects")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    projectList.clear();
                    projectListFull.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        Project project = doc.toObject(Project.class);
                        if (project != null) {
                            project.setProjectId(doc.getId());

                            projectList.add(project);
                            projectListFull.add(project);
                        }
                    }
                    adapter.updateList(projectList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Load failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
    private void setupSearch() {
        searchProject.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
        });
    }

    private void filter(String keyword) {
        ArrayList<Project> filtered = new ArrayList<>();
        for (Project p : projectListFull) {
            if (p.getName() != null &&
                    p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.updateList(filtered);
    }
}