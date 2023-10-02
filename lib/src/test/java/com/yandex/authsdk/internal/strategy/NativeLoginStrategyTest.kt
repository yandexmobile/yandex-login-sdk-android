package com.yandex.authsdk.internal.strategy

import android.content.Intent
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NativeLoginStrategyTest {

    @Test
    fun tryExtractToken_shouldReturnToken() {
        val extractor = NativeLoginStrategy.ResultExtractor()
        val tokenData = Intent()
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN, "token")
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN_TYPE, "type")
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN_EXPIRES, 1L)
        assertThat(extractor.tryExtractToken(tokenData))
            .isEqualTo(YandexAuthToken("token", 1L))
    }

    @Test
    fun tryExtractToken_shouldReturnTokenIfNoExpire() {
        val extractor = NativeLoginStrategy.ResultExtractor()
        val tokenData = Intent()
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN, "token")
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN_TYPE, "type")
        assertThat(extractor.tryExtractToken(tokenData))
            .isEqualTo(YandexAuthToken("token", 0L))
    }

    @Test
    fun tryExtractToken_shouldReturnNullIfNoToken() {
        val extractor = NativeLoginStrategy.ResultExtractor()
        val tokenData = Intent()
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN_TYPE, "type")
        tokenData.putExtra(NativeLoginStrategy.EXTRA_OAUTH_TOKEN_EXPIRES, 1.0)
        assertThat(extractor.tryExtractToken(tokenData)).isNull()
    }

    @Test
    fun tryExtractError_shouldReturnNullIfNoError() {
        val extractor = NativeLoginStrategy.ResultExtractor()
        val errorData = Intent()
        assertThat(extractor.tryExtractError(errorData)).isNull()
    }

    @Test
    fun tryExtractError_shouldReturnConnectionErrorIfNoMessages() {
        val extractor = NativeLoginStrategy.ResultExtractor()
        val errorData = Intent()
        errorData.putExtra(NativeLoginStrategy.OAUTH_TOKEN_ERROR, true)
        assertThat(extractor.tryExtractError(errorData)).isEqualTo(
            YandexAuthException(
                YandexAuthException.CONNECTION_ERROR
            )
        )
    }

    @Test
    fun tryExtractError_shouldReturnConnectionErroWithMessages() {
        val extractor = NativeLoginStrategy.ResultExtractor()
        val errorData = Intent()
        errorData.putExtra(NativeLoginStrategy.OAUTH_TOKEN_ERROR, true)
        errorData.putExtra(
            NativeLoginStrategy.OAUTH_TOKEN_ERROR_MESSAGES,
            arrayOf("error.message", "one.more.error")
        )
        assertThat(extractor.tryExtractError(errorData))
            .isEqualTo(YandexAuthException(arrayOf("error.message", "one.more.error")))
    }
}
