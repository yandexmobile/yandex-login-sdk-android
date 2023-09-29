package com.yandex.authsdk.internal

import android.content.Context

internal class PreferencesHelper(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun restoreStateValue(): String? {
        return preferences.getString(KEY_STATE_VALUE, null)
    }

    fun saveStateValue(stateValue: String) {
        preferences.edit().putString(KEY_STATE_VALUE, stateValue).apply()
    }

    companion object {

        private const val PREFERENCES_NAME = "authsdk"

        private const val KEY_STATE_VALUE = "state_value"
    }
}
