package com.yandex.authsdk.internal

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.ApplicationInfoFlags
import android.content.pm.PackageManager.PackageInfoFlags
import android.content.pm.PackageManager.ResolveInfoFlags
import android.content.pm.ResolveInfo
import android.os.Build
import java.io.Serializable
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

@Suppress("DEPRECATION")
@Throws(PackageManager.NameNotFoundException::class)
internal fun PackageManager.getPackageInfo(packageName: String, flags: Long = 0L): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getPackageInfo(packageName, PackageInfoFlags.of(flags))
    } else {
        this.getPackageInfo(packageName, flags.toInt())
    }
}

@Suppress("DEPRECATION")
@Throws(PackageManager.NameNotFoundException::class)
fun PackageManager.getApplicationInfo(packageName: String, flags: Long = 0L): ApplicationInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getApplicationInfo(packageName, ApplicationInfoFlags.of(flags))
    } else {
        this.getApplicationInfo(packageName, flags.toInt())
    }
}

@Suppress("DEPRECATION")
fun PackageManager.queryIntentActivities(intent: Intent, flags: Long = 0L): List<ResolveInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.queryIntentActivities(intent, ResolveInfoFlags.of(flags))
    } else {
        this.queryIntentActivities(intent, flags.toInt())
    }
}

@Suppress("DEPRECATION")
fun PackageManager.resolveService(intent: Intent, flags: Long = 0L): ResolveInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.resolveService(intent, ResolveInfoFlags.of(flags))
    } else {
        this.resolveService(intent, flags.toInt())
    }
}

@Suppress("DEPRECATION")
@SuppressLint("QueryPermissionsNeeded") // we have a list of apps in manifest
fun PackageManager.getInstalledApplications(flags: Long): List<ApplicationInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getInstalledApplications(ApplicationInfoFlags.of(flags))
    } else {
        this.getInstalledApplications(flags.toInt())
    }
}

@Suppress("DEPRECATION")
fun <T> Intent.getParcelableExtraCompat(name: String?, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(name, clazz)
    } else {
        this.getParcelableExtra(name)
    }
}

@Suppress("DEPRECATION", "UNCHECKED_CAST")
fun <T : Serializable?> Intent.getSerializableExtraCompat(name: String?, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(name, clazz)
    } else (
        this.getSerializableExtra(name) as? T
    )
}

@Suppress("DEPRECATION")
@Throws(PackageManager.NameNotFoundException::class, NoSuchAlgorithmException::class)
fun getApplicationSignatureDigest(packageManager: PackageManager, packageName: String): List<ByteArray> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val sig = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES.toLong()).signingInfo
        if (sig.hasMultipleSigners()) {
            sig.apkContentsSigners.map {
                val digest = MessageDigest.getInstance("SHA")
                digest.update(it.toByteArray())
                digest.digest()
            }
        } else {
            sig.signingCertificateHistory.map {
                val digest = MessageDigest.getInstance("SHA")
                digest.update(it.toByteArray())
                digest.digest()
            }
        }
    } else {
        val sig = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES.toLong()).signatures
        sig.map {
            val digest = MessageDigest.getInstance("SHA")
            digest.update(it.toByteArray())
            digest.digest()
        }
    }
}
