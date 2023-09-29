package com.yandex.authsdk.internal;

import static android.text.TextUtils.isEmpty;
import static com.yandex.authsdk.YandexAuthException.SECURITY_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_OPTIONS;
import static com.yandex.authsdk.internal.Constants.EXTRA_TOKEN;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;

class ExternalLoginHandler {

    @NonNull
    private final PreferencesHelper preferencesHelper;

    @NonNull
    private final StateGenerator stateGenerator;

    @NonNull
    private final UrlCreator urlCreator;

    ExternalLoginHandler(
            @NonNull final PreferencesHelper preferencesHelper,
            @NonNull final StateGenerator stateGenerator,
            @NonNull final UrlCreator urlCreator
    ) {
        this.preferencesHelper = preferencesHelper;
        this.stateGenerator = stateGenerator;
        this.urlCreator = urlCreator;
    }

    @NonNull
    String getUrl(@NonNull final Intent intent) {
        final YandexAuthLoginOptions loginOptions = intent.getParcelableExtra(Constants.EXTRA_LOGIN_OPTIONS);
        final YandexAuthOptions options = intent.getParcelableExtra(EXTRA_OPTIONS);
        final String state = stateGenerator.generate();
        saveState(state);
        return urlCreator.getUrl(options, loginOptions, state);
    }

    @NonNull
    Intent parseResult(@NonNull final Uri data) {
        final String state = restoreState();
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
            final String expiresInString = dummyUri.getQueryParameter("expires_in");
            final long expiresIn;
            if (expiresInString == null) {
                expiresIn = Long.MAX_VALUE;
            } else {
                expiresIn = Long.parseLong(expiresInString);
            }
            result.putExtra(EXTRA_TOKEN, new YandexAuthToken(token, expiresIn));
        }
        return result;
    }

    boolean isFinalUrl(@NonNull final YandexAuthOptions options, @NonNull final String url) {
        return url.startsWith(urlCreator.createRedirectUrl(options));
    }

    private void saveState(@NonNull final String state) {
        preferencesHelper.saveStateValue(state);
    }

    @Nullable
    private String restoreState() {
        return preferencesHelper.restoreStateValue();
    }

    interface StateGenerator {

        @NonNull
        String generate();
    }
}
