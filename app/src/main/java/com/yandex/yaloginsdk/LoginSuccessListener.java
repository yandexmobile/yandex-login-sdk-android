package com.yandex.yaloginsdk;

import android.support.annotation.NonNull;

public interface LoginSuccessListener {

    void onLoggedIn(@NonNull Token token);
}
