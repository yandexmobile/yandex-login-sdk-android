package com.yandex.yaloginsdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.yandex.yaloginsdk.internal.ActivityStarter;
import com.yandex.yaloginsdk.internal.JwtRequest;
import com.yandex.yaloginsdk.internal.Logger;
import com.yandex.yaloginsdk.internal.strategy.LoginStrategy;
import com.yandex.yaloginsdk.internal.strategy.LoginStrategy.ResultExtractor;
import com.yandex.yaloginsdk.internal.strategy.LoginStrategyProvider;
import com.yandex.yaloginsdk.internal.strategy.LoginType;

import java.io.IOException;
import java.util.Set;

import static java.util.Collections.emptySet;

public class YaLoginSdk {

    public static int LOGIN_REQUEST_CODE = 312; // TODO choose number?
    private static final String STATE_LOGIN_TYPE = "com.yandex.yaloginsdk.STATE_LOGIN_TYPE";
    private static final String TAG = YaLoginSdk.class.getSimpleName();

    @NonNull
    public static YaLoginSdk get(@NonNull final LoginSdkConfig config) {
        return new YaLoginSdk(config);
    }

    @Nullable
    private LoginType loginType;

    @NonNull
    private final LoginSdkConfig config;

    private YaLoginSdk(@NonNull final LoginSdkConfig config) {
        this.config = config;
    }

    public void login(@NonNull final FragmentActivity activity, @Nullable final Set<String> scopes) {
        startAuthorization(new ActivityStarter(activity), scopes);
    }

    public void login(@NonNull final Fragment fragment, @Nullable final Set<String> scopes) {
        startAuthorization(new ActivityStarter(fragment), scopes);
    }

    private void startAuthorization(@NonNull final ActivityStarter starter, @Nullable final Set<String> scopes) {
        final LoginStrategy strategy = new LoginStrategyProvider().getLoginStrategy(config.applicationContext());
        loginType = strategy.getType();
        strategy.login(starter, config, scopes == null ? emptySet() : scopes);
    }

    public boolean onActivityResult(
            final int requestCode,
            final int resultCode,
            @Nullable final Intent data,
            @NonNull final SuccessListener<Token> successListener,
            @NonNull final ErrorListener errorListener
    ) {
        // TODO add cancel listener?
        if (data == null || resultCode != Activity.RESULT_OK || requestCode != LOGIN_REQUEST_CODE) {
            return false;
        }
        if (loginType == null) {
            Logger.d(
                    TAG,
                    "requestCode is equals to LOGIN_REQUEST_CODE, but login type is unknown. " +
                            "Please, check that you call \"onSaveInstanceState\" and \"onRestoreInstanceState\" on YaLoginSdk"
            );
            return false;
        }

        final ResultExtractor extractor = new LoginStrategyProvider().getResultExtractor(loginType);

        final Token token = extractor.tryExtractToken(data);
        if (token != null) {
            Logger.d(TAG, "Token received");
            successListener.onSuccess(token);
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

    public void getJwt(
            @NonNull final String token,
            @NonNull final SuccessListener<String> successListener,
            @NonNull final ErrorListener errorListener
    ) {
        final HandlerThread handlerThread = new HandlerThread("YaLoginSdkIOThread");
        handlerThread.start();
        final Handler ioHandler = new Handler(handlerThread.getLooper());
        final Handler uiHandler = new Handler(Looper.getMainLooper());

        ioHandler.post(() -> {
            try {
                String jwt = new JwtRequest(token).get();
                uiHandler.post(() -> successListener.onSuccess(jwt));
            } catch (YaLoginSdkError e) {
                uiHandler.post(() -> errorListener.onError(e));
            } catch (IOException e) {
                uiHandler.post(() -> errorListener.onError(new YaLoginSdkError(e)));
            }
            handlerThread.quit();
        });
    }

    public void onSaveInstanceState(@NonNull final Bundle state) {
        if (loginType != null) {
            state.putInt(STATE_LOGIN_TYPE, loginType.ordinal());
        }
    }

    public void onRestoreInstanceState(@Nullable final Bundle state) {
        if (state != null && state.containsKey(STATE_LOGIN_TYPE)) {
            loginType = LoginType.values()[state.getInt(STATE_LOGIN_TYPE)];
        }
    }
}
