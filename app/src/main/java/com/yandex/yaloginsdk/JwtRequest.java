package com.yandex.yaloginsdk;

import android.os.Build;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class JwtRequest {

    @NonNull
    private final String token;

    JwtRequest(@NonNull String token) {
        this.token = token;
    }

    @NonNull
    public String get() throws IOException {
        disableConnectionReuseIfNecessary();

        final URL url = new URL("https://login.yandex.ru/info?format=jwt&oauth_token=" + token);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            final InputStream is = new BufferedInputStream(connection.getInputStream());
            final BufferedReader r = new BufferedReader(new InputStreamReader(is));

            final StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            return total.toString();
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
}
