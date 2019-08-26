package com.yandex.authsdk.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class HostUtilTest {
    @Test
    public void testGetLocalizedHost() {
        assertThat(HostUtil.getLocalizedHost("oauth.yandex.ru", Locale.ENGLISH))
                .isEqualTo("oauth.yandex.com");

        assertThat(HostUtil.getLocalizedHost("oauth.yandex.ru", new Locale("ru")))
                .isEqualTo("oauth.yandex.ru");

        assertThat(HostUtil.getLocalizedHost("oauth.yandex.ru", new Locale("tr")))
                .isEqualTo("oauth.yandex.com.tr");
    }
}
