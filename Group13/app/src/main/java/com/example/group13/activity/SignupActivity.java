package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.LinkMovementMethod;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    Button loginButton, signupButton;
    EditText emailEdt, passwordEdt, confirmPasswordEdt;
    TextView tvTos;
    boolean isPasswordVisible = false, isConfirmPasswordVisible = false;
    CheckBox checkToS;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_signup);

        findViewsByIds();
        setupPasswordToggle(passwordEdt, true);
        setupPasswordToggle(confirmPasswordEdt, false);
        setOnClickListeners();
    }

    private void findViewsByIds() {
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);
        confirmPasswordEdt = findViewById(R.id.passwordconfirm);
        checkToS = findViewById(R.id.checkToS);
        tvTos = findViewById(R.id.tvTos);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setOnClickListeners() {

        signupButton.setOnClickListener(v -> {

            String email = emailEdt.getText().toString().trim();
            String password = passwordEdt.getText().toString().trim();
            String confirmPassword = confirmPasswordEdt.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!checkToS.isChecked()) {
                Toast.makeText(this, "You must agree to Terms of Service", Toast.LENGTH_SHORT).show();
                return;
            }

            createFirebaseUser(email, password);
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tvTos.setOnClickListener(v ->
                startActivity(new Intent(this, TermsActivity.class))
        );
    }

    private void setupPasswordToggle(EditText editText, boolean isMainPassword) {

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableRight = 2;

                if (editText.getCompoundDrawables()[drawableRight] == null)
                    return false;

                if (event.getRawX() >= (editText.getRight()
                        - editText.getCompoundDrawables()[drawableRight].getBounds().width())) {

                    if (isMainPassword) {
                        togglePassword(editText);
                    } else {
                        toggleConfirmPassword(editText);
                    }
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePassword(EditText edt) {

        if (isPasswordVisible) {
            edt.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            edt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_eye,
                    0
            );
        } else {
            edt.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            edt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_eye_off,
                    0
            );
        }

        isPasswordVisible = !isPasswordVisible;
        edt.setSelection(edt.getText().length());
    }

    private void toggleConfirmPassword(EditText edt) {

        if (isConfirmPasswordVisible) {
            edt.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
            );
            edt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_eye,
                    0
            );
        } else {
            edt.setInputType(
                    InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            );
            edt.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_lock,
                    0,
                    R.drawable.ic_eye_off,
                    0
            );
        }

        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        edt.setSelection(edt.getText().length());
    }


    private void createFirebaseUser(String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("displayName", "");
                    user.put("profileCompleted", false);

                    db.collection("users")
                            .document(uid)
                            .set(user);

                    startActivity(new Intent(this, EmployeeAddEditActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("already in use")) {
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

}