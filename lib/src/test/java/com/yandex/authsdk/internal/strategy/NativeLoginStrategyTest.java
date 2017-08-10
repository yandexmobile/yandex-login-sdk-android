package com.yandex.authsdk.internal.strategy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.authsdk.YandexAuthException;
import com.yandex.authsdk.YandexAuthOptions;
import com.yandex.authsdk.YandexAuthToken;
import com.yandex.authsdk.internal.FingerprintExtractor;
import com.yandex.authsdk.internal.strategy.NativeLoginStrategy.ResultExtractor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.yandex.authsdk.YandexAuthException.CONNECTION_ERROR;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.EXTRA_OAUTH_TOKEN;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.EXTRA_OAUTH_TOKEN_EXPIRES;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.EXTRA_OAUTH_TOKEN_TYPE;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.FINGERPRINT;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.META_AM_VERSION;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.META_SDK_VERSION;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.OAUTH_TOKEN_ERROR;
import static com.yandex.authsdk.internal.strategy.NativeLoginStrategy.OAUTH_TOKEN_ERROR_MESSAGES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class NativeLoginStrategyTest {

    private static final ArrayList<String> SCOPE = new ArrayList<String>() {{
        add("scope");
    }};

    private static final String CLIENT_ID = "clientId";

    private static final int SDK_VERSION_CORRECT = 1;

    private static final int SDK_VERSION_CORRECT_BIGGER = 2;

    private static final int SDK_VERSION_WRONG = 0;

    private static final float AM_VERSION = 1f;

    @Mock
    FingerprintExtractor extractor;

    @Mock
    PackageManager packageManager;

    @NonNull
    private final YandexAuthOptions options = new YandexAuthOptions("client_id", true);

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void findBest_returnsMatching() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "package.name", new String[]{"wrong", FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(options, toList(info), packageManager, extractor)).isEqualTo(info);
    }

    @Test
    public void findBest_returnsMatchingWithBiggerSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT_BIGGER, AM_VERSION, "package.name", new String[]{"wrong", FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(options, toList(info), packageManager, extractor)).isEqualTo(info);
    }

    @Test
    public void findBest_nullOnWrongFingerprint() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "package.name", new String[]{"wrong", "and_wrong"});
        assertThat(NativeLoginStrategy.findBest(options, toList(info), packageManager, extractor)).isNull();
    }

    @Test
    public void findBest_nullOnWrongSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_WRONG, AM_VERSION, "package.name", new String[]{FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(options, toList(info), packageManager, extractor)).isNull();
    }

    @Test
    public void findBest_returnsBiggerAmVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info1 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "package.name1", new String[]{FINGERPRINT});
        ResolveInfo info2 = createResolveInfo(SDK_VERSION_CORRECT_BIGGER, AM_VERSION + 1, "package.name2", new String[]{FINGERPRINT});
        ResolveInfo info3 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION + 2, "package.name3", new String[]{FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(options, toList(info1, info2, info3), packageManager, extractor)).isEqualTo(info3);
    }

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

    @NonNull
    private ResolveInfo createResolveInfo(int sdkVersion, float amVersion, @NonNull String packageName, @Nullable String[] fingerprints) throws PackageManager.NameNotFoundException {
        ResolveInfo info = mock(ResolveInfo.class);
        info.activityInfo = mock(ActivityInfo.class);
        info.activityInfo.packageName = packageName;

        ApplicationInfo appInfo = mock(ApplicationInfo.class);
        Bundle metadata = new Bundle(SDK_VERSION_CORRECT);
        metadata.putInt(META_SDK_VERSION, sdkVersion);
        metadata.putFloat(META_AM_VERSION, amVersion);
        appInfo.metaData = metadata;

        when(packageManager.getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_META_DATA)).thenReturn(appInfo);
        when(extractor.get(info.activityInfo.packageName, packageManager, options)).thenReturn(fingerprints);

        return info;
    }

    @NonNull
    private List<ResolveInfo> toList(@NonNull ResolveInfo... infos) {
        return Arrays.asList(infos);
    }
}
