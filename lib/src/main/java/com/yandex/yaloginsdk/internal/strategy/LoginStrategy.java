package com.yandex.yaloginsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.YandexAuthException;
import com.yandex.yaloginsdk.YandexAuthOptions;
import com.yandex.yaloginsdk.YandexAuthToken;

import java.util.ArrayList;

import static com.yandex.yaloginsdk.internal.Constants.EXTRA_CLIENT_ID;
import static com.yandex.yaloginsdk.internal.Constants.EXTRA_OPTIONS;
import static com.yandex.yaloginsdk.internal.Constants.EXTRA_SCOPES;

public abstract class LoginStrategy {

    public abstract void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final ArrayList<String> scopes
    );

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final ArrayList<String> scopes,
            @NonNull final String clientId
    ) {
        intent.putExtra(EXTRA_SCOPES, scopes);
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        return intent;
    }

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final ArrayList<String> scopes,
            @NonNull final YandexAuthOptions options
    ) {
        intent.putExtra(EXTRA_SCOPES, scopes);
        intent.putExtra(EXTRA_OPTIONS, options);
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
