package com.yandex.authsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;

import static com.yandex.authsdk.internal.Constants.EXTRA_CLIENT_ID;
import static com.yandex.authsdk.internal.Constants.EXTRA_LOGIN_HINT;
import static com.yandex.authsdk.internal.Constants.EXTRA_LOGIN_OPTIONS;
import static com.yandex.authsdk.internal.Constants.EXTRA_OPTIONS;
import static com.yandex.authsdk.internal.Constants.EXTRA_SCOPES;
import static com.yandex.authsdk.internal.Constants.EXTRA_UID_VALUE;

public abstract class LoginStrategy {

    public abstract void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final YandexAuthLoginOptions loginOptions
    );

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final YandexAuthOptions options,
            @NonNull final YandexAuthLoginOptions loginOptions
    ) {
        intent.putExtra(EXTRA_OPTIONS, options);
        intent.putExtra(EXTRA_LOGIN_OPTIONS, loginOptions);
        return intent;
    }

    @NonNull
    static Intent putExtrasNative(
            @NonNull final Intent intent,
            @NonNull final YandexAuthOptions options,
            @NonNull final YandexAuthLoginOptions loginOptions
    ) {
        intent.putExtra(EXTRA_SCOPES, loginOptions.getScopes());
        intent.putExtra(EXTRA_CLIENT_ID, options.getClientId());
        if (loginOptions.getUid() != null) {
            intent.putExtra(EXTRA_UID_VALUE, loginOptions.getUid());
        }
        if (loginOptions.getLoginHint() != null) {
            intent.putExtra(EXTRA_LOGIN_HINT, loginOptions.getLoginHint());
        }
        return intent;
    }

    @NonNull
    public abstract LoginType getType();

    public interface ResultExtractor {

        @Nullable
        YandexAuthToken tryExtractToken(@NonNull final Intent data);

        @Nullable
        YandexAuthException tryExtractError(@NonNull final Intent data);
    }
}
