package com.yandex.yaloginsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.internal.LoginSdkActivity;
import com.yandex.yaloginsdk.internal.WebViewLoginActivity;

import java.util.ArrayList;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.internal.strategy.LoginType.WEBVIEW;

class WebViewLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get() {
        return new WebViewLoginStrategy();
    }

    @Override
    public void login(
            @NonNull final Activity activity,
            @NonNull final LoginSdkConfig config,
            @NonNull final ArrayList<String> scopes
    ) {
        final Intent loginIntent = new Intent(activity, WebViewLoginActivity.class);
        loginIntent.putExtras(extras(scopes, config));
        activity.startActivityForResult(loginIntent, LoginSdkActivity.LOGIN_REQUEST_CODE);
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
