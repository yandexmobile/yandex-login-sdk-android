package com.yandex.yaloginsdk.strategy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.yandex.yaloginsdk.Config;
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
        ResolveInfo info = resolveInfo(1, "package.name", new String[]{"wrong", FINGERPRINT});

        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isTrue();
    }

    @Test
    public void checkIsMatching_falseOnWrongFingerprint() throws PackageManager.NameNotFoundException {
        ResolveInfo info = resolveInfo(1, "package.name", new String[]{"wrong", "and_wrong"});
        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isFalse();
    }

    @Test
    public void checkIsMatching_falseOnWrongSdkVersion() throws PackageManager.NameNotFoundException {
        ResolveInfo info = resolveInfo(2, "package.name", new String[]{FINGERPRINT});

        assertThat(NativeLoginStrategy.checkIsMatching(info, packageManager, extractor)).isFalse();
    }

    @Test
    public void getIfPossible_returnsFirstValid() throws PackageManager.NameNotFoundException {
        ResolveInfo invalid1 = resolveInfo(1, "wrong1", null);
        ResolveInfo invalid2 = resolveInfo(2, "wrong2", new String[]{FINGERPRINT});
        ResolveInfo valid1 = resolveInfo(1, "valid1", new String[]{FINGERPRINT});
        ResolveInfo valid2 = resolveInfo(1, "valid2", new String[]{FINGERPRINT});

        //noinspection WrongConstant
        when(packageManager.queryIntentActivities(any(), eq(PackageManager.MATCH_DEFAULT_ONLY)))
                .thenReturn(Arrays.asList(invalid1, invalid2, valid1, valid2));
        LoginStrategy loginStrategy = NativeLoginStrategy.getIfPossible(packageManager, extractor);

        Intent expectedLoginIntent = new Intent(ACTION_YA_SDK_LOGIN);
        expectedLoginIntent.setPackage(valid1.activityInfo.packageName);
        expectedLoginIntent.putStringArrayListExtra(EXTRA_SCOPES, SCOPE);
        expectedLoginIntent.putExtra(EXTRA_CLIENT_ID, CLIENT_ID);

        Config config = Config.builder()
                .clientId(CLIENT_ID)
                .applicationContext(RuntimeEnvironment.application)
                .build();
        assert loginStrategy != null;
        final Intent actualLoginIntent = loginStrategy.getLoginIntent(config, new HashSet<>(SCOPE));

        assertThat(actualLoginIntent.getAction()).isEqualTo(expectedLoginIntent.getAction());
        assertThat(actualLoginIntent.getPackage()).isEqualTo(expectedLoginIntent.getPackage());
        assertThat(actualLoginIntent.getStringExtra(EXTRA_CLIENT_ID)).isEqualTo(expectedLoginIntent.getStringExtra(EXTRA_CLIENT_ID));
        assertThat(actualLoginIntent.getStringArrayListExtra(EXTRA_SCOPES)).isEqualTo(expectedLoginIntent.getStringArrayListExtra(EXTRA_SCOPES));
    }

    @NonNull
    private ResolveInfo resolveInfo(int sdkVersion, @NonNull String packageName, String[] fingerprints) throws PackageManager.NameNotFoundException {
        ResolveInfo info = mock(ResolveInfo.class);
        info.activityInfo = mock(ActivityInfo.class);
        info.activityInfo.packageName = packageName;

        ApplicationInfo appInfo = mock(ApplicationInfo.class);
        Bundle metadata = new Bundle(1);
        metadata.putInt(META_SDK_VERSION, sdkVersion);
        appInfo.metaData = metadata;

        when(packageManager.getApplicationInfo(info.activityInfo.packageName, PackageManager.GET_META_DATA)).thenReturn(appInfo);
        when(extractor.get(info.activityInfo.packageName, packageManager)).thenReturn(fingerprints);

        return info;
    }
}