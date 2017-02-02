package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.yandex.yaloginsdk.FingerprintExtractor;
import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import java.util.List;
import java.util.Set;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.ACTION_YA_SDK_LOGIN;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_OAUTH_TOKEN;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_OAUTH_TOKEN_EXPIRES;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_OAUTH_TOKEN_TYPE;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.FINGERPRINT;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.META_AM_VERSION;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.META_SDK_VERSION;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.OAUTH_TOKEN_ERROR;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.OAUTH_TOKEN_ERROR_MESSAGES;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.VERSION;
import static com.yandex.yaloginsdk.YaLoginSdkError.CONNECTION_ERROR;

class NativeLoginStrategy extends LoginStrategy {


    /**
     * 1. Get all activities, that can handle "com.yandex.auth.action.YA_SDK_LOGIN" action
     * 2. Check every activity if it suits requirements:
     *  * meta "com.yandex.auth.LOGIN_SDK_VERSION" in app manifest more or equal than current SDK version
     *  * app fingerprint matches known AM fingerprint
     * 3. Return first app, that suits.
     *
     * @param packageManager
     * @param fingerprintExtractor
     * @return LoginStrategy for native authorization or null
     */
    @Nullable
    static LoginStrategy getIfPossible(@NonNull PackageManager packageManager, @NonNull FingerprintExtractor fingerprintExtractor) {
        final Intent amSdkIntent = new Intent(ACTION_YA_SDK_LOGIN);
        final List<ResolveInfo> infos = packageManager.queryIntentActivities(amSdkIntent, PackageManager.MATCH_DEFAULT_ONLY);

        final ResolveInfo bestInfo = findBest(infos, packageManager, fingerprintExtractor);
        if (bestInfo != null) {
            final Intent intent = new Intent(ACTION_YA_SDK_LOGIN);
            intent.setPackage(bestInfo.activityInfo.packageName);
            return new NativeLoginStrategy(intent);
        }

        return null;
    }

    @VisibleForTesting
    @Nullable
    static ResolveInfo findBest(
            @NonNull List<ResolveInfo> infos,
            @NonNull PackageManager packageManager,
            @NonNull FingerprintExtractor fingerprintExtractor
    ) {
        float maxVersion = 0;
        ResolveInfo best = null;

        for (ResolveInfo info : infos) {
            final Bundle metadata;
            try {
                metadata = packageManager
                        .getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_META_DATA)
                        .metaData;
            } catch (PackageManager.NameNotFoundException ignored) {
                continue;
            }

            if (metadata == null || VERSION > metadata.getInt(META_SDK_VERSION)) {
                // not suitable SDK version
                continue;
            }

            // filter by am fingerprint
            final String[] fingerPrints = fingerprintExtractor.get(info.activityInfo.packageName, packageManager);
            if (fingerPrints == null) {
                // no fingerprints found
                continue;
            }
            for (String fingerprint : fingerPrints) {
                if (FINGERPRINT.equals(fingerprint)) {
                    // correct fingerprint, check for max AM version
                    final float amVersion = metadata.getFloat(META_AM_VERSION);
                    if (amVersion > maxVersion) {
                        maxVersion = amVersion;
                        best = info;
                    }
                }
            }
        }
        return best;
    }

    @NonNull
    private final Intent packagedIntent;

    private NativeLoginStrategy(@NonNull Intent packagedIntent) {
        this.packagedIntent = packagedIntent;
    }

    @Override
    @NonNull
    public Intent getLoginIntent(@NonNull LoginSdkConfig config, @NonNull Set<String> scopes) {
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
            final String token = data.getStringExtra(EXTRA_OAUTH_TOKEN);
            final String type = data.getStringExtra(EXTRA_OAUTH_TOKEN_TYPE);
            final long expiresIn = data.getLongExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 0);

            return token != null && type != null
                    ? Token.create(token, type, expiresIn)
                    : null;
        }

        @Override
        @Nullable
        public YaLoginSdkError tryExtractError(@NonNull Intent data) {
            final boolean isError = data.getBooleanExtra(OAUTH_TOKEN_ERROR, false);
            if (!isError) {
                return null;
            }

            final String[] errorMessages = data.getStringArrayExtra(OAUTH_TOKEN_ERROR_MESSAGES);
            return errorMessages == null ? new YaLoginSdkError(CONNECTION_ERROR) : new YaLoginSdkError(errorMessages);
        }
    }
}
