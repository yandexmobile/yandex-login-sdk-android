package com.yandex.authsdk.internal.provider

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.yandex.authsdk.YandexAuthAccount
import com.yandex.authsdk.exceptions.YandexAuthInteractionException
import com.yandex.authsdk.exceptions.YandexAuthSecurityException

class ProviderClient internal constructor(
    private val context: Context,
    packageName: String,
    sdkVersion: Int
) {

    private val uri: Uri

    private val sdkVersion: Int

    private enum class Method {
        GetAccounts
    }

    init {
        uri = Uri.parse("content://com.yandex.passport.authsdk.provider.$packageName")
        this.sdkVersion = sdkVersion
    }

    @Throws(YandexAuthSecurityException::class, YandexAuthInteractionException::class)
    fun getAccounts(): List<YandexAuthAccount> {
        return if (sdkVersion >= 2) {
            accountsFromBundle(call(Method.GetAccounts, null, null))
        } else {
            throw YandexAuthInteractionException("Method not supported")
        }
    }

    @Throws(YandexAuthSecurityException::class, YandexAuthInteractionException::class)
    private fun call(method: Method, arg: String?, extras: Bundle?): Bundle {
        val bundle = try {
            context.contentResolver.call(uri, method.name, arg, extras)
        } catch (e: SecurityException) {
            throw YandexAuthSecurityException(e)
        } ?: throw YandexAuthInteractionException("Unsuccessful request to content provider")
        return bundle
    }

    private fun accountsFromBundle(bundle: Bundle): List<YandexAuthAccount> {
        val accounts: MutableList<YandexAuthAccount> = mutableListOf()
        val accountsCount = bundle.getInt(KEY_ACCOUNTS_COUNT)
        for (i in 0 until accountsCount) {
            accounts.add(
                YandexAuthAccount(
                    bundle.getLong(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_UID_VALUE),
                    bundle.getString(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_PRIMARY_DISPLAY_NAME)!!,
                    bundle.getString(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_SECONDARY_DISPLAY_NAME),
                    bundle.getBoolean(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_IS_AVATAR_EMPTY),
                    bundle.getString(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_AVATAR_URL)
                )
            )
        }
        return accounts
    }

    companion object {

        private const val KEY_SUFFIX_UID_VALUE = "com.yandex.auth.UID_VALUE"

        private const val KEY_SUFFIX_PRIMARY_DISPLAY_NAME = "com.yandex.auth.PRIMARY_DISPLAY_NAME"

        private const val KEY_SUFFIX_SECONDARY_DISPLAY_NAME = "com.yandex.auth.SECONDARY_DISPLAY_NAME"

        private const val KEY_SUFFIX_IS_AVATAR_EMPTY = "com.yandex.auth.IS_AVATAR_EMPTY"

        private const val KEY_SUFFIX_AVATAR_URL = "com.yandex.auth.AVATAR_URL"

        private const val KEY_ACCOUNTS_COUNT = "com.yandex.auth.ACCOUNTS_COUNT"

        private const val ACCOUNT_KEY_PREFIX = "account-"

        private const val SEPARATOR = "-"
    }
}
