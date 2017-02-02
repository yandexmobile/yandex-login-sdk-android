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

    public static Token create(@NonNull String token, @NonNull String type, long expiresIn) {
        return new AutoValue_Token(token, type, expiresIn);
    }
}
