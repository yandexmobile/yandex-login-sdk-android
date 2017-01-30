package com.yandex.yaloginsdk;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LoginSdkConfig {

    @NonNull
    public abstract String clientId();

    @NonNull
    public abstract Application applicationContext();

    @NonNull
    public static LoginSdkConfig.Builder builder() {
        return new AutoValue_LoginSdkConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        @NonNull
        public abstract Builder clientId(@NonNull String clientId);

        @NonNull
        public abstract Builder applicationContext(@NonNull Application applicationContext);

        @NonNull
        public abstract LoginSdkConfig build();
    }
}
