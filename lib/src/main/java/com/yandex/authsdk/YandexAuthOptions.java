package com.yandex.authsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yandex.authsdk.internal.Constants;

public class YandexAuthOptions implements Parcelable {

    public static final Creator<YandexAuthOptions> CREATOR = new Creator<YandexAuthOptions>() {
        @Override
        @NonNull
        public YandexAuthOptions createFromParcel(@NonNull final Parcel in) {
            return new YandexAuthOptions(in);
        }

        @Override
        @NonNull
        public YandexAuthOptions[] newArray(final int size) {
            return new YandexAuthOptions[size];
        }
    };

    @NonNull
    private final String clientId;

    private final boolean loggingEnabled;

    public YandexAuthOptions(@NonNull final Context context, final boolean loggingEnabled) {
        final ApplicationInfo app;
        try {
            app = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (final PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        final String clientId = app.metaData.getString(Constants.META_CLIENT_ID);
        if (clientId == null) {
            throw new IllegalStateException(
                    String.format("Application should provide %s in AndroidManifest.xml",
                            Constants.META_CLIENT_ID));
        }
        this.clientId = clientId;
        this.loggingEnabled = loggingEnabled;
    }

    protected YandexAuthOptions(@NonNull final Parcel in) {
        clientId = in.readString();
        loggingEnabled = in.readByte() != 0;
    }

    @NonNull
    public String getClientId() {
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
