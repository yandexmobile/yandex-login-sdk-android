package com.yandex.authsdk
import androidx.annotation.WorkerThread
import com.yandex.authsdk.internal.JwtRequest
import java.io.IOException

internal class YandexAuthSdkImpl(
    private val options: YandexAuthOptions
): YandexAuthSdk {

    override val contract: YandexAuthSdkContract
        get() = YandexAuthSdkContract(options)

    @WorkerThread
    @Throws(YandexAuthException::class)
    override fun getJwt(token: YandexAuthToken): String {
        return try {
            JwtRequest(token.value).get()
        } catch (e: IOException) {
            throw YandexAuthException(e)
        }
    }
}
