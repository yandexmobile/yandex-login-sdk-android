package com.yandex.authsdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.yandex.authsdk.internal.Constants;
import com.yandex.authsdk.internal.Util;

import static com.yandex.authsdk.internal.Constants.HOST_PRODUCTION;

public class YandexAuthOptions implements Parcelable {

    @NonNull
    private final String clientId;

    private final boolean loggingEnabled;

    @Nullable
    private final Context context;

    @NonNull
    private final String oauthHost;

    /**
     * @deprecated Use {@link Builder} instead of constructor
     */
    @Deprecated
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
        this.context = context;

        this.oauthHost = Util.checkNotNull(app.metaData.getString(Constants.META_OAUTH_HOST));
    }

    @NonNull
    public String getClientId() {
        return clientId;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    @Nullable
    @Deprecated
    Context getContext() {
        return context;
    }

    @NonNull
    public String getOauthHost() {
        return oauthHost;
    }

    public boolean isTesting() {
        return !TextUtils.equals(oauthHost, HOST_PRODUCTION);
    }

    public static class Builder {

        private final Context context;

        private boolean loggingEnabled;

        public Builder(@NonNull final Context context) {
            this.context = context;
        }

        @NonNull
        public Builder enableLogging() {
            this.loggingEnabled = true;
            return this;
        }

        @NonNull
        public YandexAuthOptions build() {
            return new YandexAuthOptions(context, loggingEnabled);
        }
    }

    protected YandexAuthOptions(@NonNull final Parcel in) {
        clientId = in.readString();
        loggingEnabled = in.readByte() != 0;
        oauthHost = in.readString();
        context = null;
    }

    @Override
    public void writeToParcel(@NonNull final Parcel dest, final int flags) {
        dest.writeString(clientId);
        dest.writeByte((byte) (loggingEnabled ? 1 : 0));
        dest.writeString(oauthHost);
    }

    @Override
    public int describeContents() {
        return 0;
    }

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
}
