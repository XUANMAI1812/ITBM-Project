package com.example.group13.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.util.Patterns;

import com.example.group13.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText emailEdt, passwordEdt;
    boolean isPassVisible = false;
    TextView forgotPassword;
    Button loginButton, signupButton;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewsByIds();
        setOnClickListeners();

        passwordEdt.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableRight = 2;

                if (event.getRawX() >= (passwordEdt.getRight()
                        - passwordEdt.getCompoundDrawables()[drawableRight].getBounds().width())) {

                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });
    }

    private void findViewsByIds() {
        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);
        forgotPassword = findViewById(R.id.forgotPassword);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
    }

    private void setOnClickListeners() {

        loginButton.setOnClickListener(v -> {
            String email = emailEdt.getText().toString().trim();
            String password = passwordEdt.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            loginWithFirebase(email, password);
        });

        signupButton.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

        forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

    }

    private void loginWithFirebase(String email, String password) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    db.collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                if (!snapshot.exists()) {
                                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                Boolean profileCompleted = snapshot.getBoolean("profileCompleted");

                                if (profileCompleted != null && profileCompleted) {
                                    startActivity(new Intent(this, HomeActivity.class));
                                } else {
                                    startActivity(new Intent(this, EmployeeAddEditActivity.class));
                                }

                                finish();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showForgotPasswordDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);

        EditText edtEmail = dialogView.findViewById(R.id.edtResetEmail);

        builder.setPositiveButton("Send", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Email required");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Invalid email");
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this,
                                "Password reset email sent",
                                Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void togglePasswordVisibility() {

        if (isPassVisible) {
            passwordEdt.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            passwordEdt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_eye,
                    0
            );
        } else {
            passwordEdt.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            passwordEdt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_eye_off,
                    0
            );
        }

        isPassVisible = !isPassVisible;

        passwordEdt.setSelection(passwordEdt.getText().length());
    }


}