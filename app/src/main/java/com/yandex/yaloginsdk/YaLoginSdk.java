package com.yandex.yaloginsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.yandex.yaloginsdk.strategy.LoginStrategy;
import com.yandex.yaloginsdk.strategy.LoginStrategy.ResultExtractor;
import com.yandex.yaloginsdk.strategy.LoginStrategyProvider;
import com.yandex.yaloginsdk.strategy.LoginType;

import java.util.Collections;
import java.util.Set;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.State.STATE_LOGIN_TYPE;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.LOGIN_REQUEST_CODE;

public class YaLoginSdk {

    public static final String TAG = YaLoginSdk.class.getSimpleName();

    @Nullable
    private LoginType loginType;

    @NonNull
    public static YaLoginSdk get(@NonNull LoginSdkConfig config) {
        return new YaLoginSdk(config);
    }

    @NonNull
    private final LoginSdkConfig config;

    private YaLoginSdk(@NonNull LoginSdkConfig config) {
        this.config = config;
    }

    public void login(@NonNull Activity activity, @Nullable Set<String> scopes) {
        final LoginStrategy strategy = new LoginStrategyProvider().getLoginStrategy(config.applicationContext());
        activity.startActivityForResult(
                strategy.getLoginIntent(config, scopes == null ? Collections.emptySet() : scopes),
                LOGIN_REQUEST_CODE
        );
        loginType = strategy.getType();
    }

    public void login(@NonNull Fragment fragment, @Nullable Set<String> scopes) {
        final LoginStrategy strategy = new LoginStrategyProvider().getLoginStrategy(config.applicationContext());
        fragment.startActivityForResult(
                strategy.getLoginIntent(config, scopes == null ? Collections.emptySet() : scopes),
                LOGIN_REQUEST_CODE
        );
        loginType = strategy.getType();
    }

    public boolean onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data,
            @NonNull LoginSuccessListener successListener,
            @NonNull LoginErrorListener errorListener
    ) {
        // TODO add cancel listener?
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != LOGIN_REQUEST_CODE) {
            return false;
        }
        if (loginType == null) {
            Logger.d(
                    TAG,
                    "requestCode is equals to LOGIN_REQUEST_CODE, but login is unknown. " +
                    "Please, check that you call \"onSaveInstanceState\" and \"onRestoreInstanceState\" on YaLoginSdk"
            );
            return false;
        }

        final ResultExtractor extractor = new LoginStrategyProvider().getResultExtractor(loginType);

        final Token token = extractor.tryExtractToken(data);
        if (token != null) {
            Logger.d(TAG, "Token received");
            successListener.onLoggedIn(token);
            return true;
        }

        final YaLoginSdkError error = extractor.tryExtractError(data);
        if (error != null) {
            Logger.d(TAG, "Error received");
            errorListener.onError(error);
            return true;
        }

        Logger.d(TAG, "Nothing received");
        return false;
    }

    public void logout() {

    }

    public void onSaveInstanceState(@NonNull Bundle state) {
        state.putSerializable(STATE_LOGIN_TYPE, loginType);
    }

    public void onRestoreInstanceState(@NonNull Bundle state) {
        loginType = (LoginType) state.getSerializable(STATE_LOGIN_TYPE);
    }
}
