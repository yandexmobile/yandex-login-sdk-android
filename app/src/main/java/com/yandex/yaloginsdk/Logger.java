package com.yandex.yaloginsdk;

import android.support.annotation.NonNull;
import android.util.Log;

class Logger {

    static void e(@NonNull String tag, @NonNull String message, @NonNull Throwable e) {
        Log.e(tag, message, e);
    }

    public static void d(@NonNull String tag, @NonNull String message) {
        Log.d(tag, message);
    }
}
