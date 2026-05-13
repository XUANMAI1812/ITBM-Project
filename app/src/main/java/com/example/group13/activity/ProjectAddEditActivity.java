package com.example.group13.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.model.Project;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.cloudinary.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import okhttp3.*;

public class ProjectAddEditActivity extends AppCompatActivity {

    EditText etProjectID, etName, etDescription, etProjectCost;
    AutoCompleteTextView autoManager, autoStatus;
    MultiAutoCompleteTextView autoTeamMembers;
    TextView txtStartDate, txtEndDate, txtFileName;
    LinearLayout cvUploadDocument, layoutSelectedFile;
    ImageButton btnBack, btnRemoveFile;
    Button btnSave;
    MaterialButton btnCancel;

    FirebaseFirestore db;

    boolean isEdit = false;
    String projectDocId;
    Uri selectedFileUri;
    String existingDocumentUrl;
    boolean fileUploaded = false;

    List<String> managerNames = new ArrayList<>();
    List<String> memberNames = new ArrayList<>();
    Map<String, String> nameToId = new HashMap<>();
    Map<String, String> idToName = new HashMap<>();

    static final int PICK_FILE_REQUEST = 1001;
    static final String CLOUDINARY_UPLOAD_URL = "https://api.cloudinary.com/v1_1/dx6lnyyi1/upload";
    static final String CLOUDINARY_PRESET = "android_project_file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_add_edit);

