package com.yandex.yaloginsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yandex.yaloginsdk.internal.YaLoginSdkConstants;

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

    public LoginSdkConfig(@NonNull final String clientId, final boolean loggingEnabled) {
        this.clientId = clientId;
        this.loggingEnabled = loggingEnabled;
    }

    public LoginSdkConfig(@NonNull final Context context, final boolean loggingEnabled) {
        final ApplicationInfo app;
        try {
            app = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
        } catch (final PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        final String clientId = app.metaData.getString(YaLoginSdkConstants.EXTRA_CLIENT_ID);
        if (clientId == null) {
            throw new IllegalStateException(
                    String.format("Application should provide %s in AndroidManifest.xml",
                            YaLoginSdkConstants.EXTRA_CLIENT_ID));
        }
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
