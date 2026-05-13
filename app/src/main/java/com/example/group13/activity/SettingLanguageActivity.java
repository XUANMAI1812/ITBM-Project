package com.example.group13.activity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;
import com.example.group13.helper.LanguageHelper;

public class SettingLanguageActivity extends BaseActivity {

    ImageButton imageButtonBack;
    LinearLayout layoutVietnamese, layoutEnglish;
    RadioButton rbVietnamese, rbEnglish;
    TextView btnSave;

    String selectedLanguage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_language);

        findViews();
        loadCurrentLanguage();
        setupEvents();
    }

    private void findViews() {
        imageButtonBack = findViewById(R.id.imageButtonBack);
        layoutVietnamese = findViewById(R.id.layoutVietnamese);
        layoutEnglish = findViewById(R.id.layoutEnglish);
        rbVietnamese = findViewById(R.id.rbVietnamese);
        rbEnglish = findViewById(R.id.rbEnglish);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadCurrentLanguage() {
        selectedLanguage = LanguageHelper.getLanguage(this);
        updateRadioUI();
    }

    private void updateRadioUI() {
        rbVietnamese.setChecked("vi".equals(selectedLanguage));
        rbEnglish.setChecked("en".equals(selectedLanguage));
    }

    private void setupEvents() {

        imageButtonBack.setOnClickListener(v -> finish());

        layoutVietnamese.setOnClickListener(v -> selectVietnamese());
        rbVietnamese.setOnClickListener(v -> selectVietnamese());

        layoutEnglish.setOnClickListener(v -> selectEnglish());
        rbEnglish.setOnClickListener(v -> selectEnglish());

        btnSave.setOnClickListener(v -> {
            LanguageHelper.setLanguage(this, selectedLanguage);
            recreate(); // 🔥 reload Activity – KHÔNG về Home
        });
    }

    private void selectVietnamese() {
        selectedLanguage = "vi";
        rbVietnamese.setChecked(true);
        rbEnglish.setChecked(false);
    }

    private void selectEnglish() {
        selectedLanguage = "en";
        rbEnglish.setChecked(true);
        rbVietnamese.setChecked(false);
    }
}
