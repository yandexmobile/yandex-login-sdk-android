package com.yandex.authsdk.internal.strategy;

import android.content.Intent;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.internal.strategy.NativeLoginStrategy.ResultExtractor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static com.yandex.authsdk.YandexAuthException.CONNECTION_ERROR;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.EXTRA_OAUTH_TOKEN;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.EXTRA_OAUTH_TOKEN_EXPIRES;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.EXTRA_OAUTH_TOKEN_TYPE;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.OAUTH_TOKEN_ERROR;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.OAUTH_TOKEN_ERROR_MESSAGES;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class NativeLoginStrategyTest {

    private static final ArrayList<String> SCOPE = new ArrayList<String>() {{
        add("scope");
    }};

    @Test
    public void tryExtractToken_shouldReturnToken() {
        ResultExtractor extractor = new ResultExtractor();
        Intent tokenData = new Intent();
        tokenData.putExtra(EXTRA_OAUTH_TOKEN, "token");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_TYPE, "type");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 1L);

        assertThat(extractor.tryExtractToken(tokenData)).isEqualTo(new YandexAuthToken("token", 1L));
    }

    @Test
    public void tryExtractToken_shouldReturnTokenIfNoExpire() {
        ResultExtractor extractor = new ResultExtractor();
        Intent tokenData = new Intent();
        tokenData.putExtra(EXTRA_OAUTH_TOKEN, "token");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_TYPE, "type");

        assertThat(extractor.tryExtractToken(tokenData)).isEqualTo(new YandexAuthToken("token", 0L));
    }

    @Test
    public void tryExtractToken_shouldReturnNullIfNoToken() {
        ResultExtractor extractor = new ResultExtractor();
        Intent tokenData = new Intent();
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_TYPE, "type");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 1d);

        assertThat(extractor.tryExtractToken(tokenData)).isNull();
    }

    @Test
    public void tryExtractError_shouldReturnNullIfNoError() {
        ResultExtractor extractor = new ResultExtractor();
        Intent errorData = new Intent();

        assertThat(extractor.tryExtractError(errorData)).isNull();
    }

    @Test
    public void tryExtractError_shouldReturnConnectionErrorIfNoMessages() {
        ResultExtractor extractor = new ResultExtractor();
        Intent errorData = new Intent();
        errorData.putExtra(OAUTH_TOKEN_ERROR, true);

        assertThat(extractor.tryExtractError(errorData)).isEqualTo(new YandexAuthException(CONNECTION_ERROR));
    }

    @Test
    public void tryExtractError_shouldReturnConnectionErroWithMessages() {
        ResultExtractor extractor = new ResultExtractor();
        Intent errorData = new Intent();
        errorData.putExtra(OAUTH_TOKEN_ERROR, true);
        errorData.putExtra(OAUTH_TOKEN_ERROR_MESSAGES, new String[]{"error.message", "one.more.error"});

        assertThat(extractor.tryExtractError(errorData)).isEqualTo(new YandexAuthException(new String[]{"error.message", "one.more.error"}));
    }
}
