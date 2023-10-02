package com.yandex.authsdk.internal.strategy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.browser.customtabs.CustomTabsService
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthSdkParams
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.ChromeTabLoginActivity
import com.yandex.authsdk.internal.Constants
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

    internal enum class SupportedBrowser(val priority: Int, val packageName: String) {
        YANDEX(2, "com.yandex.searchapp"),
        YABROWSER(1, "com.yandex.browser"),
        CHROME(0, "com.android.chrome");
    }

    companion object {

        private const val TEST_WEB_URI = "https://ya.ru"

        fun getIfPossible(packageManager: PackageManager): LoginStrategy? {
            val sampleUri = Uri.parse(TEST_WEB_URI)
            val intent = Intent(Intent.ACTION_VIEW, sampleUri)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL.toLong() else 0L
            val resolvedActivityList = packageManager.queryIntentActivities(intent, flags)
            val packagesSupportingCustomTabs = resolvedActivityList.filter {
                val serviceIntent = Intent()
                serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
                serviceIntent.setPackage(it.activityInfo.packageName)
                packageManager.resolveService(serviceIntent) != null
            }
            val defaultPackageName = packagesSupportingCustomTabs.getOrNull(0)?.activityInfo?.packageName
            val bestPackageName = findBest(defaultPackageName, packagesSupportingCustomTabs)
            return bestPackageName?.let { ChromeTabLoginStrategy(it) }
        }

        private fun findBest(defaultPackageName: String?, infos: List<ResolveInfo>): String? {
            var best: SupportedBrowser? = null
            for (info in infos) {
                for (current in SupportedBrowser.values()) {
                    if (info.activityInfo.packageName == current.packageName) {
                        if (current.packageName == defaultPackageName){
                            return current.packageName
                        }
                        if (best == null || best.priority < current.priority){
                            best = current
                        }
                    }
                }
            }
            return best?.packageName
        }
    }
}
