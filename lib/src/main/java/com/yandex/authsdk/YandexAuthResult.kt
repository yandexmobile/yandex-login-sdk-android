package com.yandex.authsdk

sealed interface YandexAuthResult {

    data class Success(val token: YandexAuthToken) : YandexAuthResult

    data class Failure(val exception: YandexAuthException) : YandexAuthResult

    object Cancelled : YandexAuthResult
}