        db = FirebaseFirestore.getInstance();
        bindViews();
        setupStatus();
        setupDatePicker();
        loadEmployees(() -> handleIntent());
        setListeners();
    }

    void bindViews() {
        btnBack = findViewById(R.id.imageButtonBack);
        etProjectID = findViewById(R.id.etProjectID);
        etName = findViewById(R.id.etName);
        autoManager = findViewById(R.id.autoManager);
        autoTeamMembers = findViewById(R.id.autoTeamMembers);
        etDescription = findViewById(R.id.etDescription);
        etProjectCost = findViewById(R.id.etProjectcost);
        autoStatus = findViewById(R.id.autoStatus);
        txtStartDate = findViewById(R.id.txtStartDate);
        txtEndDate = findViewById(R.id.txtEndDate);
        cvUploadDocument = findViewById(R.id.cvUploadDocument);
        layoutSelectedFile = findViewById(R.id.layoutSelectedFile);
        txtFileName = findViewById(R.id.txtFileName);
        btnRemoveFile = findViewById(R.id.btnRemoveFile);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    void setupStatus() {
        autoStatus.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"Planning", "In Progress", "Completed", "On Hold"}
        ));
    }

    void setupDatePicker() {
        txtStartDate.setOnClickListener(v -> pickDate(txtStartDate));
        txtEndDate.setOnClickListener(v -> pickDate(txtEndDate));
    }

    void pickDate(TextView target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (v, y, m, d) -> target.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    void loadEmployees(Runnable onLoaded) {
        db.collection("users")
                .whereEqualTo("profileCompleted", true)
                .get()
                .addOnSuccessListener(qs -> {
                    managerNames.clear();
                    memberNames.clear();
                    nameToId.clear();
                    idToName.clear();

                    for (var d : qs) {
                        String name = d.getString("employeeName");
                        if (name != null) {
                            managerNames.add(name);
                            memberNames.add(name);
                            nameToId.put(name, d.getId());
                            idToName.put(d.getId(), name);
                        }
                    }

                    ArrayAdapter<String> managerAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            managerNames
                    );
                    autoManager.setAdapter(managerAdapter);

                    ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_dropdown_item_1line,
                            memberNames
                    );
                    autoTeamMembers.setAdapter(memberAdapter);
                    autoTeamMembers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

                    if (onLoaded != null) onLoaded.run();
                });
    }

    void handleIntent() {
        if (getIntent().hasExtra("projectId")) {
            isEdit = true;
            projectDocId = getIntent().getStringExtra("projectId");
            loadProject();
        }
    }

    void loadProject() {
        db.collection("projects").document(projectDocId).get()
                .addOnSuccessListener(d -> {
                    etProjectID.setText(d.getString("projectId"));
                    etName.setText(d.getString("name"));
                    etDescription.setText(d.getString("description"));
                    Double costValue = d.getDouble("cost");
                    etProjectCost.setText(costValue != null ? String.valueOf(costValue) : "");
                    autoStatus.setText(d.getString("status"), false);
                    txtStartDate.setText(d.getString("startDate"));
                    txtEndDate.setText(d.getString("endDate"));

                    String managerId = d.getString("managerId");
                    if (managerId != null && idToName.containsKey(managerId)) {
                        String managerName = idToName.get(managerId);
                        autoManager.setText(managerName, false);

                        List<String> filteredMembers = new ArrayList<>(memberNames);
                        filteredMembers.remove(managerName);
                        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                filteredMembers
                        );
                        autoTeamMembers.setAdapter(memberAdapter);
                        autoTeamMembers.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                    }

                    List<String> memberIds = (List<String>) d.get("memberIds");
                    if (memberIds != null) {
                        List<String> names = new ArrayList<>();
                        for (String id : memberIds) {
                            if (!id.equals(managerId) && idToName.containsKey(id)) {
                                names.add(idToName.get(id));
                            }
                        }
                        autoTeamMembers.setText(String.join(", ", names));
                    }

                    existingDocumentUrl = d.getString("documentUrl");
                    fileUploaded = existingDocumentUrl != null;
                    if (fileUploaded) layoutSelectedFile.setVisibility(View.VISIBLE);
                });
    }

    void setListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());
        cvUploadDocument.setOnClickListener(v -> pickFile());

        btnRemoveFile.setOnClickListener(v -> {
            selectedFileUri = null;
            existingDocumentUrl = null;
            fileUploaded = false;
            layoutSelectedFile.setVisibility(View.GONE);

            if (isEdit)
                db.collection("projects")
                        .document(projectDocId)
                        .update("documentUrl", FieldValue.delete());
        });
    }

    void pickFile() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri == null) {
                Toast.makeText(this, "File selection failed", Toast.LENGTH_SHORT).show();
                return;
            }

            String mimeType = getContentResolver().getType(fileUri);
            if (!"application/pdf".equals(mimeType) &&
                    !"application/msword".equals(mimeType) &&
                    !"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType)) {
                Toast.makeText(this, "Only PDF, DOC, DOCX files are allowed", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedFileUri = fileUri;
            try {
                uploadToCloudinary(selectedFileUri);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void uploadToCloudinary(Uri fileUri) {
        btnSave.setEnabled(false);
        try {
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) throw new IOException("InputStream is null");

            byte[] bytes = getBytesFromInputStream(inputStream);

            OkHttpClient client = new OkHttpClient();
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", getFileName(fileUri),
                            RequestBody.create(bytes, MediaType.parse("application/octet-stream")))
                    .addFormDataPart("upload_preset", CLOUDINARY_PRESET)
                    .build();

            Request request = new Request.Builder()
                    .url(CLOUDINARY_UPLOAD_URL)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(ProjectAddEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String bodyStr = response.body() != null ? response.body().string() : null;
                    if (!response.isSuccessful() || bodyStr == null) {
                        runOnUiThread(() -> {
                            Toast.makeText(ProjectAddEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            btnSave.setEnabled(true);
                        });
                        return;
                    }

                    try {
                        JSONObject res = new JSONObject(bodyStr);
                        existingDocumentUrl = res.optString("secure_url", null);
                        if (existingDocumentUrl == null) throw new Exception("No secure_url returned");

                        fileUploaded = true;
                        runOnUiThread(() -> {
                            txtFileName.setText(getFileName(fileUri));
                            layoutSelectedFile.setVisibility(View.VISIBLE);
                            btnSave.setEnabled(true);
                            Toast.makeText(ProjectAddEditActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(ProjectAddEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                            btnSave.setEnabled(true);
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(ProjectAddEditActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            });
        }
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    String getFileName(Uri uri) {
        String result = uri.getLastPathSegment();
        if (result != null && result.contains("/")) {
            result = result.substring(result.lastIndexOf("/") + 1);
        }
        return result;
    }

    private void pushProjectNotification(String projectName, boolean isUpdate) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("title", isUpdate ? "Project Updated" : "Project Created");
        notification.put("content", isUpdate
                ? "Project \"" + projectName + "\" has been updated"
                : "A new project \"" + projectName + "\" has been created");
        notification.put("timeMillis", System.currentTimeMillis());

        db.collection("users")
                .document(uid)
                .collection("projects")
                .add(notification);
    }

    void save() {
        if (!fileUploaded && selectedFileUri != null) {
            Toast.makeText(this, "Uploading file, please wait", Toast.LENGTH_SHORT).show();
            return;
        }

        String projectCostStr = etProjectCost.getText().toString().trim();
        double cost = 0;
        try {
            if (!projectCostStr.isEmpty()) cost = Double.parseDouble(projectCostStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid cost", Toast.LENGTH_SHORT).show();
            return;
        }

        String managerName = autoManager.getText().toString().trim();
        String managerId = nameToId.get(managerName);

        List<String> memberIds = new ArrayList<>();
        for (String s : autoTeamMembers.getText().toString().split(",")) {
            String name = s.trim();
            if (nameToId.containsKey(name) && !name.equals(managerName))
                memberIds.add(nameToId.get(name));
        }

        Project p = new Project(
                etProjectID.getText().toString(),
                etName.getText().toString(),
                managerId,
                etDescription.getText().toString(),
                memberIds,
                cost,
                autoStatus.getText().toString(),
                txtStartDate.getText().toString(),
                txtEndDate.getText().toString()
        );

        p.setDocumentUrl(existingDocumentUrl);

        if (isEdit) {
            db.collection("projects").document(projectDocId)
                    .set(p)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
                        pushProjectNotification(p.getName(), true);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("projects")
                    .add(p)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Project added", Toast.LENGTH_SHORT).show();
                        pushProjectNotification(p.getName(), false);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Add failed", Toast.LENGTH_SHORT).show());
        }
    }
}