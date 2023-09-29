package com.yandex.authsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.yandex.authsdk.internal.AuthSdkActivity
import com.yandex.authsdk.internal.Constants

class YandexAuthSdkContract
    : ActivityResultContract<YandexAuthSdkParams, Result<YandexAuthToken?>>() {

    override fun createIntent(context: Context, input: YandexAuthSdkParams): Intent {
        val intent = Intent(context, AuthSdkActivity::class.java)
        intent.putExtra(Constants.EXTRA_OPTIONS, input.options)
        intent.putExtra(Constants.EXTRA_LOGIN_OPTIONS, input.loginOptions)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Result<YandexAuthToken?> {
        if (intent == null || resultCode != Activity.RESULT_OK) {
            return Result.success(null)
        }
        val exception = intent.getSerializableExtra(Constants.EXTRA_ERROR) as? YandexAuthException
        if (exception != null) {
            return Result.failure(exception)
        }

        val yandexAuthToken = intent.getParcelableExtra<YandexAuthToken>(Constants.EXTRA_TOKEN)
        return Result.success(yandexAuthToken)
    }

    internal companion object {

        fun Intent.toYandexAuthSdkParams(): YandexAuthSdkParams = YandexAuthSdkParams(
            getParcelableExtra(Constants.EXTRA_OPTIONS)!!,
            getParcelableExtra(Constants.EXTRA_LOGIN_OPTIONS)!!,
        )
    }
}
