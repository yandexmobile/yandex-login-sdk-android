package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.yandex.yaloginsdk.Config;
import com.yandex.yaloginsdk.FingerprintExtractor;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkConstants;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.List;
import java.util.Set;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.ACTION_YA_SDK_LOGIN;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.FINGERPRINT;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.META_SDK_VERSION;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.VERSION;

class NativeLoginStrategy extends LoginStrategy {

    @Nullable
    static LoginStrategy getIfPossible(@NonNull PackageManager packageManager, @NonNull FingerprintExtractor fingerprintExtractor) {
        final Intent amSdkIntent = new Intent(ACTION_YA_SDK_LOGIN);
        final List<ResolveInfo> infos = packageManager.queryIntentActivities(amSdkIntent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo info : infos) {
            try {
                if (checkIsMatching(info, packageManager, fingerprintExtractor)) {
                    final Intent intent = new Intent(ACTION_YA_SDK_LOGIN);
                    intent.setPackage(info.activityInfo.packageName);
                    return new NativeLoginStrategy(intent);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        return null;
    }

    @VisibleForTesting
    static boolean checkIsMatching(
            @NonNull ResolveInfo info,
            @NonNull PackageManager packageManager,
            @NonNull FingerprintExtractor fingerprintExtractor
    ) throws PackageManager.NameNotFoundException {
        // filter by am sdk version
        final ApplicationInfo appInfo = packageManager.getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_META_DATA);
        final Bundle metadata = appInfo.metaData;
        if (metadata == null || VERSION != metadata.getInt(META_SDK_VERSION)) {
            return false;
        }

        // filter by am fingerprint
        final String[] fingerPrints = fingerprintExtractor.get(info.activityInfo.packageName, packageManager);
        if (fingerPrints == null) {
            return false;
        }
        for (String fingerprint : fingerPrints) {
            if (FINGERPRINT.equals(fingerprint)) {
                return true;
            }
        }

        return false;
    }

    @NonNull
    private final Intent packagedIntent;

    private NativeLoginStrategy(@NonNull Intent packagedIntent) {
        this.packagedIntent = packagedIntent;
    }

    @Override
    @NonNull
    public Intent getLoginIntent(@NonNull Config config, @NonNull Set<String> scopes) {
        return putExtras(packagedIntent, scopes, config.clientId());
    }

    @Override
    @NonNull
    public LoginType getType() {
        return LoginType.NATIVE;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public Token tryExtractToken(@NonNull Intent data) {
            final String token = data.getStringExtra(YaLoginSdkConstants.AmConstants.EXTRA_OAUTH_TOKEN);
            final String type = data.getStringExtra(YaLoginSdkConstants.AmConstants.EXTRA_OAUTH_TOKEN_TYPE);
            final double expiresIn = data.getDoubleExtra(YaLoginSdkConstants.AmConstants.EXTRA_OAUTH_TOKEN_EXPIRES, 0);
            return Token.create(token, type, expiresIn);
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull Intent data) {
            return null;
        }
    }
}
