package com.yandex.authsdk.internal.strategy

import android.app.Activity
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.Constants
import com.yandex.authsdk.internal.YandexAuthSdkParams

internal abstract class LoginStrategy {

    abstract val type: LoginType

    abstract val contract: ActivityResultContract<YandexAuthSdkParams, Result<YandexAuthToken?>>

    internal abstract class LoginContract(
        private val extractor: ResultExtractor
    ) : ActivityResultContract<YandexAuthSdkParams, Result<YandexAuthToken?>>() {

        override fun parseResult(resultCode: Int, intent: Intent?): Result<YandexAuthToken?> {
            if (intent == null || resultCode != Activity.RESULT_OK) {
                return Result.success(null)
            }

            val yandexAuthToken = extractor.tryExtractToken(intent)
            if (yandexAuthToken != null) {
                return Result.success(yandexAuthToken)
            }

            val error = extractor.tryExtractError(intent)
            if (error != null) {
                return Result.failure(error)
            }

            return Result.success(null)
        }
    }

    interface ResultExtractor {

        fun tryExtractToken(data: Intent): YandexAuthToken?

        fun tryExtractError(data: Intent): YandexAuthException?
    }

    companion object {

        fun putExtras(
            intent: Intent,
            options: YandexAuthOptions,
            loginOptions: YandexAuthLoginOptions
        ): Intent {
            return intent.apply {
                putExtra(Constants.EXTRA_OPTIONS, options)
                putExtra(Constants.EXTRA_LOGIN_OPTIONS, loginOptions)
            }
        }

        fun putExtrasNative(
            intent: Intent,
            options: YandexAuthOptions
        ): Intent {
            return intent.apply {
                putExtra(Constants.EXTRA_CLIENT_ID, options.clientId)
                putExtra(Constants.EXTRA_USE_TESTING_ENV, options.isTesting)
                putExtra(Constants.EXTRA_FORCE_CONFIRM, true)
            }
        }
    }
}
