package com.example.group13.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;
import com.example.group13.base.BaseActivity;

public class SettingLanguageActivity extends BaseActivity {

    ImageButton btn_back;
    private LinearLayout layoutVietnamese, layoutEnglish;
    private RadioButton rbVietnamese, rbEnglish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_language);

        setupNavigation();
        findViewsByIds();
        setOnClickListeners();
    }

    private void findViewsByIds() {
        layoutVietnamese = findViewById(R.id.layoutVietnamese);
        layoutEnglish = findViewById(R.id.layoutEnglish);
        rbVietnamese = findViewById(R.id.rbVietnamese);
        rbEnglish = findViewById(R.id.rbEnglish);
        btn_back = findViewById(R.id.imageButtonBack);
    }

    private void setOnClickListeners() {

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingLanguageActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        layoutVietnamese.setOnClickListener(v -> {
            rbVietnamese.setChecked(true);
            rbEnglish.setChecked(false);
        });

        layoutEnglish.setOnClickListener(v -> {
            rbEnglish.setChecked(true);
            rbVietnamese.setChecked(false);
        });

        rbVietnamese.setOnClickListener(v -> {
            rbEnglish.setChecked(false);
        });

        rbEnglish.setOnClickListener(v -> {
            rbVietnamese.setChecked(false);
        });
    }
}