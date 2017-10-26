package com.yandex.authsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;

import java.util.ArrayList;

import static com.yandex.authsdk.internal.Constants.EXTRA_CLIENT_ID;
import static com.yandex.authsdk.internal.Constants.EXTRA_OPTIONS;
import static com.yandex.authsdk.internal.Constants.EXTRA_SCOPES;
import static com.yandex.authsdk.internal.Constants.EXTRA_UID_VALUE;

public abstract class LoginStrategy {

    public abstract void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final ArrayList<String> scopes,
            @Nullable final Long uid
    );

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final ArrayList<String> scopes,
            @NonNull final String clientId,
            @Nullable final Long uid
    ) {
        intent.putExtra(EXTRA_SCOPES, scopes);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        if (uid != null) {
            intent.putExtra(EXTRA_UID_VALUE, uid);
        }
        return intent;
    }

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final ArrayList<String> scopes,
            @NonNull final YandexAuthOptions options,
            @Nullable final Long uid
    ) {
        intent.putExtra(EXTRA_SCOPES, scopes);
        intent.putExtra(EXTRA_OPTIONS, options);
        if (uid != null) {
            intent.putExtra(EXTRA_UID_VALUE, uid);
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
