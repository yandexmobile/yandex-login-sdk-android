package com.yandex.yaloginsdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Token implements Parcelable {

    public static final Creator<Token> CREATOR = new Creator<Token>() {
        @Override
        public Token createFromParcel(@NonNull final Parcel in) {
            return new Token(in);
        }

        @Override
        public Token[] newArray(final int size) {
            return new Token[size];
        }
    };

    public static Token create(@NonNull final String token, @NonNull final String type, final long expiresIn) {
        return new Token(token, type, expiresIn);
    }

    @NonNull
    private final String token;

    @NonNull
    private final String type;

    private final long expiresIn;

    private Token(@NonNull final String token, @NonNull final String type, final long expiresIn) {
        this.token = token;
        this.type = type;
        this.expiresIn = expiresIn;
    }

    protected Token(@NonNull final Parcel in) {
        token = in.readString();
        type = in.readString();
        expiresIn = in.readLong();
    }

    @NonNull
    public String token() {
        return token;
    }

    @NonNull
    public String type() {
        return type;
    }

    public long expiresIn() {
        return expiresIn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel parcel, final int i) {
        parcel.writeString(token);
        parcel.writeString(type);
        parcel.writeLong(expiresIn);
    }

    @Override
    @NonNull
    public String toString() {
        return "Token{" +
                "token='" + token + '\'' +
                ", type='" + type + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Token token1 = (Token) o;

        if (expiresIn != token1.expiresIn) return false;
        if (!token.equals(token1.token)) return false;
        return type.equals(token1.type);

    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (int) (expiresIn ^ (expiresIn >>> 32));
        return result;
    }
}
