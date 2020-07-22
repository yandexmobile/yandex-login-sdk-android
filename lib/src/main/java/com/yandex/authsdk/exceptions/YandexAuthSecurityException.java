package com.yandex.authsdk.exceptions;


import androidx.annotation.NonNull;

import com.yandex.authsdk.YandexAuthException;

public class YandexAuthSecurityException extends YandexAuthException {
    public YandexAuthSecurityException(@NonNull final SecurityException e) {
        super(YandexAuthException.SECURITY_ERROR);
    }
}
