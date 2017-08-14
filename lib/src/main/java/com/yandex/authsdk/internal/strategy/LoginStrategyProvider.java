package com.yandex.authsdk.internal.strategy;

import android.content.Context;
import android.support.annotation.NonNull;

import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.internal.FingerprintExtractor;

public class LoginStrategyProvider {

    @NonNull
    public LoginStrategy getLoginStrategy(@NonNull final Context context, @NonNull final YandexAuthOptions options) {
        LoginStrategy strategy = NativeLoginStrategy.getIfPossible(options, context.getPackageManager(), new FingerprintExtractor());
        if (strategy != null) {
            return strategy;
        }

        strategy = BrowserLoginStrategy.getIfPossible(context, context.getPackageManager());
        if (strategy != null) {
            return strategy;
        }

        return WebViewLoginStrategy.get();
    }

    @NonNull
    public LoginStrategy.ResultExtractor getResultExtractor(@NonNull final LoginType type) {
        switch (type) {
            case NATIVE:
                return new NativeLoginStrategy.ResultExtractor();
            case BROWSER:
                return new BrowserLoginStrategy.ResultExtractor();
            case WEBVIEW:
                return new WebViewLoginStrategy.ResultExtractor();
            default:
                throw new IllegalArgumentException("Unknown login type: " + type);
        }
    }
}