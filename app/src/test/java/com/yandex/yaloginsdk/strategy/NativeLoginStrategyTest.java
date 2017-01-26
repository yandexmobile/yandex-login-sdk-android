package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.FingerprintExtractor;
import com.yandex.yaloginsdk.Token;
import com.yandex.yaloginsdk.YaLoginSdkError;
import com.yandex.yaloginsdk.strategy.NativeLoginStrategy.ResultExtractor;
import com.yandex.yaloginsdk.LoginSdkConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.ACTION_YA_SDK_LOGIN;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_CLIENT_ID;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_OAUTH_TOKEN;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_OAUTH_TOKEN_EXPIRES;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_OAUTH_TOKEN_TYPE;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.EXTRA_SCOPES;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.FINGERPRINT;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.META_AM_VERSION;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.META_SDK_VERSION;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.OAUTH_TOKEN_ERROR;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.OAUTH_TOKEN_ERROR_MESSAGES;
import static com.yandex.yaloginsdk.YaLoginSdkError.CONNECTION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void findBest_returnsMatching() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "package.name", new String[]{"wrong", FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(toList(info), packageManager, extractor)).isEqualTo(info);
    }

    @Test
    public void findBest_returnsMatchingWithBiggerSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT_BIGGER, AM_VERSION, "package.name", new String[]{"wrong", FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(toList(info), packageManager, extractor)).isEqualTo(info);
    }

    @Test
    public void findBest_nullOnWrongFingerprint() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "package.name", new String[]{"wrong", "and_wrong"});
        assertThat(NativeLoginStrategy.findBest(toList(info), packageManager, extractor)).isNull();
    }

    @Test
    public void findBest_nullOnWrongSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_WRONG, AM_VERSION, "package.name", new String[]{FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(toList(info), packageManager, extractor)).isNull();
    }

    @Test
    public void findBest_returnsBiggerAmVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info1 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "package.name1", new String[]{FINGERPRINT});
        ResolveInfo info2 = createResolveInfo(SDK_VERSION_CORRECT_BIGGER, AM_VERSION + 1, "package.name2", new String[]{FINGERPRINT});
        ResolveInfo info3 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION + 2, "package.name3", new String[]{FINGERPRINT});
        assertThat(NativeLoginStrategy.findBest(toList(info1, info2, info3), packageManager, extractor)).isEqualTo(info3);
    }

    @Test
    public void getIfPossible_returnsFirstValid() throws PackageManager.NameNotFoundException {
        ResolveInfo invalid1 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "wrong1", null);
        ResolveInfo invalid2 = createResolveInfo(SDK_VERSION_WRONG, AM_VERSION, "wrong2", new String[]{FINGERPRINT});
        ResolveInfo valid1 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "valid1", new String[]{FINGERPRINT});
        ResolveInfo valid2 = createResolveInfo(SDK_VERSION_CORRECT, AM_VERSION, "valid2", new String[]{FINGERPRINT});

        //noinspection WrongConstant
        when(packageManager.queryIntentActivities(any(), eq(PackageManager.MATCH_DEFAULT_ONLY)))
                .thenReturn(Arrays.asList(invalid1, invalid2, valid1, valid2));
        LoginStrategy loginStrategy = NativeLoginStrategy.getIfPossible(packageManager, extractor);
        assert loginStrategy != null;

        Intent expectedLoginIntent = new Intent(ACTION_YA_SDK_LOGIN);
        expectedLoginIntent.setPackage(valid1.activityInfo.packageName);
        expectedLoginIntent.putStringArrayListExtra(EXTRA_SCOPES, SCOPE);
        expectedLoginIntent.putExtra(EXTRA_CLIENT_ID, CLIENT_ID);

        LoginSdkConfig config = LoginSdkConfig.builder()
                .clientId(CLIENT_ID)
                .applicationContext(RuntimeEnvironment.application)
                .build();
        final Intent actualLoginIntent = loginStrategy.getLoginIntent(config, new HashSet<>(SCOPE));

        assertThat(actualLoginIntent.getAction()).isEqualTo(expectedLoginIntent.getAction());
        assertThat(actualLoginIntent.getPackage()).isEqualTo(expectedLoginIntent.getPackage());
        assertThat(actualLoginIntent.getStringExtra(EXTRA_CLIENT_ID)).isEqualTo(expectedLoginIntent.getStringExtra(EXTRA_CLIENT_ID));
        assertThat(actualLoginIntent.getStringArrayListExtra(EXTRA_SCOPES)).isEqualTo(expectedLoginIntent.getStringArrayListExtra(EXTRA_SCOPES));
    }

    @Test
    public void tryExtractToken_shouldReturnToken() {
        ResultExtractor extractor = new ResultExtractor();
        Intent tokenData = new Intent();
        tokenData.putExtra(EXTRA_OAUTH_TOKEN, "token");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_TYPE, "type");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_EXPIRES, 1L);

        assertThat(extractor.tryExtractToken(tokenData)).isEqualTo(Token.create("token", "type", 1L));
    }

    @Test
    public void tryExtractToken_shouldReturnTokenIfNoExpire() {
        ResultExtractor extractor = new ResultExtractor();
        Intent tokenData = new Intent();
        tokenData.putExtra(EXTRA_OAUTH_TOKEN, "token");
        tokenData.putExtra(EXTRA_OAUTH_TOKEN_TYPE, "type");

        assertThat(extractor.tryExtractToken(tokenData)).isEqualTo(Token.create("token", "type", 0L));
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
    public void tryExtractToken_shouldReturnNullIfNoType() {
        ResultExtractor extractor = new ResultExtractor();
        Intent tokenData = new Intent();
        tokenData.putExtra(EXTRA_OAUTH_TOKEN, "token");
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

        assertThat(extractor.tryExtractError(errorData)).isEqualTo(new YaLoginSdkError(CONNECTION_ERROR));
    }

    @Test
    public void tryExtractError_shouldReturnConnectionErroWithMessages() {
        ResultExtractor extractor = new ResultExtractor();
        Intent errorData = new Intent();
        errorData.putExtra(OAUTH_TOKEN_ERROR, true);
        errorData.putExtra(OAUTH_TOKEN_ERROR_MESSAGES, new String[]{"error.message", "one.more.error"});

        assertThat(extractor.tryExtractError(errorData)).isEqualTo(new YaLoginSdkError(new String[]{"error.message", "one.more.error"}));
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
        when(extractor.get(info.activityInfo.packageName, packageManager)).thenReturn(fingerprints);

        return info;
    }

    @NonNull
    private List<ResolveInfo> toList(@NonNull ResolveInfo... infos) {
        return Arrays.asList(infos);
    }
}