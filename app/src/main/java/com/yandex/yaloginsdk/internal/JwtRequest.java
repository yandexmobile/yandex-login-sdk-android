package com.yandex.yaloginsdk.internal;

import android.os.Build;
import android.support.annotation.NonNull;

import com.yandex.yaloginsdk.YaLoginSdkError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JwtRequest {

    @NonNull
    private final String token;

    public JwtRequest(@NonNull String token) {
        this.token = token;
    }

    @NonNull
    public String get() throws IOException {
        disableConnectionReuseIfNecessary();

        final URL url = new URL("https://login.yandex.ru/info?format=jwt&oauth_token=" + token);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection.getResponseCode() == 401) {
            throw new YaLoginSdkError(YaLoginSdkError.JWT_AUTHORIZATION_ERROR);
        }
        try {
            return readToString(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    // TODO current minsdk in 15, remove if we will not decrease it
    private void disableConnectionReuseIfNecessary() {
        // Work around pre-Froyo bugs in HTTP connection reuse.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    @NonNull
    private static String readToString(@NonNull InputStream is) throws IOException {
        final BufferedReader r = new BufferedReader(new InputStreamReader(is));
        final StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }
}
