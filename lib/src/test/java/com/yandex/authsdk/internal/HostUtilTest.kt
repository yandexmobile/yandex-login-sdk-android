package com.yandex.authsdk.internal

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class HostUtilTest {

    @Test
    fun testGetLocalizedHost() {
        assertThat(getLocalizedHost("oauth.yandex.ru", Locale.ENGLISH))
            .isEqualTo("oauth.yandex.com")
        assertThat(getLocalizedHost("oauth.yandex.ru", Locale("ru")))
            .isEqualTo("oauth.yandex.ru")
        assertThat(getLocalizedHost("oauth.yandex.ru", Locale("tr")))
            .isEqualTo("oauth.yandex.com.tr")
    }
}
