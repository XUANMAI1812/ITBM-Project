package com.example.group13.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.group13.R;
import com.example.group13.model.Reminder;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class AddReminderDialog extends Dialog {

    EditText edtTitle, edtNote;
    TextView txtDate, txtTime;
    RadioButton rbLow, rbMedium, rbHigh;
    RadioGroup rgPriority;
    MaterialButton btnSave;

    long selectedMillis = -1;
    String priority = "Low";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Runnable onSavedCallback;

    public AddReminderDialog(Context context, Runnable onSavedCallback) {
        super(context);
        this.onSavedCallback = onSavedCallback;
        setContentView(R.layout.dialog_add_task);
        setCancelable(true);

        // Mở dialog full chiều ngang
        setOnShowListener(dialog -> {
            if (getWindow() != null) {
                getWindow().setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
        });

        initViews();
        setupEvents();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edtTitle);
        edtNote = findViewById(R.id.edtNote);
        txtDate = findViewById(R.id.txtStartDate);
        txtTime = findViewById(R.id.txtEndDate);

        rgPriority = findViewById(R.id.rgPriority);
        rbLow = findViewById(R.id.rbLow);
        rbMedium = findViewById(R.id.rbMedium);
        rbHigh = findViewById(R.id.rbHigh);

        btnSave = findViewById(R.id.btnSaveTask);

        // mặc định Low
        rbLow.setChecked(true);
        updatePriorityUI(rbLow);
    }

    private void setupEvents() {

        txtDate.setOnClickListener(v -> pickDate());
        txtTime.setOnClickListener(v -> pickTime());

        rgPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLow) {
                priority = "Low";
                updatePriorityUI(rbLow);
            } else if (checkedId == R.id.rbMedium) {
                priority = "Medium";
                updatePriorityUI(rbMedium);
            } else if (checkedId == R.id.rbHigh) {
                priority = "High";
                updatePriorityUI(rbHigh);
            }
        });

        btnSave.setOnClickListener(v -> saveReminder());
    }

    // HIGHLIGHT PRIORITY ĐƯỢC CHỌN
    private void updatePriorityUI(RadioButton selected) {

        // reset
        rbLow.setTypeface(null, Typeface.NORMAL);
        rbMedium.setTypeface(null, Typeface.NORMAL);
        rbHigh.setTypeface(null, Typeface.NORMAL);

        rbLow.setAlpha(0.5f);
        rbMedium.setAlpha(0.5f);
        rbHigh.setAlpha(0.5f);

        // highlight
        selected.setTypeface(null, Typeface.BOLD);
        selected.setAlpha(1f);
    }

    private void pickDate() {
        Calendar cal = Calendar.getInstance();

        new DatePickerDialog(getContext(),
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    selectedMillis = cal.getTimeInMillis();
                    txtDate.setText(day + "/" + (month + 1) + "/" + year);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void pickTime() {
        if (selectedMillis == -1) {
            Toast.makeText(getContext(),
                    "Please select date first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedMillis);

        new TimePickerDialog(getContext(),
                (view, hour, minute) -> {
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    selectedMillis = cal.getTimeInMillis();
                    txtTime.setText(String.format("%02d:%02d", hour, minute));
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void saveReminder() {
        String title = edtTitle.getText().toString().trim();
        String note = edtNote.getText().toString().trim();

        if (title.isEmpty() || selectedMillis == -1) {
            Toast.makeText(getContext(),
                    "Please fill title & deadline",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(),
                    "User not logged in",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Reminder reminder = new Reminder(title, note, selectedMillis, priority, uid);

        db.collection("reminders")
                .add(reminder)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                    dismiss();
                    if (onSavedCallback != null) onSavedCallback.run();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Save failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}