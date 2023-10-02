package com.yandex.authsdk.internal

import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthToken

internal class ExternalLoginHandler(
    private val preferencesHelper: PreferencesHelper,
    private val stateGenerator: () -> String,
    private val urlCreator: UrlCreator
) {

    fun getUrl(intent: Intent): String {
        val options = intent
            .getParcelableExtraCompat(Constants.EXTRA_OPTIONS, YandexAuthOptions::class.java)!!
        val state = stateGenerator()
        saveState(state)
        return urlCreator.getUrl(options, state)
    }

    fun parseResult(data: Uri): Intent {
        val state = restoreState()
        val fragment = data.fragment

        val dummyUri = Uri.parse("dummy://dummy?$fragment")

        val result = Intent()

        val serverState = dummyUri.getQueryParameter("state")
        if (TextUtils.isEmpty(serverState) || serverState != state) {
            result.putExtra(Constants.EXTRA_ERROR, YandexAuthException(YandexAuthException.SECURITY_ERROR))
            return result
        }

        val error = dummyUri.getQueryParameter("error")
        if (error != null) {
            result.putExtra(Constants.EXTRA_ERROR, YandexAuthException(error))
        } else {
            val token = dummyUri.getQueryParameter("access_token")
            val expiresInString = dummyUri.getQueryParameter("expires_in")
            val expiresIn = expiresInString?.toLong() ?: Long.MAX_VALUE
            token?.let { result.putExtra(Constants.EXTRA_TOKEN, YandexAuthToken(it, expiresIn)) }
        }
        return result
    }

    fun isFinalUrl(options: YandexAuthOptions, url: String): Boolean {
        return url.startsWith(urlCreator.createRedirectUrl(options))
    }

    private fun saveState(state: String) {
        preferencesHelper.saveStateValue(state)
    }

    private fun restoreState(): String? {
        return preferencesHelper.restoreStateValue()
    }
}
