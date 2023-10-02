package com.yandex.authsdk.internal

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import com.yandex.authsdk.YandexAuthException
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthToken
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment


@RunWith(RobolectricTestRunner::class)
class ExternalLoginHandlerTest {

    private val STATE = "test_state"
    private val options: YandexAuthOptions = mockk<YandexAuthOptions>().apply {
        every { clientId } returns "8719af4a012f4786a7a910582e741c8e"
        every { oauthHost } returns "oauth.yandex.com"
    }
    private val preferencesHelper = PreferencesHelper(RuntimeEnvironment.application)
    private val urlCreator = UrlCreator()
    private var loginHandler: ExternalLoginHandler = ExternalLoginHandler(preferencesHelper, { STATE }, urlCreator)

    @Before
    fun before() {
        val intent = Intent()
        intent.putExtra(Constants.EXTRA_LOGIN_OPTIONS, YandexAuthLoginOptions())
        intent.putExtra(Constants.EXTRA_OPTIONS, options)
        loginHandler.getUrl(intent)
    }

    @Test
    fun parseResult_shouldReturnError() {
        val data = Uri.parse("some://uri.com?with=query#error=error_message&state=$STATE")
        val result = loginHandler.parseResult(data)
        Assertions.assertThat(result.getSerializableExtra(Constants.EXTRA_ERROR)).isEqualTo(YandexAuthException("error_message"))
        Assertions.assertThat(result.getParcelableExtra<Parcelable>(Constants.EXTRA_TOKEN) as YandexAuthToken?).isNull()
    }

    @Test
    fun parseResult_shouldReturnToken() {
        val data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1&state=$STATE")
        val result = loginHandler.parseResult(data)
        Assertions.assertThat(result.getSerializableExtra(Constants.EXTRA_ERROR)).isNull()
        Assertions.assertThat(result.getParcelableExtra<Parcelable>(Constants.EXTRA_TOKEN) as YandexAuthToken?).isEqualTo(YandexAuthToken("token", 1))
    }

    @Test
    fun parseResult_shouldReturnSecurityErrorOnNullState() {
        val data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1")
        val result = loginHandler.parseResult(data)
        Assertions.assertThat(result.getSerializableExtra(Constants.EXTRA_ERROR)).isEqualTo(YandexAuthException(YandexAuthException.SECURITY_ERROR))
        Assertions.assertThat(result.getParcelableExtra<Parcelable>(Constants.EXTRA_TOKEN) as YandexAuthToken?).isNull()
    }

    @Test
    fun parseResult_shouldReturnSecurityErrorOnWrongState() {
        val data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1&state=wrong_state")
        val result = loginHandler.parseResult(data)
        Assertions.assertThat(result.getSerializableExtra(Constants.EXTRA_ERROR)).isEqualTo(YandexAuthException(YandexAuthException.SECURITY_ERROR))
        Assertions.assertThat(result.getParcelableExtra<Parcelable>(Constants.EXTRA_TOKEN) as YandexAuthToken?).isNull()
    }
}
