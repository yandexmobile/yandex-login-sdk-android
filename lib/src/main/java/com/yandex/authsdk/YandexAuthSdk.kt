package com.yandex.authsdk

import androidx.annotation.WorkerThread

interface YandexAuthSdk {

    val contract: YandexAuthSdkContract

    @Throws(YandexAuthException::class)
    @WorkerThread
    fun getJwt(token: YandexAuthToken): String

    companion object {

        @JvmStatic
        fun create(options: YandexAuthOptions): YandexAuthSdk {
            return YandexAuthSdkImpl(options)
        }
    }
}
