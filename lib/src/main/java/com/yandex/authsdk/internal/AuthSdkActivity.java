package com.yandex.authsdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthLoginOptions;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.internal.strategy.LoginStrategy;
import com.yandex.authsdk.internal.strategy.LoginStrategyResolver;
import com.yandex.authsdk.internal.strategy.LoginType;

import java.util.ArrayList;

import static com.yandex.authsdk.YandexAuthException.UNKNOWN_ERROR;


public class AuthSdkActivity extends Activity {

    public static final int LOGIN_REQUEST_CODE = 312; // TODO choose number?

    private static final String STATE_LOGIN_TYPE = "com.yandex.authsdk.STATE_LOGIN_TYPE";
    private static final String TAG = AuthSdkActivity.class.getSimpleName();

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private LoginType loginType;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private YandexAuthOptions options;

    @SuppressWarnings("NullableProblems") // onCreate
    @NonNull
    private LoginStrategyResolver loginStrategyResolver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        options = getIntent().getParcelableExtra(Constants.EXTRA_OPTIONS);
        loginStrategyResolver = new LoginStrategyResolver(
                getApplicationContext(),
                new PackageManagerHelper(
                        getPackageName(),
                        getPackageManager(),
                        options
                )
        );
        if (savedInstanceState == null) {
            final YandexAuthLoginOptions loginOptions = getIntent().getParcelableExtra(Constants.EXTRA_LOGIN_OPTIONS);
            try {
                final LoginStrategy strategy = loginStrategyResolver.getLoginStrategy();
                loginType = strategy.getType();
                strategy.login(this, options, loginOptions);
            } catch (final Exception e) {
                finishWithError(e);
            }
        } else {
            loginType = LoginType.values()[savedInstanceState.getInt(STATE_LOGIN_TYPE)];
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(STATE_LOGIN_TYPE, loginType.ordinal());
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

        final LoginStrategy.ResultExtractor extractor = loginStrategyResolver.getResultExtractor(loginType);

        final YandexAuthToken yandexAuthToken = extractor.tryExtractToken(data);
        if (yandexAuthToken != null) {
            Logger.d(options, TAG, "Token received");
            final Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_TOKEN, yandexAuthToken);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }

        final YandexAuthException error = extractor.tryExtractError(data);
        if (error != null) {
            Logger.d(options, TAG, "Error received");
            final Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_ERROR, error);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }

        Logger.d(options, TAG, "Nothing received");
    }

    private void finishWithError(@NonNull final Exception e) {
        Logger.e(options, TAG, "Unknown error:", e);
        final Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_ERROR, new YandexAuthException(UNKNOWN_ERROR));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
