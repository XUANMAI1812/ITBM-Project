package com.example.group13.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LanguageHelper {

    private static final String PREFS_NAME = "app_prefs";
    private static final String KEY_LANGUAGE = "language";

    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public static Context applyLanguage(Context context) {
        String lang = getLanguage(context);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            context.getResources().updateConfiguration(
                    config,
                    context.getResources().getDisplayMetrics()
            );
            return context;
        }
    }
}
