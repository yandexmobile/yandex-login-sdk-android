package com.yandex.authsdk.internal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.yandex.authsdk.YandexAuthOptions
import java.util.UUID

internal class BrowserLoginActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())

    private val finishRunnable = Runnable {
        setResult(RESULT_CANCELED)
        finish()
    }

    private lateinit var loginHandler: ExternalLoginHandler

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { parseTokenFromUri(it) }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = intent.getParcelableExtra<YandexAuthOptions>(Constants.EXTRA_OPTIONS)
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
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                setPackage(intent.getStringExtra(EXTRA_BROWSER_PACKAGE_NAME))
                data = Uri.parse(loginHandler.getUrl(intent))
            }
            startActivity(browserIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(finishRunnable)
    }

    override fun onPause() {
        handler.removeCallbacks(finishRunnable)
        super.onPause()
    }

    private fun parseTokenFromUri(data: Uri) {
        val result = loginHandler.parseResult(data)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object  {
        const val EXTRA_BROWSER_PACKAGE_NAME = "com.yandex.authsdk.internal.EXTRA_BROWSER_PACKAGE_NAME"
    }
}
