package com.yandex.yaloginsdk.internal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.yandex.yaloginsdk.YaLoginSdkError.SECURITY_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_ERROR;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ExternalLoginHandlerTest {

    private static final String STATE = "test_state";

    @SuppressWarnings("NullableProblems") // before
    @NonNull
    private ExternalLoginHandler loginHandler;

    @Before
    public void before() {
        loginHandler = new ExternalLoginHandler(() -> STATE);
        loginHandler.getUrl("clientId");
    }

    @Test
    public void parseResult_shouldReturnError() {
        final Uri data = Uri.parse("some://uri.com?with=query#error=error_message&state=" + STATE);
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isEqualTo(new YaLoginSdkError("error_message"));
        assertThat((Token) result.getParcelableExtra(EXTRA_TOKEN)).isNull();
    }

    @Test
    public void parseResult_shouldReturnToken() {
        final Uri data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1&state=" + STATE);
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isNull();
        assertThat((Token) result.getParcelableExtra(EXTRA_TOKEN)).isEqualTo(Token.create("token", "type", 1));
    }

    @Test
    public void parseResult_shouldReturnSecurityErrorOnNullState() {
        final Uri data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1");
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isEqualTo(new YaLoginSdkError(SECURITY_ERROR));
        assertThat((Token) result.getParcelableExtra(EXTRA_TOKEN)).isNull();
    }

    @Test
    public void parseResult_shouldReturnSecurityErrorOnWrongState() {
        final Uri data = Uri.parse("some://uri.com?with=query#access_token=token&token_type=type&expires_in=1&state=wrong_state");
        final Intent result = loginHandler.parseResult(data);
        assertThat(result.getSerializableExtra(EXTRA_ERROR)).isEqualTo(new YaLoginSdkError(SECURITY_ERROR));
        assertThat((Token) result.getParcelableExtra(EXTRA_TOKEN)).isNull();
    }

    @Test
    public void shouldSaveState() {
        Bundle savedState = new Bundle(1);
        loginHandler.saveState(savedState);

        ExternalLoginHandler newHandler = new ExternalLoginHandler(() -> "some_new_state");
        newHandler.restoreState(savedState);
        assertThat(newHandler.state).isEqualTo(STATE);
    }
}