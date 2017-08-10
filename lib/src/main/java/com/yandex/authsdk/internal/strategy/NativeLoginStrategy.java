package com.yandex.authsdk.internal.strategy;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.internal.FingerprintExtractor;
import com.yandex.authsdk.internal.AuthSdkActivity;

import java.util.ArrayList;
import java.util.List;

import static com.yandex.authsdk.YandexAuthException.CONNECTION_ERROR;

class NativeLoginStrategy extends LoginStrategy {

    // fingerprint of released app with AM
    static final String FINGERPRINT = "5D224274D9377C35DA777AD934C65C8CCA6E7A20";
    static final String ACTION_YA_SDK_LOGIN = "com.yandex.auth.action.YA_SDK_LOGIN";
    static final String META_SDK_VERSION = "com.yandex.auth.LOGIN_SDK_VERSION";
    static final String META_AM_VERSION = "com.yandex.auth.VERSION";
    static final String EXTRA_OAUTH_TOKEN = "com.yandex.auth.EXTRA_OAUTH_TOKEN";
    static final String EXTRA_OAUTH_TOKEN_TYPE = "com.yandex.auth.EXTRA_OAUTH_TOKEN_TYPE";
    static final String EXTRA_OAUTH_TOKEN_EXPIRES = "com.yandex.auth.OAUTH_TOKEN_EXPIRES";
    static final String OAUTH_TOKEN_ERROR = "com.yandex.auth.OAUTH_TOKEN_ERROR";
    static final String OAUTH_TOKEN_ERROR_MESSAGES = "com.yandex.auth.OAUTH_TOKEN_ERROR_MESSAGES";
    private static int VERSION = 1; // TODO move to gradle?

    /**
     * 1. Get all activities, that can handle "com.yandex.auth.action.YA_SDK_LOGIN" action<br>
     * 2. Check every activity if it suits requirements:<br>
     * 2.1 meta "com.yandex.auth.LOGIN_SDK_VERSION" in app manifest more or equal than current SDK version<br>
     * 2.2 app fingerprint matches known AM fingerprint<br>
     * 3. Return app, with max "com.yandex.auth.VERSION" meta.
     *
     * @param packageManager
     * @param fingerprintExtractor
     * @return LoginStrategy for native authorization or null
     */
    @Nullable
    static LoginStrategy getIfPossible(
            @NonNull final YandexAuthOptions options,
            @NonNull final PackageManager packageManager,
            @NonNull final FingerprintExtractor fingerprintExtractor
    ) {
        final Intent amSdkIntent = new Intent(ACTION_YA_SDK_LOGIN);
        final List<ResolveInfo> infos = packageManager.queryIntentActivities(amSdkIntent, PackageManager.MATCH_DEFAULT_ONLY);

        final ResolveInfo bestInfo = findBest(options, infos, packageManager, fingerprintExtractor);
        if (bestInfo != null) {
            final Intent intent = new Intent(ACTION_YA_SDK_LOGIN);
            intent.setPackage(bestInfo.activityInfo.packageName);
            return new NativeLoginStrategy(intent);
        }

        return null;
    }

    @Nullable
    static ResolveInfo findBest(
            @NonNull final YandexAuthOptions options,
            @NonNull final List<ResolveInfo> infos,
            @NonNull final PackageManager packageManager,
            @NonNull final FingerprintExtractor fingerprintExtractor
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
            final String[] fingerPrints = fingerprintExtractor.get(info.activityInfo.packageName, packageManager, options);
            if (fingerPrints == null) {
                // no fingerprints found
                continue;
            }
            for (final String fingerprint : fingerPrints) {
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

    private NativeLoginStrategy(@NonNull final Intent packagedIntent) {
        this.packagedIntent = packagedIntent;
    }

    @Override
    public void login(
            @NonNull final Activity activity,
            @NonNull final YandexAuthOptions options,
            @NonNull final ArrayList<String> scopes
    ) {
        final Intent intent = putExtras(packagedIntent, scopes, options.clientId());
        activity.startActivityForResult(intent, AuthSdkActivity.LOGIN_REQUEST_CODE);
    }

    @Override
    @NonNull
    public LoginType getType() {
        return LoginType.NATIVE;
    }

    static class ResultExtractor implements LoginStrategy.ResultExtractor {

        @Override
        @Nullable
        public YandexAuthToken tryExtractToken(@NonNull final Intent data) {
            final String token = data.getStringExtra(EXTRA_OAUTH_TOKEN);
            final long expiresIn = data.getLongExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 0);

            return token != null ? new YandexAuthToken(token, expiresIn)
                    : null;
        }

        @Override
        @Nullable
        public YandexAuthException tryExtractError(@NonNull final Intent data) {
            final boolean isError = data.getBooleanExtra(OAUTH_TOKEN_ERROR, false);
            if (!isError) {
                return null;
            }

            final String[] errorMessages = data.getStringArrayExtra(OAUTH_TOKEN_ERROR_MESSAGES);
            return errorMessages == null ? new YandexAuthException(CONNECTION_ERROR) : new YandexAuthException(errorMessages);
        }
    }
}
