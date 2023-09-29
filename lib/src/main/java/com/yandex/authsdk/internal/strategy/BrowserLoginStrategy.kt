package com.yandex.authsdk.internal.strategy

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthSdkParams
import com.yandex.authsdk.YandexAuthToken
import com.yandex.authsdk.internal.BrowserLoginActivity
import com.yandex.authsdk.internal.Constants

internal class BrowserLoginStrategy private constructor (
    private val context: Context,
    private val browserPackageName: String
) : LoginStrategy() {

    internal enum class SupportedBrowser(val priority: Int, val packageName: String) {
        YANDEX(2, "com.yandex.searchapp"),
        YABROWSER(1, "com.yandex.browser"),
        CHROME(0, "com.android.chrome");
    }

    override val contract = object : LoginContract(ResultExtractor()) {

        override fun createIntent(context: Context, input: YandexAuthSdkParams): Intent {
            return Intent(context, BrowserLoginActivity::class.java).apply {
                putExtra(
                    BrowserLoginActivity.EXTRA_BROWSER_PACKAGE_NAME,
                    browserPackageName
                )
                putExtras(this, input.options, input.loginOptions)
            }
        }
    }

    override val type: LoginType = LoginType.BROWSER

    internal class ResultExtractor : LoginStrategy.ResultExtractor {

        override fun tryExtractToken( data: Intent): YandexAuthToken? {
            return data.getParcelableExtra(Constants.EXTRA_TOKEN)
        }

        override fun tryExtractError( data: Intent): YandexAuthException? {
            return data.getSerializableExtra(Constants.EXTRA_ERROR) as YandexAuthException?
        }
    }

    companion object {

        private const val TEST_WEB_URI = "https://ya.ru"

        fun getIfPossible(context: Context, packageManager: PackageManager): LoginStrategy? {
            val sampleUri = Uri.parse(TEST_WEB_URI)
            val browserIntent = Intent(Intent.ACTION_VIEW, sampleUri)
            val infos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                packageManager.queryIntentActivities(browserIntent, PackageManager.MATCH_ALL)
            } else {
                packageManager.queryIntentActivities(browserIntent, 0)
            }

            val defaultBrowserPackageName = defaultBrowserPackageName(context)
            val bestPackageName = findBest(defaultBrowserPackageName, infos)
            return bestPackageName?.let { BrowserLoginStrategy(context, it) }
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

        private fun defaultBrowserPackageName(context: Context): String? {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com"))
            val resolveInfo = context.packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            var defaultBrowserPkg: String? = null
            if (resolveInfo != null) {
                if (resolveInfo.activityInfo != null) {
                    defaultBrowserPkg = resolveInfo.activityInfo.packageName
                }
            }
            return defaultBrowserPkg
        }
    }
}
