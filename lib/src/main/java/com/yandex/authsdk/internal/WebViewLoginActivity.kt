package com.yandex.authsdk.internal

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import com.yandex.authsdk.YandexAuthOptions
import java.util.UUID

internal class WebViewLoginActivity : Activity() {

    private lateinit var loginHandler: ExternalLoginHandler

    private lateinit var options: YandexAuthOptions

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = intent.getParcelableExtraCompat(Constants.EXTRA_OPTIONS, YandexAuthOptions::class.java)
        if (options == null){
            finish()
            return
        }
        this.options = options

        // no need to save state, url will be loaded once again after rotation
        loginHandler = ExternalLoginHandler(
            PreferencesHelper(this),
            { UUID.randomUUID().toString() },
            UrlCreator()
        )

        if (savedInstanceState == null) {
            clearCookies()
        }

        webView = WebView(this).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(loginHandler.getUrl(intent))
        }
        setContentView(webView)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    private fun parseTokenFromUrl(url: String) {
        val result = loginHandler.parseResult(Uri.parse(url))
        setResult(RESULT_OK, result)
        finish()
    }

    private inner class WebViewClient : android.webkit.WebViewClient() {

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            if (loginHandler.isFinalUrl(options, url)) {
                parseTokenFromUrl(url)
            } else {
                super.onPageStarted(view, url, favicon)
            }
        }
    }
}
