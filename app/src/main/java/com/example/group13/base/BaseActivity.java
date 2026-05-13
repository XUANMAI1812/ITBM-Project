package com.example.group13.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.activity.AttendanceHistoryActivity;
import com.example.group13.activity.HomeActivity;
import com.example.group13.activity.ProjectListActivity;
import com.example.group13.activity.SettingActivity;
import com.example.group13.activity.SettingLanguageActivity;
import com.example.group13.helper.LanguageHelper;
import com.example.group13.model.Employee;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        // Áp dụng ngôn ngữ cho toàn app
        super.attachBaseContext(LanguageHelper.applyLanguage(newBase));
    }

    protected LinearLayout NavHome, NavProject, NavAttendance, NavAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupNavigation() {
        NavHome = findViewById(R.id.navHome);
        NavProject = findViewById(R.id.navProject);
        NavAttendance = findViewById(R.id.navAttendance);
        NavAccount = findViewById(R.id.navAccount);

        if (NavHome != null) NavHome.setOnClickListener(navClick);
        if (NavProject != null) NavProject.setOnClickListener(navClick);
        if (NavAttendance != null) NavAttendance.setOnClickListener(navClick);
        if (NavAccount != null) NavAccount.setOnClickListener(navClick);
    }

    private final View.OnClickListener navClick = v -> {
        Class<?> target = null;

        int id = v.getId();
        if (id == R.id.navHome) target = HomeActivity.class;
        else if (id == R.id.navProject) target = ProjectListActivity.class;
        else if (id == R.id.navAttendance) target = AttendanceHistoryActivity.class;
        else if (id == R.id.navAccount) target = SettingActivity.class;

        if (target != null && !getClass().equals(target)) {
            startActivity(new Intent(this, target));
        }
    };

    protected static Employee currentEmployee;

    protected String getCurrentUserUid() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    protected Employee getCurrentEmployee() {
        return currentEmployee;
    }

    protected void setCurrentEmployee(Employee employee) {
        currentEmployee = employee;
    }

    protected boolean isAdmin() {
        return "r5tpV2Ubv0XVE6y8V1IY8VdqcrF3".equals(getCurrentUserUid());
    }

    protected boolean isHR() {
        if (currentEmployee == null) return false;

        String pos = currentEmployee.getPosition();
        if (pos == null) return false;

        return pos.trim().equalsIgnoreCase("HR Executive");
    }
    protected boolean canManageEmployee() {
        return isAdmin() || isHR();
    }
}
