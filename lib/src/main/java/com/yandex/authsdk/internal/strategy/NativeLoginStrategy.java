package com.yandex.authsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.internal.AuthSdkActivity;
import com.yandex.authsdk.internal.PackageManagerHelper;

import static com.yandex.authsdk.YandexAuthException.CONNECTION_ERROR;

public class NativeLoginStrategy extends LoginStrategy {

    static final String ACTION_YA_SDK_LOGIN = "com.yandex.auth.action.YA_SDK_LOGIN";
    static final String EXTRA_OAUTH_TOKEN = "com.yandex.auth.EXTRA_OAUTH_TOKEN";
    static final String EXTRA_OAUTH_TOKEN_TYPE = "com.yandex.auth.EXTRA_OAUTH_TOKEN_TYPE";
    static final String EXTRA_OAUTH_TOKEN_EXPIRES = "com.yandex.auth.OAUTH_TOKEN_EXPIRES";
    static final String OAUTH_TOKEN_ERROR = "com.yandex.auth.OAUTH_TOKEN_ERROR";
    static final String OAUTH_TOKEN_ERROR_MESSAGES = "com.yandex.auth.OAUTH_TOKEN_ERROR_MESSAGES";

    /**
     * 1. Get all activities, that can handle "com.yandex.auth.action.YA_SDK_LOGIN" action<br>
     * 2. Check every activity if it suits requirements:<br>
     * 2.1 meta "com.yandex.auth.LOGIN_SDK_VERSION" in app manifest more or equal than current SDK version<br>
     * 2.2 app fingerprint matches known AM fingerprint<br>
     * 3. Return app, with max "com.yandex.auth.VERSION" meta.
     *
     * @param packageManagerHelper
     * @return LoginStrategy for native authorization or null
     */
    @Nullable
    static LoginStrategy getIfPossible(
            @NonNull final PackageManagerHelper packageManagerHelper
    ) {
        final PackageManagerHelper.YandexApplicationInfo applicationInfo = packageManagerHelper.findLatestApplication();

        if (applicationInfo != null) {
            final Intent intent = getActionIntent(applicationInfo.packageName);
            return new NativeLoginStrategy(intent);
        }

        return null;
    }

    @NonNull
    public static Intent getActionIntent(@NonNull final String packageName) {
        final Intent intent = new Intent(ACTION_YA_SDK_LOGIN);
        intent.setPackage(packageName);
        return intent;
    }

    @NonNull
    private final Intent packagedIntent;

    private NativeLoginStrategy(@NonNull final Intent packagedIntent) {
        this.packagedIntent = packagedIntent;
    }

    @Override
    public void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final YandexAuthLoginOptions loginOptions
    ) {
        final Intent intent = putExtrasNative(packagedIntent, options, loginOptions);
        activity.startActivityForResult(intent, AuthSdkActivity.LOGIN_REQUEST_CODE);
    }

    @Override
    @NonNull
    public LoginType getType() {
        return LoginType.NATIVE;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public YandexAuthToken tryExtractToken(@NonNull final Intent data) {
            final String token = data.getStringExtra(EXTRA_OAUTH_TOKEN);
            final long expiresIn = data.getLongExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 0);

            return token != null ? new YandexAuthToken(token, expiresIn)
                    : null;
        }

        @Override
        @Nullable
        public YandexAuthException tryExtractError(@NonNull final Intent data) {
            final boolean isError = data.getBooleanExtra(OAUTH_TOKEN_ERROR, false);
            if (!isError) {
                return null;
            }

            final String[] errorMessages = data.getStringArrayExtra(OAUTH_TOKEN_ERROR_MESSAGES);
            return errorMessages == null ? new YandexAuthException(CONNECTION_ERROR) : new YandexAuthException(errorMessages);
        }
    }
}
