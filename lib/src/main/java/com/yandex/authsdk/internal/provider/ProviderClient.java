package com.yandex.authsdk.internal.provider;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthAccount;
import com.yandex.authsdk.exceptions.YandexAuthInteractionException;
import com.yandex.authsdk.exceptions.YandexAuthSecurityException;

import java.util.ArrayList;
import java.util.List;

import static com.yandex.authsdk.internal.Util.checkNotNull;

public class ProviderClient {

    private static final String KEY_SUFFIX_UID_VALUE = "com.yandex.auth.UID_VALUE";

    private static final String KEY_SUFFIX_PRIMARY_DISPLAY_NAME = "com.yandex.auth.PRIMARY_DISPLAY_NAME";

    private static final String KEY_SUFFIX_SECONDARY_DISPLAY_NAME = "com.yandex.auth.SECONDARY_DISPLAY_NAME";

    private static final String KEY_SUFFIX_IS_AVATAR_EMPTY = "com.yandex.auth.IS_AVATAR_EMPTY";

    private static final String KEY_SUFFIX_AVATAR_URL = "com.yandex.auth.AVATAR_URL";

    private static final String KEY_ACCOUNTS_COUNT = "com.yandex.auth.ACCOUNTS_COUNT";

    private static final String ACCOUNT_KEY_PREFIX = "account-";

    private static final String SEPARATOR = "-";

    @NonNull
    private final Context context;

    @NonNull
    private final Uri uri;

    private final int sdkVersion;


    private enum Method {
        GetAccounts
    }


    ProviderClient(@NonNull final Context context, @NonNull final String packageName, final int sdkVersion) {
        this.context = context;
        this.uri = Uri.parse("content://com.yandex.passport.authsdk.provider." + packageName);
        this.sdkVersion = sdkVersion;
    }

    @NonNull
    public List<YandexAuthAccount> getAccounts()
            throws YandexAuthSecurityException, YandexAuthInteractionException {
        if (sdkVersion >= 2) {
            return accountsFromBundle(call(Method.GetAccounts, null, null));
        } else {
            throw new YandexAuthInteractionException("Method not supported");
        }
    }

    @NonNull
    private Bundle call(@NonNull final Method method, @Nullable final String arg, @Nullable final Bundle extras)
            throws YandexAuthSecurityException, YandexAuthInteractionException {
        final Bundle bundle;
        try {
            bundle = context.getContentResolver().call(this.uri, method.name(), arg, extras);
        } catch (final SecurityException e) {
            throw new YandexAuthSecurityException(e);
        }
        if (bundle == null) {
            throw new YandexAuthInteractionException("Unsuccessful request to content provider");
        }
        return bundle;
    }

    @NonNull
    private List<YandexAuthAccount> accountsFromBundle(@NonNull final Bundle bundle) {
        final List<YandexAuthAccount> accounts = new ArrayList<>();
        final int accountsCount = bundle.getInt(KEY_ACCOUNTS_COUNT);
        for (int i = 0; i < accountsCount; i++) {
            accounts.add(new YandexAuthAccount(
                    bundle.getLong(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_UID_VALUE),
                    checkNotNull(bundle.getString(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_PRIMARY_DISPLAY_NAME)),
                    bundle.getString(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_SECONDARY_DISPLAY_NAME),
                    bundle.getBoolean(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_IS_AVATAR_EMPTY),
                    bundle.getString(ACCOUNT_KEY_PREFIX + i + SEPARATOR + KEY_SUFFIX_AVATAR_URL)
            ));
        }
        return accounts;
    }
}
