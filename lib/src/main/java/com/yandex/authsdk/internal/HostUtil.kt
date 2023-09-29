package com.yandex.authsdk.internal

import java.util.Locale

private val LANGUAGE_TO_TLD: Map<String, String> = mapOf(
    "be" to "by", // Belarusian
    "tr" to "com.tr", // Turkish
    "be" to "by", // Belarusian
    "tr" to "com.tr", // Turkish
    "kk" to "kz", // Kazakh
    "et" to "ru", // Estonian
    "hy" to "ru", // Armenian
    "ka" to "ru", // Georgian
    "ru" to "ru", // Russian
    "uk" to "ua", // Ukrainian
)

private const val DEFAULT_TLD = "com"

/**
 * @param baseHost - host ended with ".ru"
 */
fun getLocalizedHost(baseHost: String, locale: Locale): String {
    var tld = LANGUAGE_TO_TLD[locale.language]
    if (tld == null) {
        tld = DEFAULT_TLD
    }
    return baseHost.replace("ru$".toRegex(), tld)
}
