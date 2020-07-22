package com.yandex.authsdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthOptions;

import java.util.UUID;

import static com.yandex.authsdk.internal.Constants.EXTRA_LOGIN_HINT;
import static com.yandex.authsdk.internal.Constants.EXTRA_OPTIONS;
import static com.yandex.authsdk.internal.Constants.EXTRA_UID_VALUE;

public class BrowserLoginActivity extends Activity {

    public static final String EXTRA_BROWSER_PACKAGE_NAME = "com.yandex.authsdk.internal.EXTRA_BROWSER_PACKAGE_NAME";

    private final Handler handler = new Handler();

    private final Runnable finishRunnable = () -> {
        setResult(RESULT_CANCELED);
        finish();
    };

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private ExternalLoginHandler loginHandler;

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        parseTokenFromUri(intent.getData());
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final YandexAuthOptions options = getIntent().getParcelableExtra(EXTRA_OPTIONS);

        if (options == null) {
            // Ignore opening this activity from browser
            finish();
            return;
        }

        loginHandler = new ExternalLoginHandler(new PreferencesHelper(this), () -> UUID.randomUUID().toString());

        if (savedInstanceState == null) {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setPackage(getIntent().getStringExtra(EXTRA_BROWSER_PACKAGE_NAME));
            browserIntent.setData(Uri.parse(loginHandler.getUrl(getIntent())));
            startActivity(browserIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(finishRunnable);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(finishRunnable);
        super.onPause();
    }

    private void parseTokenFromUri(@NonNull final Uri data) {
        final Intent result = loginHandler.parseResult(data);
        setResult(RESULT_OK, result);
        finish();
    }
}
