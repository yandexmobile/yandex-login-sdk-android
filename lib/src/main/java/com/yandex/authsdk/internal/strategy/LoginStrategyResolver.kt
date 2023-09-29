package com.yandex.authsdk.internal.strategy

import android.content.Context
import com.yandex.authsdk.internal.PackageManagerHelper

internal class LoginStrategyResolver(
    private val context: Context,
    private val packageManagerHelper: PackageManagerHelper
) {

    fun getLoginStrategy(preferredLoginType: LoginType?): LoginStrategy {
        return when (preferredLoginType) {
            null, LoginType.NATIVE -> {
                NativeLoginStrategy.getIfPossible(packageManagerHelper)
                    ?: BrowserLoginStrategy.getIfPossible(context, context.packageManager)
                    ?: WebViewLoginStrategy.get()
            }

            LoginType.BROWSER -> {
                BrowserLoginStrategy.getIfPossible(context, context.packageManager)
                    ?: WebViewLoginStrategy.get()
            }

            LoginType.WEBVIEW -> WebViewLoginStrategy.get()
        }
    }
}
