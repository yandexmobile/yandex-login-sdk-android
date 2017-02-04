package com.yandex.yaloginsdk.internal.strategy;

import android.content.Context;
import android.support.annotation.NonNull;

import com.yandex.yaloginsdk.internal.FingerprintExtractor;

public class LoginStrategyProvider {

    @NonNull
    public LoginStrategy getLoginStrategy(@NonNull Context context) {
        LoginStrategy strategy = NativeLoginStrategy.getIfPossible(context.getPackageManager(), new FingerprintExtractor());
        if (strategy != null) {
            return strategy;
        }

        return BrowserLoginStrategy.get(context);
    }

    @NonNull
    public LoginStrategy.ResultExtractor getResultExtractor(@NonNull LoginType type) {
        switch (type) {
            case NATIVE:
                return new NativeLoginStrategy.ResultExtractor();
            case BROWSER:
                return new BrowserLoginStrategy.ResultExtractor();
            default:
                throw new IllegalArgumentException("Unknown login type: " + type);
        }
    }
}
