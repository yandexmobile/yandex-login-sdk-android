package com.yandex.authsdk.internal;

import androidx.annotation.NonNull;
import android.util.Log;

import com.yandex.authsdk.YandexAuthOptions;

public class Logger {

    public static void e(
            @NonNull final YandexAuthOptions options,
            @NonNull final String tag,
            @NonNull final String message,
            @NonNull final Throwable e
    ) {
        if (options.isLoggingEnabled()) {
            Log.e(tag, message, e);
        }
    }

    public static void d(
            @NonNull final YandexAuthOptions options,
            @NonNull final String tag,
            @NonNull final String message
    ) {
        if (options.isLoggingEnabled()) {
            Log.d(tag, message);
        }
    }
}
