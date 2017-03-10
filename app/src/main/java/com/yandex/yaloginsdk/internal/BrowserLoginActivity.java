package com.yandex.yaloginsdk.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.UUID;

import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CLIENT_ID;

public class BrowserLoginActivity extends AppCompatActivity {

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private ExternalLoginHandler loginHandler;

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        final Uri data = intent.getData();
        if (data != null) {
            parseTokenFromUri(data);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginHandler = new ExternalLoginHandler(() -> UUID.randomUUID().toString());

        if (savedInstanceState == null) {
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            final String clientId = getIntent().getStringExtra(EXTRA_CLIENT_ID);
            browserIntent.setData(Uri.parse(loginHandler.getUrl(clientId)));
            startActivity(browserIntent);
        } else {
            loginHandler.restoreState(savedInstanceState);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        loginHandler.saveState(outState);
    }

    private void parseTokenFromUri(@NonNull final Uri data) {
        final Intent result = loginHandler.parseResult(data);
        setResult(RESULT_OK, result);
        finish();
    }
}
