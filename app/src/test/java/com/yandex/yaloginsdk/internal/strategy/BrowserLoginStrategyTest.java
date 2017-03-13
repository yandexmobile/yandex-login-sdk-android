package com.yandex.yaloginsdk.internal.strategy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;

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

import static com.yandex.yaloginsdk.internal.BrowserLoginActivity.EXTRA_BROWSER_PACKAGE_NAME;
import static com.yandex.yaloginsdk.internal.YaLoginSdkConstants.EXTRA_CLIENT_ID;
import static com.yandex.yaloginsdk.internal.strategy.LoginStrategy.EXTRA_SCOPES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class BrowserLoginStrategyTest {

    private static final ArrayList<String> SCOPE = new ArrayList<String>() {{
        add("scope");
    }};

    private static final String CLIENT_ID = "clientId";
    private static final String CHROME = "com.android.chrome";
    private static final String YABRO = "com.yandex.browser";
    public static final String OTHER_BROWSER = "some.other.browser";

    @Mock
    PackageManager packageManager;

    @Before
    public void beforeEachTest() {
        initMocks(this);
    }

    @Test
    public void findBest_returnsYaBro() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(YABRO);
        ResolveInfo wrong = createResolveInfo(OTHER_BROWSER);
        assertThat(BrowserLoginStrategy.findBest(toList(wrong, info))).isEqualTo(info.activityInfo.packageName);
    }

    @Test
    public void findBest_returnsChrome() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(CHROME);
        ResolveInfo wrong = createResolveInfo(OTHER_BROWSER);
        assertThat(BrowserLoginStrategy.findBest(toList(wrong, info))).isEqualTo(info.activityInfo.packageName);
    }

    @Test
    public void findBest_prefersChrome() throws PackageManager.NameNotFoundException {
        ResolveInfo infoChrome = createResolveInfo(CHROME);
        ResolveInfo infoYabro = createResolveInfo(YABRO);
        assertThat(BrowserLoginStrategy.findBest(toList(infoChrome, infoYabro))).isEqualTo(infoYabro.activityInfo.packageName);
        assertThat(BrowserLoginStrategy.findBest(toList(infoYabro, infoChrome))).isEqualTo(infoYabro.activityInfo.packageName);
    }

    @Test
    public void findBest_returnsNullIfNotSupported() throws PackageManager.NameNotFoundException {
        ResolveInfo info = createResolveInfo(OTHER_BROWSER);
        assertThat(BrowserLoginStrategy.findBest(toList(info))).isNull();
    }

    @Test
    public void getIfPossible_returnsFirstYabro() throws PackageManager.NameNotFoundException {
        ResolveInfo invalid1 = createResolveInfo(OTHER_BROWSER);
        ResolveInfo invalid2 = createResolveInfo(CHROME);
        ResolveInfo valid1 = createResolveInfo(YABRO);

        //noinspection WrongConstant
        when(packageManager.queryIntentActivities(any(), eq(PackageManager.MATCH_DEFAULT_ONLY)))
                .thenReturn(Arrays.asList(invalid1, invalid2, valid1));
        LoginStrategy loginStrategy = BrowserLoginStrategy.getIfPossible(RuntimeEnvironment.application, packageManager);
        assert loginStrategy != null;

        LoginSdkConfig config = LoginSdkConfig.builder()
                .clientId(CLIENT_ID)
                .applicationContext(RuntimeEnvironment.application)
                .build();
        final Intent actualLoginIntent = loginStrategy.getLoginIntent(config, new HashSet<>(SCOPE));

        assertThat(actualLoginIntent.getStringExtra(EXTRA_CLIENT_ID)).isEqualTo(CLIENT_ID);
        assertThat(actualLoginIntent.getStringExtra(EXTRA_BROWSER_PACKAGE_NAME)).isEqualTo(YABRO);
        assertThat(actualLoginIntent.getStringArrayListExtra(EXTRA_SCOPES)).isEqualTo(SCOPE);
    }

    @NonNull
    private ResolveInfo createResolveInfo(@NonNull String packageName) throws PackageManager.NameNotFoundException {
        ResolveInfo info = mock(ResolveInfo.class);
        info.activityInfo = mock(ActivityInfo.class);
        info.activityInfo.packageName = packageName;
        return info;
    }

    @NonNull
    private List<ResolveInfo> toList(@NonNull ResolveInfo... infos) {
        return Arrays.asList(infos);
    }
}