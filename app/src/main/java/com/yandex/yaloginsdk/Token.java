package com.yandex.yaloginsdk;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Token implements Parcelable {

    @NonNull
    public abstract String token();

    @NonNull
    public abstract String type();

    public abstract long expiresIn();

    public static Token create(@NonNull final String token, @NonNull final String type, final long expiresIn) {
        return new AutoValue_Token(token, type, expiresIn);
    }
}
