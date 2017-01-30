package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.yandex.yaloginsdk.LoginSdkConfig;
import com.yandex.yaloginsdk.FingerprintExtractor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.ACTION_YA_SDK_LOGIN;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.EXTRA_CLIENT_ID;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.EXTRA_SCOPES;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.FINGERPRINT;
import static com.yandex.yaloginsdk.YaLoginSdkConstants.AmConstants.META_SDK_VERSION;
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

    @Mock
    FingerprintExtractor extractor;

    @Mock
    PackageManager packageManager;

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void checkIsMatching_trueForMatching() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT, "package.name", new String[]{"wrong", FINGERPRINT});

        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isTrue();
    }

    @Test
    public void checkIsMatching_trueForMatchingWithBiggerSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT_BIGGER, "package.name", new String[]{"wrong", FINGERPRINT});

        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isTrue();
    }

    @Test
    public void checkIsMatching_falseOnWrongFingerprint() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_CORRECT, "package.name", new String[]{"wrong", "and_wrong"});
        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isFalse();
    }

    @Test
    public void checkIsMatching_falseOnWrongSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(SDK_VERSION_WRONG, "package.name", new String[]{FINGERPRINT});

        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isFalse();
    }

    @Test
    public void getIfPossible_returnsFirstValid() throws PackageManager.NameNotFoundException {
        ResolveInfo invalid1 = createResolveInfo(SDK_VERSION_CORRECT, "wrong1", null);
        ResolveInfo invalid2 = createResolveInfo(SDK_VERSION_WRONG, "wrong2", new String[]{FINGERPRINT});
        ResolveInfo valid1 = createResolveInfo(SDK_VERSION_CORRECT, "valid1", new String[]{FINGERPRINT});
        ResolveInfo valid2 = createResolveInfo(SDK_VERSION_CORRECT, "valid2", new String[]{FINGERPRINT});

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

    @NonNull
    private ResolveInfo createResolveInfo(int sdkVersion, @NonNull String packageName, @Nullable String[] fingerprints) throws PackageManager.NameNotFoundException {
        ResolveInfo info = mock(ResolveInfo.class);
        info.activityInfo = mock(ActivityInfo.class);
        info.activityInfo.packageName = packageName;

        ApplicationInfo appInfo = mock(ApplicationInfo.class);
        Bundle metadata = new Bundle(SDK_VERSION_CORRECT);
        metadata.putInt(META_SDK_VERSION, sdkVersion);
        appInfo.metaData = metadata;

        when(packageManager.getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_META_DATA)).thenReturn(appInfo);
        when(extractor.get(info.activityInfo.packageName, packageManager)).thenReturn(fingerprints);

        return info;
    }
}