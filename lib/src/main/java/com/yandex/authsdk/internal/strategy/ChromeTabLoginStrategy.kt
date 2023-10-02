package com.yandex.authsdk.internal.strategy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.browser.customtabs.CustomTabsService
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.ChromeTabLoginActivity
import com.yandex.authsdk.internal.Constants
import com.yandex.authsdk.internal.YandexAuthSdkParams
import com.yandex.authsdk.internal.getParcelableExtraCompat
import com.yandex.authsdk.internal.getSerializableExtraCompat
import com.yandex.authsdk.internal.queryIntentActivities
import com.yandex.authsdk.internal.resolveService

internal class ChromeTabLoginStrategy(
    private val packageName: String
) : LoginStrategy() {

    override val type: LoginType = LoginType.CHROME_TAB

    override val contract = object : LoginContract(ResultExtractor()) {

        override fun createIntent(context: Context, input: YandexAuthSdkParams): Intent {
            return Intent(context, ChromeTabLoginActivity::class.java).apply {
                putExtra(ChromeTabLoginActivity.EXTRA_PACKAGE_NAME, packageName)
                putExtras(this, input.options, input.loginOptions)
            }
        }
    }

    internal class ResultExtractor : LoginStrategy.ResultExtractor {

        override fun tryExtractToken(data: Intent): YandexAuthToken? {
            return data.getParcelableExtraCompat(Constants.EXTRA_TOKEN, YandexAuthToken::class.java)
        }

        override fun tryExtractError(data: Intent): YandexAuthException? {
            return data.getSerializableExtraCompat(Constants.EXTRA_ERROR, YandexAuthException::class.java)
        }
    }

    companion object {

        private const val TEST_WEB_URI = "https://ya.ru"

        fun getIfPossible(packageManager: PackageManager): LoginStrategy? {
            val sampleUri = Uri.parse(TEST_WEB_URI)
            val intent = Intent(Intent.ACTION_VIEW, sampleUri)
            val resolvedActivityList = packageManager.queryIntentActivities(intent)
            val packagesSupportingCustomTabs = resolvedActivityList.filter {
                val serviceIntent = Intent()
                serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
                serviceIntent.setPackage(it.activityInfo.packageName)
                packageManager.resolveService(serviceIntent) != null
            }
            val defaultPackageName = packagesSupportingCustomTabs.getOrNull(0)?.activityInfo?.packageName
            return defaultPackageName?.let { ChromeTabLoginStrategy(it) }
        }
    }
}
