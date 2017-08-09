package com.yandex.yaloginsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.internal.Constants;
import com.yandex.yaloginsdk.internal.JwtRequest;
import com.yandex.yaloginsdk.internal.Logger;
import com.yandex.yaloginsdk.internal.LoginSdkActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class YandexAuthSdk {

    private static final String TAG = YandexAuthSdk.class.getSimpleName();

    @NonNull
    private final YandexAuthOptions options;

    public YandexAuthSdk(@NonNull final YandexAuthOptions options) {
        this.options = options;
    }

    @NonNull
    public Intent createLoginIntent(@NonNull final Context context, @Nullable final Set<String> scopes) {
        final Intent intent = new Intent(context, LoginSdkActivity.class);
        if (scopes != null) {
            intent.putExtra(Constants.EXTRA_SCOPES, new ArrayList<>(scopes));
        }
        intent.putExtra(Constants.EXTRA_OPTIONS, options);
        return intent;
    }

    @Nullable
    public YandexAuthToken extractToken(final int resultCode, @Nullable final Intent data) throws YandexAuthException {
        if (data == null || resultCode != Activity.RESULT_OK) {
            return null;
        }
        final YandexAuthException exception = (YandexAuthException) data.getSerializableExtra(Constants.EXTRA_ERROR);
        if (exception != null) {
            Logger.d(options, TAG, "Exception received");
            throw exception;
        }

        return data.getParcelableExtra(Constants.EXTRA_TOKEN);
    }

    @NonNull
    public String getJwt(@NonNull final YandexAuthToken token) throws YandexAuthException {
        try {
            return new JwtRequest(token.getValue()).get();
        } catch (final IOException e) {
            throw new YandexAuthException(e);
        }
    }
}
