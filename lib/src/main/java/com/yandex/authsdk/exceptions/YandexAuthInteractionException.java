package com.yandex.authsdk.exceptions;


import androidx.annotation.NonNull;

import com.yandex.authsdk.YandexAuthException;

public class YandexAuthInteractionException extends YandexAuthException {
    public YandexAuthInteractionException(@NonNull final String message) {
        super(message);
    }
}
