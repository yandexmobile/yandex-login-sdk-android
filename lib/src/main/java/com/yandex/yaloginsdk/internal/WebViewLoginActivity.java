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

import com.yandex.yaloginsdk.internal.ExternalLoginHandler;

import java.util.UUID;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CLIENT_ID;

public class WebViewLoginActivity extends AppCompatActivity {

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private ExternalLoginHandler loginHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginHandler = new ExternalLoginHandler(() -> UUID.randomUUID().toString());

        clearCookies();
        final WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(loginHandler.getUrl(getIntent().getStringExtra(EXTRA_CLIENT_ID)));

        setContentView(webView);

        if (savedInstanceState != null) {
            loginHandler.restoreState(savedInstanceState);
        }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        loginHandler.saveState(outState);
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
