package com.yandex.yaloginsdk;

import android.support.annotation.NonNull;

public interface LoginErrorListener {

    void onError(@NonNull YaLoginSdkError error);
}
