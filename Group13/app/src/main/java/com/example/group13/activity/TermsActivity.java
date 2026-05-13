package com.example.group13.activity;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group13.R;

public class TermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        WebView webView = findViewById(R.id.webView);
        ImageButton btnClose = findViewById(R.id.btnClose);

        webView.getSettings().setJavaScriptEnabled(false);
        webView.loadUrl("file:///android_asset/terms.html");

        btnClose.setOnClickListener(v -> finish());
    }
}