package com.yandex.authsdk.internal.strategy

import android.content.Context
import android.content.Intent
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthSdkParams
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.Constants
import com.yandex.authsdk.internal.WebViewLoginActivity

internal class WebViewLoginStrategy : LoginStrategy() {

    override val type = LoginType.WEBVIEW

    override val contract = object : LoginContract(ResultExtractor()) {

        override fun createIntent(context: Context, input: YandexAuthSdkParams): Intent {
            return Intent(context, WebViewLoginActivity::class.java).apply {
                putExtras(this, input.options, input.loginOptions)
            }
        }
    }

    internal class ResultExtractor : LoginStrategy.ResultExtractor {

        override fun tryExtractToken( data: Intent): YandexAuthToken? {
            return data.getParcelableExtra(Constants.EXTRA_TOKEN)
        }

        override fun tryExtractError( data: Intent): YandexAuthException? {
            return data.getSerializableExtra(Constants.EXTRA_ERROR) as YandexAuthException?
        }
    }

    companion object {
        fun get(): LoginStrategy {
            return WebViewLoginStrategy()
        }
    }
}
