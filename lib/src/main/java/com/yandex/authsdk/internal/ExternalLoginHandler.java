package com.yandex.authsdk.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static android.text.TextUtils.isEmpty;
import static com.yandex.authsdk.YandexAuthException.SECURITY_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_TOKEN;

class ExternalLoginHandler {

    private static final String STATE_KEY = "com.yandex.authsdk.internal.ExternalLoginHandler.STATE";

    private static final String TAG = BrowserLoginActivity.class.getSimpleName();

    private static final String LOGIN_URL_FORMAT = "https://oauth.yandex.ru/authorize" +
            "?response_type=token" +
            "&client_id=%s" +
            "&redirect_uri=%s" +
            "&state=%s" +
            "&force_confirm=true" +
            "&origin=yandex_auth_sdk_android";

    private static final String REDIRECT_URI_APPLINKS = "https://yx%s.oauth.yandex.ru/auth/finish?platform=android";

    private static final String REDIRECT_URI_SCHEME = "yx%s:///auth/finish?platform=android";

    private static final boolean SUPPORT_APPLINKS = Build.VERSION.SDK_INT >= 23;

    private static final String REDIRECT_URL = SUPPORT_APPLINKS ? REDIRECT_URI_APPLINKS : REDIRECT_URI_SCHEME;

    @Nullable
    String state;

    @NonNull
    private final YandexAuthOptions options;

    @NonNull
    private final StateGenerator stateGenerator;

    public ExternalLoginHandler(@NonNull final YandexAuthOptions options, @NonNull final StateGenerator stateGenerator) {
        this.options = options;
        this.stateGenerator = stateGenerator;
    }

    @NonNull
    String getUrl(@NonNull final String clientId) {
        state = stateGenerator.generate();
        try {
            final String redirectUri = URLEncoder.encode(String.format(REDIRECT_URL, clientId), "UTF-8");
            return String.format(LOGIN_URL_FORMAT, clientId, redirectUri, state);
        } catch (UnsupportedEncodingException e) {
            Logger.e(options, TAG, "No UTF-8 found", e);
            throw new RuntimeException(e);
        }
    }

    @NonNull
    Intent parseResult(@NonNull final Uri data) {
        final String fragment = data.getFragment();

        final Uri dummyUri = Uri.parse("dummy://dummy?" + fragment);

        final Intent result = new Intent();

        final String serverState = dummyUri.getQueryParameter("state");
        if (isEmpty(serverState) || !serverState.equals(state)) {
            result.putExtra(EXTRA_ERROR, new YandexAuthException(SECURITY_ERROR));
            return result;
        }

        final String error = dummyUri.getQueryParameter("error");
        if (error != null) {
            result.putExtra(EXTRA_ERROR, new YandexAuthException(error));
        } else {
            final String token = dummyUri.getQueryParameter("access_token");
            final long expiresIn = Long.parseLong(dummyUri.getQueryParameter("expires_in"));
            result.putExtra(EXTRA_TOKEN, new YandexAuthToken(token, expiresIn));
        }
        return result;
    }

    public boolean isFinalUrl(@NonNull final String url, @NonNull final String clientId) {
        return url.startsWith(String.format(REDIRECT_URL, clientId));
    }

    void saveState(@NonNull final Bundle outState) {
        outState.putString(STATE_KEY, state);
    }

    void restoreState(@NonNull final Bundle outState) {
        state = outState.getString(STATE_KEY);
    }

    interface StateGenerator {

        @NonNull
        String generate();
    }
}
