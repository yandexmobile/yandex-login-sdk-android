package com.yandex.authsdk.internal.provider

import android.content.Context
import com.yandex.authsdk.internal.PackageManagerHelper

class ProviderClientResolver(private val packageManagerHelper: PackageManagerHelper) {

    fun createProviderClient(context: Context): ProviderClient? {
        val latestApplicationInfo = packageManagerHelper.findLatestApplication() ?: return null
        return if (latestApplicationInfo.loginSdkVersion >= 2) {
            ProviderClient(
                context,
                latestApplicationInfo.packageName,
                latestApplicationInfo.loginSdkVersion
            )
        } else {
            null
        }
    }
}
