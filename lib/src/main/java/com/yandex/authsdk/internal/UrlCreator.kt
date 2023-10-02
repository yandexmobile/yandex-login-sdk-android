package com.yandex.authsdk.internal

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import com.yandex.authsdk.YandexAuthOptions
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*

internal class UrlCreator {

    fun getUrl(options: YandexAuthOptions, state: String): String {
        val redirectUri = createEncodedRedirectUrl(options)
        // Don't use force confirm due to browser redirect problem
        return String.format(
            LOGIN_URL_FORMAT,
            getOauthHost(options),
            options.clientId,
            redirectUri,
            state
        )
            .let { StringBuilder(it) }
            .toString()
    }

    @Throws(UnsupportedEncodingException::class)
    private fun createEncodedRedirectUrl(options: YandexAuthOptions): String {
        return URLEncoder.encode(createRedirectUrl(options), "UTF-8")
    }

    fun createRedirectUrl(options: YandexAuthOptions): String {
        return if (SUPPORT_APPLINKS) {
            String.format(REDIRECT_URI_APPLINKS, options.clientId, options.oauthHost)
        } else {
            String.format(REDIRECT_URI_SCHEME, options.clientId)
        }
    }

    private fun getOauthHost(options: YandexAuthOptions): String {
        return getLocalizedHost(options.oauthHost, Locale.getDefault())
    }

    companion object {

        private const val LOGIN_URL_FORMAT = "https://%s/authorize" +
                "?response_type=token" +
                "&client_id=%s" +
                "&redirect_uri=%s" +
                "&state=%s" +
                "&force_confirm=true" +
                "&origin=yandex_auth_sdk_android_v3"

        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
        private val SUPPORT_APPLINKS = Build.VERSION.SDK_INT >= 23

        private const val REDIRECT_URI_APPLINKS = "https://yx%s.%s/auth/finish?platform=android"

        private const val REDIRECT_URI_SCHEME = "yx%s:///auth/finish?platform=android"
    }
}
