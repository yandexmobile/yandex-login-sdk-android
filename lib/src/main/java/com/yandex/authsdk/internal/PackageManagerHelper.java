package com.yandex.authsdk.internal;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.yandex.authsdk.YandexAuthOptions;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.getActionIntent;

public class PackageManagerHelper {

    private static final String TAG = PackageManagerHelper.class.getSimpleName();

    // fingerprint of released app with AM
    static final String YANDEX_FINGERPRINT = "5D224274D9377C35DA777AD934C65C8CCA6E7A20";

    static final String META_SDK_VERSION = "com.yandex.auth.LOGIN_SDK_VERSION";

    static final String META_AM_VERSION = "com.yandex.auth.VERSION";

    static final String META_AM_INTERNAL_VERSION = "com.yandex.auth.INTERNAL_VERSION";

    @NonNull
    private final PackageManager packageManager;

    @NonNull
    private final YandexAuthOptions options;

    @NonNull
    private final String myPackageName;

    public PackageManagerHelper(
            @NonNull final String myPackageName,
            @NonNull final PackageManager packageManager,
            @NonNull final YandexAuthOptions options
    ) {
        this.myPackageName = myPackageName;
        this.packageManager = packageManager;
        this.options = options;
    }

    @Nullable
    public YandexApplicationInfo findLatestApplication() {
        YandexApplicationInfo latestApplicationInfo = null;
        for (final YandexApplicationInfo info : findLoginSdkApplications()) {
            if (latestApplicationInfo == null
                    || info.amVersion > latestApplicationInfo.amVersion
                    || info.amInternalVersion > latestApplicationInfo.amInternalVersion) {
                latestApplicationInfo = info;
            }
        }
        return latestApplicationInfo;
    }

    @NonNull
    private List<YandexApplicationInfo> findLoginSdkApplications() {
        final List<YandexApplicationInfo> result = new ArrayList<>();
        final List<ApplicationInfo> applicationInfos = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (final ApplicationInfo applicationInfo : applicationInfos) {
            if (TextUtils.equals(applicationInfo.packageName, myPackageName)) {
                continue;
            }

            if (!applicationInfo.enabled) {
                continue;
            }

            final Bundle metaData = applicationInfo.metaData;
            final String packageName = applicationInfo.packageName;

            if (metaData == null) {
                continue;
            }
            if (!metaData.containsKey(META_SDK_VERSION)) {
                continue;
            }
            if (!metaData.containsKey(META_AM_VERSION)) {
                continue;
            }

            final List<String> fingerprints = extractFingerprints(packageName);
            if (fingerprints == null) {
                continue;
            }
            if (!fingerprints.contains(YANDEX_FINGERPRINT)) {
                continue;
            }

            if (!isActionActivityExist(packageManager, applicationInfo.packageName)) {
                continue;
            }

            result.add(new YandexApplicationInfo(
                    packageName,
                    metaData.getInt(META_SDK_VERSION),
                    metaData.getFloat(META_AM_VERSION),
                    metaData.getInt(META_AM_INTERNAL_VERSION, -1)
            ));
        }

        return result;
    }

    private boolean isActionActivityExist(@NonNull final PackageManager packageManager, @NonNull final String packageName) {
        final Intent intent = getActionIntent(packageName);
        final List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        return resolveInfoList.size() > 0;
    }

    @Nullable
    private List<String> extractFingerprints(@NonNull final String packageName) {
        try {
            @SuppressLint("PackageManagerGetSignatures") final PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            final List<String> result = new ArrayList<>(info.signatures.length);
            for (final Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                result.add(toHex(md.digest()));
            }
            return result;
        } catch (final PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Logger.e(options, TAG, "Error getting fingerprint", e);
            return null;
        }
    }

    @NonNull
    private static String toHex(@NonNull final byte[] bytes) {
        final BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public static class YandexApplicationInfo {

        @NonNull
        public final String packageName;

        public final int loginSdkVersion;

        public final float amVersion;

        public final float amInternalVersion;

        public YandexApplicationInfo(@NonNull final String packageName, final int loginSdkVersion, final float amVersion, final int amInternalVersion) {
            this.packageName = packageName;
            this.loginSdkVersion = loginSdkVersion;
            this.amVersion = amVersion;
            this.amInternalVersion = amInternalVersion;
        }
    }
}
