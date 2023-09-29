package com.yandex.authsdk.internal

import com.yandex.authsdk.YandexAuthException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

internal class JwtRequest(private val token: String) {

    @Throws(IOException::class, YandexAuthException::class)
    fun get(): String {
        val url = URL(String.format(JWT_REQUEST_URL_FORMAT, token))
        val connection = url.openConnection() as HttpURLConnection
        if (connection.responseCode == RESPONSE_CODE_UNAUTHORIZED) {
            throw YandexAuthException(YandexAuthException.JWT_AUTHORIZATION_ERROR)
        }
        return try {
            readToString(connection.inputStream)
        } finally {
            connection.disconnect()
        }
    }

    companion object {

        const val JWT_REQUEST_URL_FORMAT = "https://login.yandex.ru/info?format=jwt&oauth_token=%s"

        const val RESPONSE_CODE_UNAUTHORIZED = 401

        @Throws(IOException::class)
        private fun readToString(stream: InputStream): String {
            val reader = BufferedReader(InputStreamReader(stream))
            return reader.use { r ->
                val total = StringBuilder()
                var line: String?
                while (r.readLine().also { line = it } != null) {
                    total.append(line).append('\n')
                }
                total.toString()
            }
        }
    }
}
