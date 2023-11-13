package com.yandex.authsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.yandex.authsdk.internal.AuthSdkActivity
import com.yandex.authsdk.internal.Constants
import com.yandex.authsdk.internal.YandexAuthSdkParams
import com.yandex.authsdk.internal.getParcelableExtraCompat
import com.yandex.authsdk.internal.getSerializableExtraCompat

class YandexAuthSdkContract(private val options: YandexAuthOptions)
    : ActivityResultContract<YandexAuthLoginOptions, YandexAuthResult>() {

    override fun createIntent(context: Context, input: YandexAuthLoginOptions): Intent {
        val intent = Intent(context, AuthSdkActivity::class.java)
        intent.putExtra(Constants.EXTRA_OPTIONS, options)
        intent.putExtra(Constants.EXTRA_LOGIN_OPTIONS, input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): YandexAuthResult {
        if (intent == null || resultCode != Activity.RESULT_OK) {
            return YandexAuthResult.Cancelled
        }

        val exception =
            intent.getSerializableExtraCompat(Constants.EXTRA_ERROR, YandexAuthException::class.java)
        if (exception != null) {
            return YandexAuthResult.Failure(exception)
        }

        val yandexAuthToken =
            intent.getParcelableExtraCompat(Constants.EXTRA_TOKEN, YandexAuthToken::class.java)
        return yandexAuthToken?.let {
            YandexAuthResult.Success(it)
        } ?: YandexAuthResult.Cancelled
    }

    internal companion object {

        fun Intent.toYandexAuthSdkParams(): YandexAuthSdkParams = YandexAuthSdkParams(
            getParcelableExtraCompat(Constants.EXTRA_OPTIONS, YandexAuthOptions::class.java)!!,
            getParcelableExtraCompat(Constants.EXTRA_LOGIN_OPTIONS, YandexAuthLoginOptions::class.java)!!,
        )
    }
}
