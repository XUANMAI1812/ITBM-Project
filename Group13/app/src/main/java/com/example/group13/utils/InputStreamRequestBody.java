package com.example.group13.utils;

import android.content.ContentResolver;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class InputStreamRequestBody extends RequestBody {

    private final ContentResolver contentResolver;
    private final Uri uri;
    private final String contentType;

    public InputStreamRequestBody(ContentResolver contentResolver, Uri uri, String contentType) {
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.contentType = contentType;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        if (inputStream == null) return;

        try {
            sink.writeAll(Okio.source(inputStream));
        } finally {
            inputStream.close();
        }
    }
}
