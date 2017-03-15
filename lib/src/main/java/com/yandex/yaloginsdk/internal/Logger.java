package com.yandex.yaloginsdk.internal;

import android.support.annotation.NonNull;
import android.util.Log;

import com.yandex.yaloginsdk.BuildConfig;

public class Logger {

    public static void e(@NonNull final String tag, @NonNull final String message, @NonNull final Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, e);
        }
    }

    public static void d(@NonNull final String tag, @NonNull final String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }
}
