package com.yandex.yaloginsdk;

import android.app.Application;
import android.support.annotation.NonNull;

public class LoginSdkConfig {

    @NonNull
    private final String clientId;

    @NonNull
    private final Application applicationContext;

    public LoginSdkConfig(@NonNull String clientId, @NonNull Application applicationContext) {
        this.clientId = clientId;
        this.applicationContext = applicationContext;
    }

    @NonNull
    public String clientId() {
        return clientId;
    }

    @NonNull
    public Application applicationContext() {
        return applicationContext;
    }
}
