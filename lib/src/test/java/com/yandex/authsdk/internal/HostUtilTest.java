package com.yandex.authsdk.internal;

import static com.yandex.authsdk.internal.HostUtilKt.getLocalizedHost;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
public class HostUtilTest {
    @Test
    public void testGetLocalizedHost() {
        assertThat(getLocalizedHost("oauth.yandex.ru", Locale.ENGLISH))
                .isEqualTo("oauth.yandex.com");

        assertThat(getLocalizedHost("oauth.yandex.ru", new Locale("ru")))
                .isEqualTo("oauth.yandex.ru");

        assertThat(getLocalizedHost("oauth.yandex.ru", new Locale("tr")))
                .isEqualTo("oauth.yandex.com.tr");
    }
}
