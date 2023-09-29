package com.yandex.authsdk.internal.strategy;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    public LoginStrategy getLoginStrategy(@Nullable LoginType preferredLoginType) {
        if (preferredLoginType != null) {
            switch (preferredLoginType) {
                case NATIVE: {
                    LoginStrategy strategy = NativeLoginStrategy.Companion.getIfPossible(packageManagerHelper);
                    if (strategy != null) {
                        Log.d("LoginStrategyResolver", "Native strategy");
                        return strategy;
                    }
                }
                case BROWSER: {
                    LoginStrategy strategy = BrowserLoginStrategy.Companion.getIfPossible(context, context.getPackageManager());
                    if (strategy != null) {
                        Log.d("LoginStrategyResolver", "Browser strategy");
                        return strategy;
                    }
                }
                default: {
                    return WebViewLoginStrategy.Companion.get();
                }
            }
        } else {
            LoginStrategy strategy = NativeLoginStrategy.Companion.getIfPossible(packageManagerHelper);
            if (strategy != null) {
                return strategy;
            }

            strategy = BrowserLoginStrategy.Companion.getIfPossible(context, context.getPackageManager());
            if (strategy != null) {
                return strategy;
            }

            return WebViewLoginStrategy.Companion.get();
        }
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
