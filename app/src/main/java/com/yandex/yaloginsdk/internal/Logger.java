package com.yandex.yaloginsdk.internal;

import android.support.annotation.NonNull;
import android.util.Log;

public class Logger {

    public static void e(@NonNull final String tag, @NonNull final String message, @NonNull final Throwable e) {
        Log.e(tag, message, e);
    }

    public static void d(@NonNull final String tag, @NonNull final String message) {
        Log.d(tag, message);
    }
}
