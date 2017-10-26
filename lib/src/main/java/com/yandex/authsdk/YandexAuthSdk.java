package com.yandex.authsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.yandex.authsdk.exceptions.YandexAuthInteractionException;
import com.yandex.authsdk.exceptions.YandexAuthSecurityException;
import com.yandex.authsdk.internal.AuthSdkActivity;
import com.yandex.authsdk.internal.Constants;
import com.yandex.authsdk.internal.JwtRequest;
import com.yandex.authsdk.internal.Logger;
import com.yandex.authsdk.internal.PackageManagerHelper;
import com.yandex.authsdk.internal.provider.ProviderClient;
import com.yandex.authsdk.internal.provider.ProviderClientResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class YandexAuthSdk {

    private static final String TAG = YandexAuthSdk.class.getSimpleName();

    @NonNull
    private final YandexAuthOptions options;

    @Nullable
    private final ProviderClient providerClient;

    @Deprecated
    public YandexAuthSdk(@NonNull final YandexAuthOptions options) {
        //noinspection ConstantConditions,deprecation
        this(options.getContext(), options);
    }

    public YandexAuthSdk(@NonNull final Context context, @NonNull final YandexAuthOptions options) {
        this.options = options;
        this.providerClient = new ProviderClientResolver(
                new PackageManagerHelper(context.getPackageManager(), options)
        ).createProviderClient(context);
    }

    @NonNull
    public Intent createLoginIntent(@NonNull final Context context, @Nullable final Set<String> scopes) {
        final Intent intent = new Intent(context, AuthSdkActivity.class);
        if (scopes != null) {
            intent.putExtra(Constants.EXTRA_SCOPES, new ArrayList<>(scopes));
        }

        intent.putExtra(Constants.EXTRA_OPTIONS, options);
        return intent;
    }

    @NonNull
    public Intent createLoginIntent(@NonNull final Context context, @Nullable final Set<String> scopes, final long uid) {
        final Intent intent = createLoginIntent(context, scopes);
        intent.putExtra(Constants.EXTRA_UID_VALUE, uid);
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
    @WorkerThread
    public String getJwt(@NonNull final YandexAuthToken token) throws YandexAuthException {
        try {
            return new JwtRequest(token.getValue()).get();
        } catch (final IOException e) {
            throw new YandexAuthException(e);
        }
    }

    @NonNull
    @WorkerThread
    public List<YandexAuthAccount> getAccounts()
            throws YandexAuthSecurityException, YandexAuthInteractionException {
        if (providerClient == null) {
            throw new YandexAuthInteractionException("Yandex AuthSDK provider not found");
        }
        return providerClient.getAccounts();
    }
}
