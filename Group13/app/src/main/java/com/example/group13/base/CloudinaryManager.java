package com.example.group13.base;

import android.content.Context;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {

    private static boolean initialized = false;

    public static void init(Context context) {
        if (initialized) return;

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dx6lnyyi1");
        config.put("api_key", "379529454374344");

        MediaManager.init(context, config);
        initialized = true;
    }
}