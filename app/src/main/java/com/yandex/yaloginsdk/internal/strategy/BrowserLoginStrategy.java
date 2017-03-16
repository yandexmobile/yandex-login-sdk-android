package com.yandex.yaloginsdk.internal.strategy;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdk;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.internal.ActivityStarter;
import com.yandex.yaloginsdk.internal.BrowserLoginActivity;

import java.util.List;
import java.util.Set;

import static com.yandex.yaloginsdk.internal.BrowserLoginActivity.EXTRA_BROWSER_PACKAGE_NAME;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static com.yandex.yaloginsdk.internal.strategy.LoginType.BROWSER;

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

    @VisibleForTesting
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
            @NonNull final ActivityStarter activityStarter,
            @NonNull final LoginSdkConfig config,
            @NonNull final Set<String> scopes
    ) {
        final Intent loginIntent = new Intent(context, BrowserLoginActivity.class);
        loginIntent.putExtra(EXTRA_BROWSER_PACKAGE_NAME, browserPackageName);
        putExtras(loginIntent, scopes, config.clientId());

        activityStarter.startActivityForResult(loginIntent, YaLoginSdk.LOGIN_REQUEST_CODE);
    }

    @Override
    @NonNull
    public LoginType getType() {
        return BROWSER;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public Token tryExtractToken(@NonNull final Intent data) {
            return data.getParcelableExtra(EXTRA_TOKEN);
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull final Intent data) {
            return (YaLoginSdkError) data.getSerializableExtra(EXTRA_ERROR);
        }
    }
}
