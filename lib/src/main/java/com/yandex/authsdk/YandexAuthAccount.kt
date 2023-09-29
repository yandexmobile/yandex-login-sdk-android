package com.yandex.authsdk

data class YandexAuthAccount(
    val uid: Long,
    val primaryDisplayName: String,
    val secondaryDisplayName: String?,
    val isAvatarEmpty: Boolean,
    val avatarUrl: String?
)
