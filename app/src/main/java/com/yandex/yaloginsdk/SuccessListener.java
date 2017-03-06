package com.yandex.yaloginsdk;

import android.support.annotation.NonNull;

public interface SuccessListener<T> {

    void onSuccess(@NonNull T result);
}
