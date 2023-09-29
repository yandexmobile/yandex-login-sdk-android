package com.yandex.authsdk
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.WorkerThread
import com.yandex.authsdk.exceptions.YandexAuthInteractionException
import com.yandex.authsdk.exceptions.YandexAuthSecurityException
import com.yandex.authsdk.internal.AuthSdkActivity
import com.yandex.authsdk.internal.Constants
import com.yandex.authsdk.internal.JwtRequest
import com.yandex.authsdk.internal.Logger
import com.yandex.authsdk.internal.PackageManagerHelper
import com.yandex.authsdk.internal.provider.ProviderClient
import com.yandex.authsdk.internal.provider.ProviderClientResolver
import java.io.IOException

internal class YandexAuthSdkImpl(
    context: Context,
    private val options: YandexAuthOptions
): YandexAuthSdk {

    private val providerClient: ProviderClient?

    private val context: Context

    init {
        providerClient = ProviderClientResolver(
            PackageManagerHelper(context.packageName, context.packageManager, options)
        ).createProviderClient(context)
        this.context = context
    }

    @Deprecated("Use contract")
    fun createLoginIntent(
        requiredScopes: Set<String>,
        optionalScopes: Set<String>,
    ): Intent {
        return createLoginIntent(requiredScopes, optionalScopes, null, null)
    }

    @Deprecated("Use contract")
    fun createLoginIntent(
        requiredScopes: Set<String>,
        optionalScopes: Set<String>,
        uid: Long?,
        loginHint: String?
    ): Intent {
        return createLoginIntent(
            YandexAuthLoginOptions.Builder()
                .setRequiredScopes(requiredScopes)
                .setOptionalScopes(optionalScopes)
                .setUid(uid)
                .setLoginHint(loginHint)
                .build()
        )
    }

    @Deprecated("Use contract")
    override fun createLoginIntent(
        loginOptions: YandexAuthLoginOptions
    ): Intent {
        val intent = Intent(context, AuthSdkActivity::class.java)
        intent.putExtra(Constants.EXTRA_OPTIONS, options)
        intent.putExtra(Constants.EXTRA_LOGIN_OPTIONS, loginOptions)
        return intent
    }

    @Deprecated("Use contract")
    @Throws(YandexAuthException::class)
    override fun extractToken(resultCode: Int, data: Intent?): YandexAuthToken? {
        if (data == null || resultCode != Activity.RESULT_OK) {
            return null
        }
        val exception = data.getSerializableExtra(Constants.EXTRA_ERROR) as? YandexAuthException
        if (exception != null) {
            Logger.d(options, TAG, "Exception received")
            throw exception
        }
        return data.getParcelableExtra(Constants.EXTRA_TOKEN)
    }

    override val contract: YandexAuthSdkContract
        get() = YandexAuthSdkContract()

    @WorkerThread
    @Throws(YandexAuthException::class)
    override fun getJwt(token: YandexAuthToken): String {
        return try {
            JwtRequest(token.value).get()
        } catch (e: IOException) {
            throw YandexAuthException(e)
        }
    }

    @Throws(YandexAuthSecurityException::class, YandexAuthInteractionException::class)
    @WorkerThread
    override fun getAccounts(): List<YandexAuthAccount> {
        if (providerClient == null) {
            throw YandexAuthInteractionException("Yandex AuthSDK provider not found")
        }
        return providerClient.getAccounts()
    }

    companion object {
        private val TAG = YandexAuthSdkImpl::class.java.simpleName
    }
}
