package com.yandex.authsdk.exceptions

import com.yandex.authsdk.YandexAuthException

class YandexAuthSecurityException(e: SecurityException) : YandexAuthException(SECURITY_ERROR)
