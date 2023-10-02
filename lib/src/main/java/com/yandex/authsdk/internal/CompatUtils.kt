package com.yandex.authsdk.internal

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import java.io.Serializable

@Suppress("DEPRECATION")
fun PackageManager.queryIntentActivities(intent: Intent, flags: Long = 0L): List<ResolveInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags))
    } else {
        this.queryIntentActivities(intent, flags.toInt())
    }
}

@Suppress("DEPRECATION")
fun PackageManager.resolveService(intent: Intent, flags: Long = 0L): ResolveInfo? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.resolveService(intent, PackageManager.ResolveInfoFlags.of(flags))
    } else {
        this.resolveService(intent, flags.toInt())
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

@Suppress("DEPRECATION")
fun <T : Serializable> Intent.getSerializableExtraCompat(name: String?, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(name, clazz)
    } else {
        this.getSerializableExtra(name) as? T
    }
}
