package com.yandex.yaloginsdk.internal.strategy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.internal.ActivityStarter;

import java.util.ArrayList;
import java.util.Set;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CLIENT_ID;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CONFIG;

public abstract class LoginStrategy {

    static final String EXTRA_SCOPES = "com.yandex.auth.SCOPES";

    public abstract void login(
            @NonNull final ActivityStarter activityStarter,
            @NonNull final LoginSdkConfig config,
            @NonNull final Set<String> scopes
    );

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final Set<String> scopes,
            @NonNull final String clientId
    ) {
        intent.putExtra(EXTRA_SCOPES, new ArrayList<>(scopes));
        intent.putExtra(EXTRA_CLIENT_ID, clientId);
        return intent;
    }

    @NonNull
    static Intent putExtras(
            @NonNull final Intent intent,
            @NonNull final Set<String> scopes,
            @NonNull final LoginSdkConfig config
    ) {
        intent.putExtras(extras(scopes, config));
        return intent;
    }

    @NonNull
    static Bundle extras(@NonNull final Set<String> scopes, @NonNull final LoginSdkConfig config) {
        final Bundle bundle = new Bundle(2);
        bundle.putStringArrayList(EXTRA_SCOPES, new ArrayList<>(scopes));
        bundle.putParcelable(EXTRA_CONFIG, config);
        return bundle;
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
