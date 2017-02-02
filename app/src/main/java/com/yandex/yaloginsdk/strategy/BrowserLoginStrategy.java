package com.yandex.yaloginsdk.strategy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.BrowserActivity;
import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.Set;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.strategy.LoginType.BROWSER;

class BrowserLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get(@NonNull Context context) {
        return new BrowserLoginStrategy(context);
    }

    @NonNull
    private final Context context;

    private BrowserLoginStrategy(@NonNull Context context) {
        this.context = context;
    }

    @Override
    @NonNull
    public Intent getLoginIntent(@NonNull LoginSdkConfig config, @NonNull Set<String> scopes) {
        final Intent loginIntent = new Intent(context, BrowserActivity.class);
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
        public Token tryExtractToken(@NonNull Intent data) {
            return data.getParcelableExtra(EXTRA_TOKEN);
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull Intent data) {
            return (YaLoginSdkError) data.getSerializableExtra(EXTRA_ERROR);
        }
    }
}
