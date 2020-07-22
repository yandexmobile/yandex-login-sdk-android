package com.yandex.authsdk.internal.strategy;

import android.content.Context;
import androidx.annotation.NonNull;

import com.yandex.authsdk.internal.PackageManagerHelper;

public class LoginStrategyResolver {

    @NonNull
    private final PackageManagerHelper packageManagerHelper;

    @NonNull
    private final Context context;

    public LoginStrategyResolver(@NonNull final Context context, @NonNull final PackageManagerHelper packageManagerHelper) {
        this.context = context;
        this.packageManagerHelper = packageManagerHelper;
    }

    @NonNull
    public LoginStrategy getLoginStrategy() {
        LoginStrategy strategy = NativeLoginStrategy.getIfPossible(packageManagerHelper);
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
