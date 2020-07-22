package com.yandex.authsdk.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Util {

    private static final String TAG = Util.class.getSimpleName();

    @NonNull
    public static <T> T checkNotNull(@Nullable T reference) {
        if (reference == null) {
            throw new NullPointerException("Argument should not be null");
        }
        return reference;
    }
}
