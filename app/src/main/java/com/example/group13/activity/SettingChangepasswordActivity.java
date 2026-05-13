package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.AuthCredential;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;

public class SettingChangepasswordActivity extends BaseActivity {

    ImageButton btn_back;
    Button btn_save;
    MaterialButton btn_cancel;
    EditText currentPassword, newPassword, confirmPassword;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_changepassword);

        auth = FirebaseAuth.getInstance();

        setupNavigation();
        findViewsByIds();
        setOnClickListeners();
    }

    private void findViewsByIds() {
        currentPassword = findViewById(R.id.etCurrentPassword);
        newPassword = findViewById(R.id.etNewPassword);
        confirmPassword = findViewById(R.id.etConfirmPassword);
        btn_back = findViewById(R.id.imageButtonBack);
        btn_save = findViewById(R.id.btnSave);
        btn_cancel = findViewById(R.id.btnCancel);
    }

    private void setOnClickListeners() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingChangepasswordActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        btn_back.setOnClickListener(v -> {
            finish();
        });

        btn_cancel.setOnClickListener(v -> {
            finish();
        });

        btn_save.setOnClickListener(v -> {
            changePassword();
        });

    }

    private void changePassword() {

        String current = currentPassword.getText().toString().trim();
        String newPass = newPassword.getText().toString().trim();
        String confirm = confirmPassword.getText().toString().trim();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPass.equals(confirm)) {
            Toast.makeText(this, "Password confirmation does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        AuthCredential credential =
                EmailAuthProvider.getCredential(user.getEmail(), current);

        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> {

                    user.updatePassword(newPass)
                            .addOnSuccessListener(unused2 ->
                                    Toast.makeText(this,
                                            "Password updated successfully",
                                            Toast.LENGTH_SHORT
                                    ).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            e.getMessage(),
                                            Toast.LENGTH_SHORT
                                    ).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                );
    }

}
