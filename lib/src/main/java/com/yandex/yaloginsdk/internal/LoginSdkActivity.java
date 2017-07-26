package com.yandex.yaloginsdk.internal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.internal.strategy.LoginStrategy;
import com.yandex.yaloginsdk.internal.strategy.LoginStrategyProvider;
import com.yandex.yaloginsdk.internal.strategy.LoginType;

import java.util.ArrayList;


public class LoginSdkActivity extends AppCompatActivity {

    public static final int LOGIN_REQUEST_CODE = 312; // TODO choose number?

    private static final String STATE_LOGIN_TYPE = "com.yandex.yaloginsdk.STATE_LOGIN_TYPE";
    private static final String TAG = LoginSdkActivity.class.getSimpleName();

    @Nullable
    private LoginType loginType;
    private LoginSdkConfig config;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = getIntent().getParcelableExtra(YaLoginSdkConstants.EXTRA_CONFIG);
        if (savedInstanceState == null) {
            final ArrayList<String> scopes = getIntent().getStringArrayListExtra(YaLoginSdkConstants.EXTRA_SCOPES);
            final LoginStrategy strategy = new LoginStrategyProvider().getLoginStrategy(this, config);
            loginType = strategy.getType();
            strategy.login(this, config, scopes == null ? new ArrayList<>() : scopes);
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

        final Token token = extractor.tryExtractToken(data);
        if (token != null) {
            Logger.d(config, TAG, "Token received");
            Intent intent = new Intent();
            intent.putExtra(YaLoginSdkConstants.EXTRA_TOKEN, token);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }

        final YaLoginSdkError error = extractor.tryExtractError(data);
        if (error != null) {
            Logger.d(config, TAG, "Error received");
            Intent intent = new Intent();
            intent.putExtra(YaLoginSdkConstants.EXTRA_ERROR, error);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return;
        }

        Logger.d(config, TAG, "Nothing received");
    }
}
