package com.yandex.authsdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YandexAuthToken(
    val value: String,
    val expiresIn: Long,
) : Parcelable {

    override fun toString(): String {
        return YandexAuthToken::class.java.simpleName + "{" +
            "token='" + value + '\'' +
            ", expiresIn=" + expiresIn +
            '}'
    }
}
