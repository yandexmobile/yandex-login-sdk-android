package com.yandex.authsdk;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class YandexAuthToken implements Parcelable {

    public static final Creator<YandexAuthToken> CREATOR = new Creator<YandexAuthToken>() {
        @Override
        public YandexAuthToken createFromParcel(@NonNull final Parcel in) {
            return new YandexAuthToken(in);
        }

        @Override
        public YandexAuthToken[] newArray(final int size) {
            return new YandexAuthToken[size];
        }
    };

    @NonNull
    private final String value;

    private final long expiresIn;

    public YandexAuthToken(@NonNull final String value, final long expiresIn) {
        this.value = value;
        this.expiresIn = expiresIn;
    }

    protected YandexAuthToken(@NonNull final Parcel in) {
        value = in.readString();
        expiresIn = in.readLong();
    }

    @NonNull
    public String getValue() {
        return value;
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
        parcel.writeString(value);
        parcel.writeLong(expiresIn);
    }

    @Override
    @NonNull
    public String toString() {
        return YandexAuthToken.class.getSimpleName() + "{" +
                "token='" + value + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        YandexAuthToken that = (YandexAuthToken) o;

        if (expiresIn != that.expiresIn) return false;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (int) (expiresIn ^ (expiresIn >>> 32));
        return result;
    }
}
