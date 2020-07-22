package com.yandex.authsdk.internal;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yandex.authsdk.YandexAuthOptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;

import edu.emory.mathcs.backport.java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PackageManagerHelperTest {

    private static final String YANDEX_FINGERPRINT = "30820328308202e6a00302010202044af98700300b06072a8648ce38040305003076310b3009060355040613025255310f300d060355040813064d6f73636f77310f300d060355040713064d6f73636f7731133011060355040a130a4f4f4f2059616e646578311b3019060355040b13124d6f62696c6520446576656c6f706d656e74311330110603550403130a4f4f4f2059616e6465783020170d3039313131303135333030385a180f32303530313230353135333030385a3076310b3009060355040613025255310f300d060355040813064d6f73636f77310f300d060355040713064d6f73636f7731133011060355040a130a4f4f4f2059616e646578311b3019060355040b13124d6f62696c6520446576656c6f706d656e74311330110603550403130a4f4f4f2059616e646578308201b83082012c06072a8648ce3804013082011f02818100fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c70215009760508f15230bccb292b982a2eb840bf0581cf502818100f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a03818500028181008d46ae68eb83b6ecdbf8b50c4687d839d1b67f6b3bb8fc131fae32d19771ca793f3222d95102aac86f315bb35f731690d395f9c075fcd5586e0d1f596d0ef69e2e64a076faa1f07425b53026d8ee5433d764101d6ded44c5a0775df6972736ca09c1a442aa8e979341cf73b58f41ccab038547e97e5863319e2864648e903a3c300b06072a8648ce3804030500032f00302c021433938f31f9895ae15eb17e737c2592fa8d54009702147eb5146e2b2af743d796abb14b812f952aea6acf";

    private static final ApplicationInfo YANDEX_APPLICATION_1 = createApplicationInfo("package2", 1, 2, null);
    private static final ApplicationInfo YANDEX_APPLICATION_2 = createApplicationInfo("package3", 2, 3, null);
    private static final ApplicationInfo YANDEX_APPLICATION_3 = createApplicationInfo("package3", 2, 3, 1);


    @Mock
    private PackageManager packageManager;

    @Mock
    private YandexAuthOptions options;

    private PackageManagerHelper packageManagerHelper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(packageManager.queryIntentActivities(any(), anyInt()))
                .thenReturn(new ArrayList<ResolveInfo>() {{
                    add(new ResolveInfo());
                }});

        packageManagerHelper = new PackageManagerHelper(
                RuntimeEnvironment.application.getPackageName(),
                packageManager,
                options
        );
    }

    @Test
    public void testApplicationsNotFound() {
        doReturn(Collections.emptyList()).when(packageManager).getInstalledApplications(anyInt());

        assertThat(packageManagerHelper.findLatestApplication()).isNull();
    }

    @Test
    public void testYandexApplicationsNotFound() {
        doReturn(Arrays.asList(
                createApplicationInfo("package1", null, null, null),
                createApplicationInfo("package2", null, null, null)
        )).when(packageManager).getInstalledApplications(anyInt());

        assertThat(packageManagerHelper.findLatestApplication()).isNull();
    }

    @Test
    public void testYandexApplicationsWithValidFingerprintNotFound() throws Exception {
        doReturn(Arrays.asList(
                createApplicationInfo("package1", null, null, null),
                YANDEX_APPLICATION_1

        )).when(packageManager).getInstalledApplications(anyInt());

        addFingerprint(YANDEX_APPLICATION_1.packageName, "112233");

        assertThat(packageManagerHelper.findLatestApplication()).isNull();
    }

    @Test
    public void testYandexApplicationsFound() throws Exception {
        doReturn(Arrays.asList(
                YANDEX_APPLICATION_1

        )).when(packageManager).getInstalledApplications(anyInt());

        addFingerprint(YANDEX_APPLICATION_1.packageName, YANDEX_FINGERPRINT);

        assertThat(packageManagerHelper.findLatestApplication()).isNotNull()
                .extracting("packageName")
                .isEqualTo(new String[]{YANDEX_APPLICATION_1.packageName});
    }

    @Test
    public void testTwoYandexApplicationsFoundOneWrongSignature() throws Exception {
        doReturn(Arrays.asList(
                YANDEX_APPLICATION_1,
                YANDEX_APPLICATION_2

        )).when(packageManager).getInstalledApplications(anyInt());

        addFingerprint(YANDEX_APPLICATION_1.packageName, YANDEX_FINGERPRINT);
        addFingerprint(YANDEX_APPLICATION_2.packageName, "112233");

        assertThat(packageManagerHelper.findLatestApplication()).isNotNull()
                .extracting("packageName")
                .isEqualTo(new String[]{YANDEX_APPLICATION_1.packageName});
    }

    @Test
    public void testTwoYandexApplicationsFound_SelectNewest() throws Exception {
        doReturn(Arrays.asList(
                YANDEX_APPLICATION_1,
                YANDEX_APPLICATION_2

        )).when(packageManager).getInstalledApplications(anyInt());

        addFingerprint(YANDEX_APPLICATION_1.packageName, YANDEX_FINGERPRINT);
        addFingerprint(YANDEX_APPLICATION_2.packageName, YANDEX_FINGERPRINT);

        assertThat(packageManagerHelper.findLatestApplication()).isNotNull()
                .extracting("packageName")
                .isEqualTo(new String[]{YANDEX_APPLICATION_2.packageName});
    }

    @Test
    public void testTwoYandexApplicationsFound_SelectNewestWithInternalVersion() throws Exception {
        doReturn(Arrays.asList(
                YANDEX_APPLICATION_2,
                YANDEX_APPLICATION_3

        )).when(packageManager).getInstalledApplications(anyInt());

        addFingerprint(YANDEX_APPLICATION_2.packageName, YANDEX_FINGERPRINT);
        addFingerprint(YANDEX_APPLICATION_3.packageName, YANDEX_FINGERPRINT);

        assertThat(packageManagerHelper.findLatestApplication()).isNotNull()
                .extracting("packageName")
                .isEqualTo(new String[]{YANDEX_APPLICATION_3.packageName});
    }

    private void addFingerprint(@NonNull final String packageName, @NonNull final String fingerprint) throws Exception {
        final PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = packageName;
        packageInfo.signatures = new Signature[]{new Signature(fingerprint)};
        doReturn(packageInfo).when(packageManager).getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
    }


    @NonNull
    private static ApplicationInfo createApplicationInfo(
            @NonNull final String packageName,
            @Nullable final Integer metaSdkVersion,
            @Nullable final Integer metaAmVersion,
            @Nullable final Integer metaAmInternalVersion
    ) {
        final ApplicationInfo info = new ApplicationInfo();
        info.packageName = packageName;
        if (metaSdkVersion != null || metaAmVersion != null) {
            info.metaData = new Bundle();
            if (metaSdkVersion != null) {
                info.metaData.putInt(PackageManagerHelper.META_SDK_VERSION, metaSdkVersion);
            }
            if (metaAmVersion != null) {
                info.metaData.putFloat(PackageManagerHelper.META_AM_VERSION, metaAmVersion);
            }
            if (metaAmInternalVersion != null) {
                info.metaData.putInt(PackageManagerHelper.META_AM_INTERNAL_VERSION, metaAmInternalVersion);
            }
        }
        return info;
    }
}
