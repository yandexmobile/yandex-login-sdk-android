package com.yandex.authsdk.internal.provider;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.internal.PackageManagerHelper;

public class ProviderClientResolver {

    @NonNull
    private final PackageManagerHelper packageManagerHelper;

    public ProviderClientResolver(@NonNull final PackageManagerHelper packageManagerHelper) {
        this.packageManagerHelper = packageManagerHelper;
    }

    @Nullable
    public ProviderClient createProviderClient(@NonNull final Context context) {
        final PackageManagerHelper.YandexApplicationInfo latestApplicationInfo = packageManagerHelper.findLatestApplication();
        if (latestApplicationInfo == null) {
            return null;
        }
        if (latestApplicationInfo.loginSdkVersion >= 2) {
            return new ProviderClient(context, latestApplicationInfo.packageName, latestApplicationInfo.loginSdkVersion);
        }
        return null;
    }
}
