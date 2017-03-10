package com.yandex.yaloginsdk;

import android.support.annotation.NonNull;

public interface ErrorListener {

    void onError(@NonNull final YaLoginSdkError error);
}
