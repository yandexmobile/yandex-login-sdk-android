package com.yandex.authsdk.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.yandex.authsdk.YandexAuthException.SECURITY_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_ERROR;
import static com.yandex.authsdk.internal.Constants.EXTRA_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class ExternalLoginHandlerTest {

    private static final String STATE = "test_state";

    @SuppressWarnings("NullableProblems") // before
    @NonNull
    private ExternalLoginHandler loginHandler;

    @NonNull
    private final YandexAuthOptions options = mock(YandexAuthOptions.class);

    @Before
    public void before() {
        loginHandler = new ExternalLoginHandler(options, () -> STATE);
        loginHandler.getUrl("clientId");
    }

    @Test
    public void parseResult_shouldReturnError() {
        final Uri data = Uri.parse("some://uri.com?with=query#error=error_message&state=" + STATE);
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isEqualTo(new YandexAuthException("error_message"));
        assertThat((YandexAuthToken) result.getParcelableExtra(EXTRA_TOKEN)).isNull();
    }

    @Test
    public void parseResult_shouldReturnToken() {
        final Uri data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1&state=" + STATE);
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isNull();
        assertThat((YandexAuthToken) result.getParcelableExtra(EXTRA_TOKEN)).isEqualTo(new YandexAuthToken("token", 1));
    }

    @Test
    public void parseResult_shouldReturnSecurityErrorOnNullState() {
        final Uri data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1");
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isEqualTo(new YandexAuthException(SECURITY_ERROR));
        assertThat((YandexAuthToken) result.getParcelableExtra(EXTRA_TOKEN)).isNull();
    }

    @Test
    public void parseResult_shouldReturnSecurityErrorOnWrongState() {
        final Uri data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1&state=wrong_state");
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isEqualTo(new YandexAuthException(SECURITY_ERROR));
        assertThat((YandexAuthToken) result.getParcelableExtra(EXTRA_TOKEN)).isNull();
    }

    @Test
    public void shouldSaveState() {
        Bundle savedState = new Bundle(1);
        loginHandler.saveState(savedState);

        ExternalLoginHandler newHandler = new ExternalLoginHandler(options, () -> "some_new_state");
        newHandler.restoreState(savedState);
        assertThat(newHandler.state).isEqualTo(STATE);
    }
}
