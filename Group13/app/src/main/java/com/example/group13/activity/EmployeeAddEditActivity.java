package com.example.group13.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.cloudinary.android.callback.ErrorInfo;
import com.example.group13.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.group13.base.CloudinaryManager;

public class EmployeeAddEditActivity extends AppCompatActivity {

    // Views
    ImageButton btnBack;
    Button btnSave, btnCancel;
    LinearLayout layoutBirthday, layoutStartDate;
    EditText etEmployeeName, etPhone, etEmail;
    AutoCompleteTextView autoPosition, autoDepartment;
    TextView txtBirthday, txtStartDate;
    RadioButton rbMale, rbFemale;

    // Firebase
    FirebaseAuth auth;
    FirebaseFirestore db;

    ImageView imgAvatar, imgAvatarCamera;

    Uri avatarUri;
    String avatarUrl = null;
    String employeeId = null;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_add_edit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewsByIds();
        setupDropdowns();
        setOnClickListeners();

        CloudinaryManager.init(this);

        employeeId = getIntent().getStringExtra("EMPLOYEE_ID");
        isEditMode = getIntent().getBooleanExtra("IS_EDIT", false);

        if (isEditMode && employeeId != null) {
            loadEmployeeDetail(employeeId);
            btnSave.setText("Update");
        } else {
            isEditMode = false;
            clearForm();
            btnSave.setText("Create");
        }

    }

    private void findViewsByIds() {
        btnBack = findViewById(R.id.imageButtonBack);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        etEmployeeName = findViewById(R.id.etEmployeeName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);

        autoPosition = findViewById(R.id.autoPosition);
        autoDepartment = findViewById(R.id.autoDepartment);

        txtBirthday = findViewById(R.id.txtBirthday);
        txtStartDate = findViewById(R.id.txtStartDate);
        layoutBirthday = findViewById(R.id.layoutBirthday);
        layoutStartDate = findViewById(R.id.layoutStartDate);

        imgAvatarCamera = findViewById(R.id.imgAvatarCamera);
        imgAvatar = findViewById(R.id.imgAvatar);

        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
    }

    private final String[] POSITION_LIST = {
            "Engineering Manager",
            "Tech Lead",
            "Senior Software Engineer",
            "Software Engineer",
            "Junior Software Engineer",
            "Mobile Developer",
            "Backend Developer",
            "Frontend Developer",
            "Fullstack Developer",
            "QA Engineer",
            "DevOps Engineer",
            "UI/UX Designer",
            "Product Manager",
            "Business Analyst",
            "HR Executive",
            "Accountant"
    };

    private final String[] DEPARTMENT_LIST = {
            "Engineering",
            "Product",
            "Quality Assurance",
            "DevOps / Infrastructure",
            "UI/UX Design",
            "Human Resources",
            "Finance & Accounting",
            "Sales",
            "Marketing",
            "Customer Support"
    };

    private void setupDropdowns() {
        ArrayAdapter<String> positionAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, POSITION_LIST
        );
        autoPosition.setAdapter(positionAdapter);
        autoPosition.setThreshold(0);

        ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, DEPARTMENT_LIST
        );
        autoDepartment.setAdapter(departmentAdapter);
        autoDepartment.setThreshold(0);
    }

    private void setOnClickListeners() {

        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());

        autoPosition.setOnClickListener(v -> autoPosition.showDropDown());
        autoDepartment.setOnClickListener(v -> autoDepartment.showDropDown());

        layoutBirthday.setOnClickListener(v -> showDatePicker(txtBirthday));
        layoutStartDate.setOnClickListener(v -> showDatePicker(txtStartDate));

        imgAvatarCamera.setOnClickListener(v -> showAvatarPicker());

        btnSave.setOnClickListener(v -> saveEmployeeInfo());
    }

    private void showAvatarPicker() {
        String[] options = {"Take photo", "Choose from gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Select avatar")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                })
                .show();
    }

    ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            avatarUri = result.getData().getData();
                            previewAvatar(avatarUri);
                            uploadToCloudinary(avatarUri);
                        }
                    }
            );

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            if (bitmap == null) return;
                            if (extras == null || extras.get("data") == null) return;
                            avatarUri = ImageUtils.bitmapToUri(this, bitmap);
                            previewAvatar(avatarUri);
                            uploadToCloudinary(avatarUri);
                        }
                    }
            );

    public static class ImageUtils {
        public static Uri bitmapToUri(Context context, Bitmap bitmap) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

            String path = MediaStore.Images.Media.insertImage(
                    context.getContentResolver(),
                    bitmap,
                    "avatar" + System.currentTimeMillis(),
                    null
            );

            if (path == null) return null;
            return Uri.parse(path);
        }
    }


    private void previewAvatar(Uri uri) {
        Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(imgAvatar);
    }

    private void uploadToCloudinary(Uri uri) {

        MediaManager.get().upload(uri)
                .unsigned("android_avatar")
                .option("folder", "avatars")
                .callback(new com.cloudinary.android.callback.UploadCallback() {

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        avatarUrl = resultData.get("secure_url").toString();
                    }

                    @Override public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        Toast.makeText(EmployeeAddEditActivity.this,
                                "Upload avatar failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {}
                })
                .dispatch();
    }



    private void showDatePicker(TextView targetView) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, year, month, day) ->
                        targetView.setText(day + "/" + (month + 1) + "/" + year),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Giới hạn năm
        Calendar min = Calendar.getInstance();
        min.set(1960, 0, 1);

        Calendar max = Calendar.getInstance();
        max.set(Calendar.getInstance().get(Calendar.YEAR), 11, 31);

        dialog.getDatePicker().setMinDate(min.getTimeInMillis());
        dialog.getDatePicker().setMaxDate(max.getTimeInMillis());

        dialog.show();
    }

    private void generateEmployeeId(OnEmployeeIdGenerated callback) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String employeeId = "EMP-" + year + "-" + (int)(Math.random() * 900000 + 100000);
        callback.onSuccess(employeeId);
    }

    interface OnEmployeeIdGenerated {
        void onSuccess(String employeeId);
        void onFailure(Exception e);
    }

    private void loadEmployeeDetail(String employeeId) {
        db.collection("users")
                .document(employeeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etEmployeeName.setText(doc.getString("employeeName"));
                    etPhone.setText(doc.getString("phone"));
                    autoPosition.setText(doc.getString("position"), false);
                    autoDepartment.setText(doc.getString("department"), false);
                    txtBirthday.setText(doc.getString("dateOfBirth"));
                    txtStartDate.setText(doc.getString("startDate"));

                    String gender = doc.getString("gender");
                    if ("Male".equals(gender)) rbMale.setChecked(true);
                    else if ("Female".equals(gender)) rbFemale.setChecked(true);

                    avatarUrl = doc.getString("avatarUrl");
                    if (avatarUrl != null) {
                        Glide.with(this)
                                .load(avatarUrl)
                                .circleCrop()
                                .into(imgAvatar);
                    }

                    // đổi title cho ló khác :)))
                    btnSave.setText("Update");
                });
    }


    private void saveEmployeeInfo() {

        String employeeName = etEmployeeName.getText().toString().trim();
        String position = autoPosition.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String department = autoDepartment.getText().toString().trim();
        String birthday = txtBirthday.getText().toString();
        String startDate = txtStartDate.getText().toString();

        if (employeeName.isEmpty() || position.isEmpty() || phone.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!rbMale.isChecked() && !rbFemale.isChecked()) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (avatarUrl == null) {
            Toast.makeText(this, "Please select avatar", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> employee = new HashMap<>();
        employee.put("employeeName", employeeName);
        employee.put("position", position);
        employee.put("phone", phone);
        employee.put("department", department);
        employee.put("gender", rbMale.isChecked() ? "Male" : "Female");
        employee.put("dateOfBirth", birthday);
        employee.put("startDate", startDate);
        employee.put("avatarUrl", avatarUrl);
        employee.put("profileCompleted", true);

        String uid = isEditMode ? employeeId : auth.getCurrentUser().getUid();

        // edit
        if (isEditMode) {
            saveUser(uid, employee);
            NotificationHelper.pushNotification(
                    "New Employee",
                    "You have added employee " + employeeName
            );
            return;
        }

        // add
        employee.put("email",
                auth.getCurrentUser() != null
                        ? auth.getCurrentUser().getEmail()
                        : "");

        generateEmployeeId(new OnEmployeeIdGenerated() {
            @Override
            public void onSuccess(String empId) {
                employee.put("employeeId", empId);
                saveUser(uid, employee);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(
                        EmployeeAddEditActivity.this,
                        "Create employeeId failed",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }


    private void saveUser(String uid, Map<String, Object> employee) {
        if (isEditMode) {
            db.collection("users")
                    .document(uid)
                    .update(employee)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();

                        NotificationHelper.pushNotification(
                                "Employee Updated",
                                "Employee \"" + employee.get("employeeName") + "\" has been updated"
                        );

                        finish();
                    });
        } else {
            employee.put("uid", uid);
            db.collection("users")
                    .document(uid)
                    .set(employee)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Created", Toast.LENGTH_SHORT).show();

                        NotificationHelper.pushNotification(
                                "Employee Created",
                                "New employee \"" + employee.get("employeeName") + "\" has been added"
                        );

                        finish();
                    });
        }
    }

    private void clearForm() {
        etEmployeeName.setText("");
        etPhone.setText("");
        etEmail.setText("");
        autoPosition.setText("", false);
        autoDepartment.setText("", false);
        txtBirthday.setText("");
        txtStartDate.setText("");
        rbMale.setChecked(false);
        rbFemale.setChecked(false);
        avatarUrl = null;

        Glide.with(this)
                .load(R.drawable.pic_placeholder)
                .circleCrop()
                .into(imgAvatar);
    }

    public void onError(String requestId, ErrorInfo error) {
        Toast.makeText(
                EmployeeAddEditActivity.this,
                error.getDescription(),
                Toast.LENGTH_LONG
        ).show();
    }

}