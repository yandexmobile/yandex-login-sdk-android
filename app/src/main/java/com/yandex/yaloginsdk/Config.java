package com.yandex.yaloginsdk;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Config {

    @NonNull
    public abstract String clientId();

    @NonNull
    public abstract Application applicationContext();

    @NonNull
    public static Config.Builder builder() {
        return new AutoValue_Config.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        @NonNull
        public abstract Builder clientId(@NonNull String clientId);

        @NonNull
        public abstract Builder applicationContext(@NonNull Application applicationContext);

        @NonNull
        public abstract Config build();
    }
}
