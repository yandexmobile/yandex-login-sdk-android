package com.yandex.authsdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.internal.strategy.LoginStrategy;
import com.yandex.authsdk.internal.strategy.LoginStrategyProvider;
import com.yandex.authsdk.internal.strategy.LoginType;

import java.util.ArrayList;


public class AuthSdkActivity extends Activity {

    public static final int LOGIN_REQUEST_CODE = 312; // TODO choose number?

    private static final String STATE_LOGIN_TYPE = "com.yandex.authsdk.STATE_LOGIN_TYPE";
    private static final String TAG = AuthSdkActivity.class.getSimpleName();

    @Nullable
    private LoginType loginType;
    private YandexAuthOptions options;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        options = getIntent().getParcelableExtra(Constants.EXTRA_OPTIONS);
        if (savedInstanceState == null) {
            final ArrayList<String> scopes = getIntent().getStringArrayListExtra(Constants.EXTRA_SCOPES);
            final LoginStrategy strategy = new LoginStrategyProvider().getLoginStrategy(this, options);
            loginType = strategy.getType();
            strategy.login(this, options, scopes == null ? new ArrayList<>() : scopes);
        } else {
            loginType = LoginType.values()[savedInstanceState.getInt(STATE_LOGIN_TYPE)];
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle state) {
        super.onSaveInstanceState(state);
        if (loginType != null) {
            state.putInt(STATE_LOGIN_TYPE, loginType.ordinal());
        }
    }

    @Override
    public void onActivityResult(
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data
    ) {
        // TODO add cancel listener?
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != LOGIN_REQUEST_CODE) {
            finish();
            return;
        }

        final LoginStrategy.ResultExtractor extractor = new LoginStrategyProvider().getResultExtractor(loginType);

        final YandexAuthToken yandexAuthToken = extractor.tryExtractToken(data);
        if (yandexAuthToken != null) {
            Logger.d(options, TAG, "Token received");
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_TOKEN, yandexAuthToken);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }

        final YandexAuthException error = extractor.tryExtractError(data);
        if (error != null) {
            Logger.d(options, TAG, "Error received");
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_ERROR, error);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }

        Logger.d(options, TAG, "Nothing received");
    }
}
