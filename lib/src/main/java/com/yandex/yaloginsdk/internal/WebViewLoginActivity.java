package com.yandex.yaloginsdk.internal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.yandex.yaloginsdk.LoginSdkConfig;

import java.util.UUID;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CONFIG;


public class WebViewLoginActivity extends AppCompatActivity {

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private ExternalLoginHandler loginHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final LoginSdkConfig config = getIntent().getParcelableExtra(EXTRA_CONFIG);

        // no need to save state, url will be loaded once again after rotation
        loginHandler = new ExternalLoginHandler(config, () -> UUID.randomUUID().toString());

        if (savedInstanceState == null) {
            clearCookies();
        }

        final WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(loginHandler.getUrl(config.clientId()));

        setContentView(webView);
    }

    private void clearCookies() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void parseTokenFromUrl(@NonNull String url) {
        final Intent result = loginHandler.parseResult(Uri.parse(url));
        setResult(RESULT_OK, result);
        finish();
    }

    private class WebViewClient extends android.webkit.WebViewClient {

        @Override
        public void onPageStarted(@NonNull WebView view, @NonNull String url, @NonNull Bitmap favicon) {
            if (loginHandler.isFinalUrl(url)) {
                parseTokenFromUrl(url);
            } else {
                super.onPageStarted(view, url, favicon);
            }
        }
    }
}
