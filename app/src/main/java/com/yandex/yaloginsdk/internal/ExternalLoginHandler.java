package com.yandex.yaloginsdk.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static android.text.TextUtils.isEmpty;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.YaLoginSdkError.SECURITY_ERROR;

class ExternalLoginHandler {

    private static final String STATE_KEY = "com.yandex.yaloginsdk.internal.ExternalLoginHandler.STATE";

    private static final String TAG = BrowserLoginActivity.class.getSimpleName();

    private static final String LOGIN_URL_FORMAT = "https://oauth.yandex.ru/authorize?response_type=token&client_id=%s&redirect_uri=%s&state=%s&force_confirm=true";

    private static final String REDIRECT_URI_APPLINKS = "https://yxfcdddf83a97843ae80815c1c9247015b.oauth.yandex.ru/auth/finish?platform=android";

    private static final String REDIRECT_URI_SCHEME = "yxfcdddf83a97843ae80815c1c9247015b:///auth/finish?platform=android";

    private static final boolean SUPPORT_APPLINKS = Build.VERSION.SDK_INT >= 23;

    @VisibleForTesting
    @Nullable
    String state;

    @NonNull
    private final StateGenerator stateGenerator;

    public ExternalLoginHandler(@NonNull StateGenerator stateGenerator) {
        this.stateGenerator = stateGenerator;
    }

    @NonNull
    String getUrl(@NonNull final String clientId) {
        state = stateGenerator.generate();
        try {
            final String redirectUri = URLEncoder.encode(SUPPORT_APPLINKS ? REDIRECT_URI_APPLINKS : REDIRECT_URI_SCHEME, "UTF-8");
            return String.format(LOGIN_URL_FORMAT, clientId, redirectUri, state);
        } catch (UnsupportedEncodingException e) {
            Logger.e(TAG, "No UTF-8 found", e);
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
            result.putExtra(EXTRA_ERROR, new YaLoginSdkError(SECURITY_ERROR));
            return result;
        }

        final String error = dummyUri.getQueryParameter("error");
        if (error != null) {
            result.putExtra(EXTRA_ERROR, new YaLoginSdkError(error));
        } else {
            final String token = dummyUri.getQueryParameter("access_token");
            final String type = dummyUri.getQueryParameter("token_type");
            final long expiresIn = Long.parseLong(dummyUri.getQueryParameter("expires_in"));
            result.putExtra(EXTRA_TOKEN, Token.create(token, type, expiresIn));
        }
        return result;
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
