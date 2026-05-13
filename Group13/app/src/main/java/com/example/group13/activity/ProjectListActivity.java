package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.R;
import com.example.group13.adapter.ProjectAdapter;
import com.example.group13.base.BaseActivity;
import com.example.group13.model.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectListActivity extends BaseActivity {

    ImageButton btnBack, btnAddProject;
    EditText searchProject;
    RecyclerView recyclerProject;

    FirebaseFirestore db;
    ProjectAdapter adapter;

    List<Project> projectList;
    List<Project> projectListFull;
    List<String> projectDocIds;
    List<String> projectDocIdsFull;

    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        setupNavigation();
        findViews();
        setupRecyclerView();
        loadProjects();
        setListeners();
    }

    private void findViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnAddProject = findViewById(R.id.btn_addproject);
        searchProject = findViewById(R.id.searchProject);
        recyclerProject = findViewById(R.id.recyclerProject);
    }

    private void setupRecyclerView() {
        projectList = new ArrayList<>();
        projectListFull = new ArrayList<>();
        projectDocIds = new ArrayList<>();
        projectDocIdsFull = new ArrayList<>();

        adapter = new ProjectAdapter(
                this,
                projectList,
                projectDocIds,
                currentUserId,
                isAdmin()
        );

        recyclerProject.setLayoutManager(new LinearLayoutManager(this));
        recyclerProject.setAdapter(adapter);
    }
    private void loadProjects() {
        projectList.clear();
        projectListFull.clear();
        projectDocIds.clear();
        projectDocIdsFull.clear();

        Map<String, Project> tempMap = new HashMap<>();

        db.collection("projects")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Project p = doc.toObject(Project.class);
                        if (p != null) {
                            boolean isManager = currentUserId.equals(p.getManagerId());
                            boolean isMember = p.getMemberIds() != null && p.getMemberIds().contains(currentUserId);
                            if (isManager || isMember) {
                                tempMap.put(doc.getId(), p);
                            }
                        }
                    }

                    for (Map.Entry<String, Project> entry : tempMap.entrySet()) {
                        String docId = entry.getKey();
                        Project p = entry.getValue();
                        projectList.add(p);
                        projectListFull.add(p);
                        projectDocIds.add(docId);
                        projectDocIdsFull.add(docId);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void setListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnAddProject.setOnClickListener(v -> {
            startActivity(
                    new Intent(this, ProjectAddEditActivity.class)
            );
        });

        searchProject.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProject(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterProject(String text) {
        projectList.clear();
        projectDocIds.clear();

        for (int i = 0; i < projectListFull.size(); i++) {
            Project p = projectListFull.get(i);
            String docId = projectDocIdsFull.get(i);

            if (p.getName() != null &&
                    p.getName().toLowerCase(Locale.ROOT)
                            .contains(text.toLowerCase(Locale.ROOT))) {

                projectList.add(p);
                projectDocIds.add(docId);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }
}