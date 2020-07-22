package com.yandex.authsdk.internal.strategy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.internal.BrowserLoginActivity;
import com.yandex.authsdk.internal.AuthSdkActivity;

import java.util.List;

import static com.yandex.authsdk.internal.BrowserLoginActivity.EXTRA_BROWSER_PACKAGE_NAME;
import static com.yandex.authsdk.internal.Constants.EXTRA_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_TOKEN;
import static com.yandex.authsdk.internal.strategy.LoginType.BROWSER;

class BrowserLoginStrategy extends LoginStrategy {

    private static final String TEST_WEB_URI = "https://ya.ru";

    enum SupportedBrowser {

        YA_BRO(1, "com.yandex.browser"), CHROME(0, "com.android.chrome");

        private final int priority;

        @NonNull
        private final String packageName;

        SupportedBrowser(final int priority, @NonNull final String packageName) {
            this.priority = priority;
            this.packageName = packageName;
        }
    }

    @Nullable
    static LoginStrategy getIfPossible(@NonNull final Context context, @NonNull final PackageManager packageManager) {
        final Uri sampleUri = Uri.parse(TEST_WEB_URI);
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, sampleUri);
        final List<ResolveInfo> infos = packageManager.queryIntentActivities(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);

        final String bestPackageName = findBest(infos);
        if (bestPackageName != null) {
            return new BrowserLoginStrategy(context, bestPackageName);
        }

        return null;
    }

    @Nullable
    static String findBest(@NonNull final List<ResolveInfo> infos) {
        SupportedBrowser best = null;

        for (final ResolveInfo info : infos) {
            for (final SupportedBrowser current : SupportedBrowser.values()) {
                if (info.activityInfo.packageName.equals(current.packageName)) {
                    if (best == null || best.priority < current.priority) {
                        best = current;
                    }
                }
            }
        }
        return best != null ? best.packageName : null;
    }

    @NonNull
    private final Context context;

    @NonNull
    private final String browserPackageName;

    private BrowserLoginStrategy(@NonNull final Context context, @NonNull final String browserPackageName) {
        this.context = context;
        this.browserPackageName = browserPackageName;
    }

    @Override
    public void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final YandexAuthLoginOptions loginOptions
    ) {
        final Intent loginIntent = new Intent(context, BrowserLoginActivity.class);
        loginIntent.putExtra(EXTRA_BROWSER_PACKAGE_NAME, browserPackageName);
        putExtras(loginIntent, options, loginOptions);

        activity.startActivityForResult(loginIntent, AuthSdkActivity.LOGIN_REQUEST_CODE);
    }

    @Override
    @NonNull
    public LoginType getType() {
        return BROWSER;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public YandexAuthToken tryExtractToken(@NonNull final Intent data) {
            return data.getParcelableExtra(EXTRA_TOKEN);
        }

        @Override
        @Nullable
        public YandexAuthException tryExtractError(@NonNull final Intent data) {
            return (YandexAuthException) data.getSerializableExtra(EXTRA_ERROR);
        }
    }
}
