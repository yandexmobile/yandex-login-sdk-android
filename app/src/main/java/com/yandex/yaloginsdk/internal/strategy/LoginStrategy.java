package com.yandex.yaloginsdk.internal.strategy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.ArrayList;
import java.util.Set;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CLIENT_ID;

public abstract class LoginStrategy {

    static final String EXTRA_SCOPES = "com.yandex.auth.SCOPES";

    @NonNull
    public abstract Intent getLoginIntent(@NonNull final LoginSdkConfig config, @NonNull final Set<String> scopes);

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final Set<String> scopes,
            @NonNull final String clientId
    ) {
        intent.putStringArrayListExtra(EXTRA_SCOPES, new ArrayList<>(scopes));
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        return intent;
    }

    @NonNull
    public abstract LoginType getType();

    public interface ResultExtractor {

        @Nullable
        Token tryExtractToken(@NonNull final Intent data);

        @Nullable
        YaLoginSdkError tryExtractError(@NonNull final Intent data);
    }
}
