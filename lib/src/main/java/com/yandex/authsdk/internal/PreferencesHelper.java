package com.yandex.authsdk.internal;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PreferencesHelper {

    private static final String PREFERENCES_NAME = "authsdk";

    private static final String KEY_STATE_VALUE = "state_value";

    @NonNull
    private final SharedPreferences preferences;

    public PreferencesHelper(@NonNull final Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    public String restoreStateValue() {
        return preferences.getString(KEY_STATE_VALUE, null);
    }

    public void saveStateValue(@NonNull final String stateValue) {
        preferences.edit().putString(KEY_STATE_VALUE, stateValue).apply();
    }
}
