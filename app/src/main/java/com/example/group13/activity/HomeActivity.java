package com.example.group13.activity;

import com.example.group13.adapter.RecentProjectAdapter;
import com.example.group13.model.Project;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.group13.model.Employee;
import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    FloatingActionButton checkinout;
    ImageView attendanceovertime, attendanceleavereq, employeelist, salarylist, mytask, cost;
    ImageView userIcons;
    TextView tvUserName;
    RecyclerView recyclerRecentProjects;
    RecentProjectAdapter recentProjectAdapter;
    List<Project> recentProjects = new ArrayList<>();
    FirebaseAuth auth;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        tvUserName = findViewById(R.id.place_holder_name);

        setupNavigation();
        findViewsByIds();
        setOnClickListeners();

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        userIcons = findViewById(R.id.userIcons);

        loadUserProfile();
        loadRecentProjects();
    }

    private void findViewsByIds() {
        checkinout = findViewById(R.id.btn_attendance);
        attendanceovertime = findViewById(R.id.img_attendance_overtime);
        attendanceleavereq = findViewById(R.id.img_attendance_leaverequest);
        employeelist = findViewById(R.id.img_employeelist);
        salarylist = findViewById(R.id.img_salarylist);
        mytask = findViewById(R.id.img_my_task);
        cost = findViewById(R.id.img_cost);
        recyclerRecentProjects = findViewById(R.id.recyclerRecentProjects);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecentProjects.setLayoutManager(layoutManager);
        recentProjectAdapter = new RecentProjectAdapter(this, recentProjects);
        recyclerRecentProjects.setAdapter(recentProjectAdapter);
    }

    private void setOnClickListeners() {
        attendanceovertime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AttendanceOvertimeActivity.class);
                startActivity(intent);
            }
        });

        attendanceleavereq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AttendanceLeaverequestActivity.class);
                startActivity(intent);
            }
        });

        employeelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, EmployeeListActivity.class);
                startActivity(intent);
            }
        });

        salarylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SalaryListActivity.class);
                startActivity(intent);
            }
        });

        checkinout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AttendanceActivity.class);
                startActivity(intent);
            }
        });

        mytask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, ReminderActivity.class);
                startActivity(intent);
            }
        });

        cost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, CostProjectListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserProfile() {

        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) return;

                    Employee employee = documentSnapshot.toObject(Employee.class);
                    if (employee != null) {
                        setCurrentEmployee(employee);
                    }

                    String fullName = documentSnapshot.getString("employeeName");
                    if (fullName != null && !fullName.isEmpty()) {
                        tvUserName.setText(fullName);
                    }

                    String avatarUrl = documentSnapshot.getString("avatarUrl");

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        Glide.with(HomeActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.pic_placeholder)
                                .error(R.drawable.pic_placeholder)
                                .circleCrop()
                                .into(userIcons);
                    } else {
                        Glide.with(HomeActivity.this)
                                .load(R.drawable.pic_placeholder)
                                .circleCrop()
                                .into(userIcons);
                    }
                });
    }

    private void loadRecentProjects() {
        db.collection("projects")
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recentProjects.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Project project = doc.toObject(Project.class);
                        if (project != null) recentProjects.add(project);
                    }
                    recentProjectAdapter.notifyDataSetChanged();
                });
    }
}