package com.yandex.authsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.internal.AuthSdkActivity;
import com.yandex.authsdk.internal.WebViewLoginActivity;

import java.util.ArrayList;

import static com.yandex.authsdk.internal.Constants.EXTRA_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_TOKEN;
import static com.yandex.authsdk.internal.strategy.LoginType.WEBVIEW;

class WebViewLoginStrategy extends LoginStrategy {

    @NonNull
    static LoginStrategy get() {
        return new WebViewLoginStrategy();
    }

    @Override
    public void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final ArrayList<String> scopes,
            @Nullable final Long uid,
            @Nullable final String loginHint
    ) {
        final Intent loginIntent = new Intent(activity, WebViewLoginActivity.class);
        putExtras(loginIntent, scopes, options, uid, loginHint);
        activity.startActivityForResult(loginIntent, AuthSdkActivity.LOGIN_REQUEST_CODE);
    }

    @Override
    @NonNull
    public LoginType getType() {
        return WEBVIEW;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public YandexAuthToken tryExtractToken(@NonNull Intent data) {
            return data.getParcelableExtra(EXTRA_TOKEN);
        }

        @Override
        @Nullable
        public YandexAuthException tryExtractError(@NonNull Intent data) {
            return (YandexAuthException) data.getSerializableExtra(EXTRA_ERROR);
        }
    }
}
