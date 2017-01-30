package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.WebViewActivity;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.Set;

class WebViewLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get() {
        return new WebViewLoginStrategy();
    }

    @Override
    @NonNull
    public Intent getLoginIntent(@NonNull LoginSdkConfig config, @NonNull Set<String> scopes) {
        final Intent loginIntent = new Intent(config.applicationContext(), WebViewActivity.class);
        putExtras(loginIntent, scopes, config.clientId());
        return loginIntent;
    }

    @Override
    @NonNull
    public LoginType getType() {
        return LoginType.WEB_VIEW;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public Token tryExtractToken(@NonNull Intent data) {
            return null;
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull Intent data) {
            return null;
        }
    }
}
