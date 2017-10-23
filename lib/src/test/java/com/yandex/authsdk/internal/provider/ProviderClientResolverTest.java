package com.yandex.authsdk.internal.provider;

import com.yandex.authsdk.internal.PackageManagerHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(RobolectricTestRunner.class)
public class ProviderClientResolverTest {

    @Mock
    private PackageManagerHelper packageManagerHelper;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateProviderClientV2IfApplicationV2Found() throws Exception {
        final ProviderClientResolver providerClientResolver = new ProviderClientResolver(packageManagerHelper);

        doReturn(new PackageManagerHelper.YandexApplicationInfo(
                "any_package_name",
                2,
                1,
                -1
        )).when(packageManagerHelper).findLatestApplication();

        assertThat(providerClientResolver.createProviderClient(RuntimeEnvironment.application))
                .isInstanceOf(ProviderClient.class);
    }

    @Test
    public void testCreateProviderV1IfApplicationNotFound() throws Exception {
        final ProviderClientResolver providerClientResolver = new ProviderClientResolver(packageManagerHelper);

        doReturn(null).when(packageManagerHelper).findLatestApplication();

        assertThat(providerClientResolver.createProviderClient(RuntimeEnvironment.application))
                .isNull();
    }

    @Test
    public void testCreateProviderV1IfApplicationV1Found() throws Exception {
        final ProviderClientResolver providerClientResolver = new ProviderClientResolver(packageManagerHelper);

        doReturn(new PackageManagerHelper.YandexApplicationInfo(
                "any_package_name",
                1,
                1,
                -1
        )).when(packageManagerHelper).findLatestApplication();

        assertThat(providerClientResolver.createProviderClient(RuntimeEnvironment.application))
                .isNull();
    }

}
