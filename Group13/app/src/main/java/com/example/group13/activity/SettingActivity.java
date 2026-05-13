package com.example.group13.activity;

import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;

public class SettingActivity extends BaseActivity {

    ImageButton btn_back;
    ImageView imgAvatar;
    TextView tvUserName, tvEmail, tvUserCode;
    LinearLayout btn_profile, btn_noti, btn_changepass, btn_language,
            btn_deleteAccount, btn_logout;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupNavigation();
        findViewsByIds();
        setOnClickListeners();
        loadAccountInfo();
    }
    private void loadAccountInfo() {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        tvEmail.setText(user.getEmail());

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) return;

                    String name = documentSnapshot.getString("employeeName");
                    String code = documentSnapshot.getString("employeeId");
                    String avatarUrl = documentSnapshot.getString("avatarUrl");

                    if (name != null)
                        tvUserName.setText(name);

                    if (code != null)
                        tvUserCode.setText("ID: " + code);

                    if (avatarUrl != null && !avatarUrl.isEmpty()) {

                        Glide.with(SettingActivity.this)
                                .load(avatarUrl)
                                .placeholder(R.drawable.pic_placeholder)
                                .error(R.drawable.pic_placeholder)
                                .circleCrop()
                                .into(imgAvatar);

                    } else {
                        // Không có avatar → dùng mặc định
                        Glide.with(SettingActivity.this)
                                .load(R.drawable.pic_placeholder)
                                .circleCrop()
                                .into(imgAvatar);
                    }
                });
    }

    private void findViewsByIds() {
        btn_back = findViewById(R.id.imageButtonBack);
        btn_profile = findViewById(R.id.btnProfile);
        btn_noti = findViewById(R.id.btnNotifications);
        btn_changepass = findViewById(R.id.btnChangePassword);
        btn_language = findViewById(R.id.btnLanguage);
        btn_deleteAccount = findViewById(R.id.btnDeleteAccount);
        btn_logout = findViewById(R.id.btnLogout);

        imgAvatar = findViewById(R.id.imgAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvEmail = findViewById(R.id.tvEmail);
        tvUserCode = findViewById(R.id.tvUserCode);
    }

    private void setOnClickListeners() {

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btn_profile.setOnClickListener(view -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) return;

            Intent intent = new Intent(SettingActivity.this, EmployeeAddEditActivity.class);
            intent.putExtra("IS_EDIT", true);
            intent.putExtra("EMPLOYEE_ID", user.getUid());
            startActivity(intent);
        });

        btn_noti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, SettingNotificationActivity.class);
                startActivity(intent);
            }
        });

        btn_changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, SettingChangepasswordActivity.class);
                startActivity(intent);
            }
        });

        btn_language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, SettingLanguageActivity.class);
                startActivity(intent);
            }
        });

        btn_deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SettingActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_deleteaccount, null);
                builder.setView(dialogView);

                android.app.AlertDialog dialog = builder.create();
                dialog.show();

                View btnConfirm = dialogView.findViewById(R.id.btnYes);
                View btnCancel = dialogView.findViewById(R.id.btnNo);

                if (btnConfirm != null) {
                    btnConfirm.setOnClickListener(v -> {

                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            dialog.dismiss();
                            return;
                        }

                        String uid = user.getUid();

                        // 1. Xóa dữ liệu Firestore (users/{uid})
                        db.collection("users").document(uid)
                                .delete()
                                .addOnSuccessListener(unused -> {

                                    // 2. Xóa Firebase Auth
                                    user.delete()
                                            .addOnSuccessListener(unused2 -> {

                                                Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);

                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            SettingActivity.this,
                                                            "Failed to delete auth account",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );

                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                SettingActivity.this,
                                                "Failed to delete user data",
                                                Toast.LENGTH_SHORT
                                        ).show()
                                );

                        dialog.dismiss();
                    });

                }

                if (btnCancel != null) {
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SettingActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout, null);
                builder.setView(dialogView);

                android.app.AlertDialog dialog = builder.create();
                dialog.show();

                View btnYes = dialogView.findViewById(R.id.btnYes);
                View btnNo = dialogView.findViewById(R.id.btnNo);

                if (btnYes != null) {
                    btnYes.setOnClickListener(v -> {
                        auth.signOut();

                        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        dialog.dismiss();
                    });

                }

                if (btnNo != null) {
                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

    }
}