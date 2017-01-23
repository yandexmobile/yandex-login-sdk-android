package com.yandex.yaloginsdk.strategy;

import android.content.Context;
import android.support.annotation.NonNull;

public class LoginStrategyProvider {

    @NonNull
    public LoginStrategy getLoginStrategy(@NonNull Context context) {
        LoginStrategy strategy = NativeLoginStrategy.getIfPossible(context);
        if (strategy != null) {
            return strategy;
        }

        return WebViewLoginStrategy.get();
    }

    @NonNull
    public LoginStrategy.ResultExtractor getResultExtractor(@NonNull LoginType type) {
        switch (type) {
            case NATIVE:
                return new NativeLoginStrategy.ResultExtractor();
            case WEB_VIEW:
                return new WebViewLoginStrategy.ResultExtractor();
            default:
                throw new IllegalArgumentException("Unknown login type: " + type);
        }
    }
}
