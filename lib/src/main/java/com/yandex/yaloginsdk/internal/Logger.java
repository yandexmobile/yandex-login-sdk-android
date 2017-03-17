package com.yandex.yaloginsdk.internal;

import android.support.annotation.NonNull;
import android.util.Log;

import com.yandex.yaloginsdk.LoginSdkConfig;

public class Logger {

    public static void e(
            @NonNull final LoginSdkConfig config,
            @NonNull final String tag,
            @NonNull final String message,
            @NonNull final Throwable e
    ) {
        if (config.isLoggingEnabled()) {
            Log.e(tag, message, e);
        }
    }

    public static void d(
            @NonNull final LoginSdkConfig config,
            @NonNull final String tag,
            @NonNull final String message
    ) {
        if (config.isLoggingEnabled()) {
            Log.d(tag, message);
        }
    }
}
