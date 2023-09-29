package com.yandex.authsdk.internal

import android.util.Log
import com.yandex.authsdk.YandexAuthOptions

internal object Logger {

    fun e(options: YandexAuthOptions, tag: String, message: String, e: Throwable) {
        if (options.isLoggingEnabled) {
            Log.e(tag, message, e)
        }
    }

    fun d(options: YandexAuthOptions, tag: String, message: String) {
        if (options.isLoggingEnabled) {
            Log.d(tag, message)
        }
    }
}
