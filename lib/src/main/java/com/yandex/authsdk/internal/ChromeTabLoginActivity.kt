package com.yandex.authsdk.internal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import com.yandex.authsdk.YandexAuthOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

internal class ChromeTabLoginActivity : AppCompatActivity() {

    private lateinit var loginHandler: ExternalLoginHandler

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { parseTokenFromUri(it) }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = intent.getParcelableExtraCompat(Constants.EXTRA_OPTIONS, YandexAuthOptions::class.java)
        if (options == null) {
            // Ignore opening this activity from browser
            finish()
            return
        }

        loginHandler = ExternalLoginHandler(
            PreferencesHelper(this),
            { UUID.randomUUID().toString() },
            UrlCreator()
        )

        if (savedInstanceState == null) {
            val url = loginHandler.getUrl(intent)
            val browserPackageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
            openLinkInChromeTabs(url, browserPackageName)
        }

        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.withResumed {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    private fun openLinkInChromeTabs(url: String, browserPackageName: String?) {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.intent.putExtra(
            Intent.EXTRA_REFERRER,
            Uri.parse("android-app://$packageName")
        )
        customTabsIntent.intent.setPackage(browserPackageName)
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }

    private fun parseTokenFromUri(data: Uri) {
        val result = loginHandler.parseResult(data)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object  {
        const val EXTRA_PACKAGE_NAME = "com.yandex.authsdk.internal.EXTRA_PACKAGE_NAME"
    }
}
