package com.yandex.authsdk.internal.strategy

import android.content.Context
import android.content.Intent
import com.yandex.authsdk.BuildConfig
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthSdkParams
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.PackageManagerHelper

internal class NativeLoginStrategy private constructor(
    private val packagedIntent: Intent
) : LoginStrategy() {

    override val type = LoginType.NATIVE

    override val contract = object : LoginContract(ResultExtractor()) {

        override fun createIntent(context: Context, input: YandexAuthSdkParams): Intent {
            return putExtrasNative(packagedIntent, input.options, input.loginOptions)
        }
    }

    internal class ResultExtractor : LoginStrategy.ResultExtractor {

        override fun tryExtractToken( data: Intent): YandexAuthToken? {
            val token = data.getStringExtra(EXTRA_OAUTH_TOKEN)
            val expiresIn = data.getLongExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 0)
            return token?.let { YandexAuthToken(it, expiresIn) }
        }

        override fun tryExtractError( data: Intent): YandexAuthException? {
            val isError = data.getBooleanExtra(OAUTH_TOKEN_ERROR, false)
            if (!isError) {
                return null
            }

            val errorMessages = data.getStringArrayExtra(OAUTH_TOKEN_ERROR_MESSAGES)
            return errorMessages?.let { YandexAuthException(it) }
                ?: YandexAuthException(YandexAuthException.CONNECTION_ERROR)
        }
    }

    companion object  {

        const val EXTRA_OAUTH_TOKEN = "com.yandex.auth.EXTRA_OAUTH_TOKEN"
        const val EXTRA_OAUTH_TOKEN_TYPE = "com.yandex.auth.EXTRA_OAUTH_TOKEN_TYPE"
        const val EXTRA_OAUTH_TOKEN_EXPIRES = "com.yandex.auth.OAUTH_TOKEN_EXPIRES"
        const val OAUTH_TOKEN_ERROR = "com.yandex.auth.OAUTH_TOKEN_ERROR"
        const val OAUTH_TOKEN_ERROR_MESSAGES = "com.yandex.auth.OAUTH_TOKEN_ERROR_MESSAGES"

        /**
         * 1. Get all activities, that can handle "com.yandex.auth.action.YA_SDK_LOGIN" action<br></br>
         * 2. Check every activity if it suits requirements:<br></br>
         * 2.1 meta "com.yandex.auth.LOGIN_SDK_VERSION" in app manifest more or equal than current SDK version<br></br>
         * 2.2 app fingerprint matches known AM fingerprint<br></br>
         * 3. Return app, with max "com.yandex.auth.VERSION" meta.
         *
         * @param packageManagerHelper
         * @return LoginStrategy for native authorization or null
         */
        fun getIfPossible(packageManagerHelper: PackageManagerHelper): LoginStrategy? {
            val applicationInfo = packageManagerHelper.findLatestApplication()
            if (applicationInfo != null) {
                val intent = getActionIntent(applicationInfo.packageName)
                return NativeLoginStrategy(intent)
            }
            return null
        }

        fun getActionIntent(packageName: String): Intent {
            val intent = Intent(BuildConfig.ACTION_YA_SDK_LOGIN)
            intent.setPackage(packageName)
            return intent
        }
    }
}
