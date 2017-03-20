package com.yandex.yaloginsdk;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public class LoginSdkConfig implements Parcelable {

    public static final Creator<LoginSdkConfig> CREATOR = new Creator<LoginSdkConfig>() {
        @Override
        @NonNull
        public LoginSdkConfig createFromParcel(@NonNull final Parcel in) {
            return new LoginSdkConfig(in);
        }

        @Override
        @NonNull
        public LoginSdkConfig[] newArray(final int size) {
            return new LoginSdkConfig[size];
        }
    };

    @NonNull
    private final String clientId;

    private final boolean loggingEnabled;

    public LoginSdkConfig(@NonNull String clientId, boolean loggingEnabled) {
        this.clientId = clientId;
        this.loggingEnabled = loggingEnabled;
    }

    protected LoginSdkConfig(@NonNull final Parcel in) {
        clientId = in.readString();
        loggingEnabled = in.readByte() != 0;
    }

    @NonNull
    public String clientId() {
        return clientId;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(clientId);
        dest.writeByte((byte) (loggingEnabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
