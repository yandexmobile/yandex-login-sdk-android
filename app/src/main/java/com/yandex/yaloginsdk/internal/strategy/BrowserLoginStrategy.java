package com.yandex.yaloginsdk.internal.strategy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.internal.BrowserLoginActivity;
import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.Set;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.internal.strategy.LoginType.BROWSER;

class BrowserLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get(@NonNull final Context context) {
        return new BrowserLoginStrategy(context);
    }

    @NonNull
    private final Context context;

    private BrowserLoginStrategy(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    @NonNull
    public Intent getLoginIntent(@NonNull final LoginSdkConfig config, @NonNull final Set<String> scopes) {
        final Intent loginIntent = new Intent(context, BrowserLoginActivity.class);
        putExtras(loginIntent, scopes, config.clientId());
        return loginIntent;
    }

    @Override
    @NonNull
    public LoginType getType() {
        return BROWSER;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public Token tryExtractToken(@NonNull final Intent data) {
            return data.getParcelableExtra(EXTRA_TOKEN);
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull final Intent data) {
            return (YaLoginSdkError) data.getSerializableExtra(EXTRA_ERROR);
        }
    }
}
