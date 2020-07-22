package com.yandex.authsdk.internal;

import androidx.annotation.NonNull;

import com.yandex.authsdk.YandexAuthException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JwtRequest {

    public static final String JWT_REQUEST_URL_FORMAT = "https://login.yandex.ru/info?format=jwt&oauth_token=%s";

    public static final int RESPONSE_CODE_UNAUTHORIZED = 401;

    @NonNull
    private final String token;

    public JwtRequest(@NonNull final String token) {
        this.token = token;
    }

    @NonNull
    public String get() throws IOException, YandexAuthException {
        final URL url = new URL(String.format(JWT_REQUEST_URL_FORMAT, token));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (connection.getResponseCode() == RESPONSE_CODE_UNAUTHORIZED) {
            throw new YandexAuthException(YandexAuthException.JWT_AUTHORIZATION_ERROR);
        }
        try {
            return readToString(connection.getInputStream());
        } finally {
            connection.disconnect();
        }
    }

    @NonNull
    private static String readToString(@NonNull final InputStream is) throws IOException {
        final BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try {
            final StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            return total.toString();
        } finally {
            r.close();
        }
    }
}
