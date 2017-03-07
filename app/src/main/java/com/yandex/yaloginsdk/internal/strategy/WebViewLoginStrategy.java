package com.yandex.yaloginsdk.internal.strategy;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.internal.WebViewLoginActivity;

import java.util.Set;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.internal.strategy.LoginType.WEBVIEW;

class WebViewLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get(@NonNull Context context) {
        return new WebViewLoginStrategy(context);
    }

    @NonNull
    private final Context context;

    private WebViewLoginStrategy(@NonNull Context context) {
        this.context = context;
    }

    @Override
    @NonNull
    public Intent getLoginIntent(@NonNull LoginSdkConfig config, @NonNull Set<String> scopes) {
        final Intent loginIntent = new Intent(context, WebViewLoginActivity.class);
        putExtras(loginIntent, scopes, config.clientId());
        return loginIntent;
    }

    @Override
    @NonNull
    public LoginType getType() {
        return WEBVIEW;
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
