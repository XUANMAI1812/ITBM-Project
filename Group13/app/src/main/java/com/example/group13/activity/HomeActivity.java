package com.example.group13.activity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.bumptech.glide.Glide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.model.Employee;
import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends BaseActivity {

    FloatingActionButton checkinout;
    ImageView attendanceovertime, attendanceleavereq, employeelist, salarylist, mytask, history, cost;
    ImageView userIcons;
    TextView tvUserName;
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

    }

    private void findViewsByIds() {
        checkinout = findViewById(R.id.btn_attendance);
        attendanceovertime = findViewById(R.id.img_attendance_overtime);
        attendanceleavereq = findViewById(R.id.img_attendance_leaverequest);
        employeelist = findViewById(R.id.img_employeelist);
        salarylist = findViewById(R.id.img_salarylist);
        mytask = findViewById(R.id.img_my_task);
        history = findViewById(R.id.img_history);
        cost = findViewById(R.id.img_cost);
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

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, AttendanceHistoryActivity.class);
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
}