package com.yandex.authsdk.internal

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.text.TextUtils
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.internal.strategy.NativeLoginStrategy.Companion.getActionIntent
import java.math.BigInteger
import java.security.NoSuchAlgorithmException

internal class PackageManagerHelper(
    private val myPackageName: String,
    private val packageManager: PackageManager,
    private val options: YandexAuthOptions,
) {

    fun findLatestApplication(): YandexApplicationInfo? {
        var latestApplicationInfo: YandexApplicationInfo? = null
        for (info in findLoginSdkApplications()) {
            if (latestApplicationInfo == null
                || info.amVersion > latestApplicationInfo.amVersion
                || info.amInternalVersion > latestApplicationInfo.amInternalVersion) {
                latestApplicationInfo = info
            }
        }
        return latestApplicationInfo
    }

    private fun findLoginSdkApplications(): List<YandexApplicationInfo> {
        val result: MutableList<YandexApplicationInfo> = mutableListOf()
        @SuppressLint("QueryPermissionsNeeded")
        val applicationInfos = packageManager.getInstalledApplications(PackageManager.GET_META_DATA.toLong())
        for (applicationInfo in applicationInfos) {
            if (TextUtils.equals(applicationInfo.packageName, myPackageName)) {
                continue
            }
            if (!ALLOWED_PACKAGES.contains(applicationInfo.packageName)) {
                continue
            }
            if (!applicationInfo.enabled) {
                continue
            }
            val metaData = applicationInfo.metaData
            val packageName = applicationInfo.packageName
            if (metaData == null) {
                continue
            }
            if (!metaData.containsKey(META_SDK_VERSION)) {
                continue
            }
            if (!metaData.containsKey(META_AM_VERSION)) {
                continue
            }
            val fingerprints = extractFingerprints(packageName) ?: continue
            if (!fingerprints.contains(YANDEX_FINGERPRINT)) {
                continue
            }
            if (!isActionActivityExist(packageManager, applicationInfo.packageName)) {
                continue
            }
            result.add(
                YandexApplicationInfo(
                    packageName,
                    metaData.getInt(META_SDK_VERSION),
                    metaData.getFloat(META_AM_VERSION),
                    metaData.getInt(META_AM_INTERNAL_VERSION, -1)
                )
            )
        }
        return result
    }

    private fun isActionActivityExist(
        packageManager: PackageManager,
        packageName: String
    ): Boolean {
        val intent = getActionIntent(packageName)
        val resolveInfoList = packageManager.queryIntentActivities(intent)
        return resolveInfoList.isNotEmpty()
    }

    private fun extractFingerprints(packageName: String): List<String>? {
        return try {
            val signatures = getApplicationSignatureDigest(packageManager, packageName)
            signatures.map { toHex(it) }
        } catch (e: PackageManager.NameNotFoundException) {
            Logger.e(options, TAG, "Error getting fingerprint", e)
            null
        } catch (e: NoSuchAlgorithmException) {
            Logger.e(options, TAG, "Error getting fingerprint", e)
            null
        }
    }

    class YandexApplicationInfo(
        val packageName: String,
        val loginSdkVersion: Int,
        val amVersion: Float,
        amInternalVersion: Int
    ) {
        val amInternalVersion: Float

        init {
            this.amInternalVersion = amInternalVersion.toFloat()
        }
    }

    companion object {

        private val TAG = PackageManagerHelper::class.java.simpleName

        // fingerprint of released app with AM
        const val YANDEX_FINGERPRINT = "5D224274D9377C35DA777AD934C65C8CCA6E7A20"

        const val META_SDK_VERSION = "com.yandex.auth.LOGIN_SDK_VERSION"

        const val META_AM_VERSION = "com.yandex.auth.VERSION"

        const val META_AM_INTERNAL_VERSION = "com.yandex.auth.INTERNAL_VERSION"

        private fun toHex(bytes: ByteArray): String {
            val bi = BigInteger(1, bytes)
            return String.format("%0" + (bytes.size shl 1) + "X", bi)
        }

        val ALLOWED_PACKAGES = listOf(
            "com.yandex.browser",
            "ru.yandex.searchplugin",
            "com.yandex.searchapp",
            "ru.yandex.taxi",
            "ru.yandex.mail",
            "ru.yandex.disk",
            "com.yandex.bank",
            "ru.yandex.key",
            "ru.yandex.auth.client",
        )
    }
}
