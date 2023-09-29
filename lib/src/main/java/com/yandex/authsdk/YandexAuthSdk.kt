package com.yandex.authsdk

import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import com.yandex.authsdk.exceptions.YandexAuthInteractionException
import com.yandex.authsdk.exceptions.YandexAuthSecurityException

interface YandexAuthSdk {

    val contract: YandexAuthSdkContract

    @Deprecated("use contract")
    fun createLoginIntent(loginOptions: YandexAuthLoginOptions): Intent

    @Deprecated("Use contract")
    @Throws(YandexAuthException::class)
    fun extractToken(resultCode: Int, data: Intent?): YandexAuthToken?

    @Throws(YandexAuthException::class)
    @WorkerThread
    fun getJwt(token: YandexAuthToken): String

    @Throws(YandexAuthSecurityException::class, YandexAuthInteractionException::class)
    @WorkerThread
    fun getAccounts(): List<YandexAuthAccount>

    companion object {

        @JvmStatic
        fun create(context: Context, options: YandexAuthOptions): YandexAuthSdk {
            return YandexAuthSdkImpl(context, options)
        }
    }
}
