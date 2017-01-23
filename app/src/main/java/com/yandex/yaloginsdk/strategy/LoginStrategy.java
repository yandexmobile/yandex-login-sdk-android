package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.Config;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.ArrayList;
import java.util.Set;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.EXTRA_CLIENT_ID;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.EXTRA_SCOPES;

public abstract class LoginStrategy {

    @NonNull
    public abstract Intent getLoginIntent(@NonNull Config config, @NonNull Set<String> scopes);

    @NonNull
    static Intent putExtras(
            @NonNull Intent intent,
            @NonNull Set<String> scopes,
            @NonNull String clientId
    ) {
        intent.putStringArrayListExtra(EXTRA_SCOPES, new ArrayList<>(scopes));
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        return intent;
    }

    @NonNull
    public abstract LoginType getType();

    public interface ResultExtractor {

        @Nullable
        Token tryExtractToken(@NonNull Intent data);

        @Nullable
        YaLoginSdkError tryExtractError(@NonNull Intent data);
    }
}
