package com.yandex.yaloginsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.internal.JwtRequest;
import com.yandex.yaloginsdk.internal.Logger;
import com.yandex.yaloginsdk.internal.LoginSdkActivity;
import com.yandex.yaloginsdk.internal.YaLoginSdkConstants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class YaLoginSdk {

    private static final String TAG = YaLoginSdk.class.getSimpleName();

    @NonNull
    private final LoginSdkConfig config;

    @NonNull
    public static YaLoginSdk get(@NonNull final LoginSdkConfig config) {
        return new YaLoginSdk(config);
    }

    private YaLoginSdk(@NonNull final LoginSdkConfig config) {
        this.config = config;
    }

    @NonNull
    public Intent createLoginIntent(@NonNull final Context context, @Nullable final Set<String> scopes) {
        final Intent intent = new Intent(context, LoginSdkActivity.class);
        if (scopes != null) {
            intent.putExtra(YaLoginSdkConstants.EXTRA_SCOPES, new ArrayList<>(scopes));
        }
        intent.putExtra(YaLoginSdkConstants.EXTRA_CONFIG, config);
        return intent;
    }

    public boolean onActivityResult(
            final int resultCode,
            @Nullable final Intent data,
            @NonNull final SuccessListener<Token> successListener,
            @NonNull final ErrorListener errorListener
    ) {
        // TODO add cancel listener?
        if (data == null || resultCode != Activity.RESULT_OK) {
            return false;
        }

        final Token token = data.getParcelableExtra(YaLoginSdkConstants.EXTRA_TOKEN);
        if (token != null) {
            Logger.d(config, TAG, "Token received");
            successListener.onSuccess(token);
            return true;
        }

        final YaLoginSdkError error = (YaLoginSdkError) data.getSerializableExtra(YaLoginSdkConstants.EXTRA_ERROR);
        if (error != null) {
            Logger.d(config, TAG, "Error received");
            errorListener.onError(error);
            return true;
        }

        Logger.d(config, TAG, "Nothing received");
        return false;
    }

    public String getJwt(String token) throws YaLoginSdkError {
        try {
            return new JwtRequest(token).get();
        } catch (IOException e) {
            throw new YaLoginSdkError(e);
        }
    }
}
