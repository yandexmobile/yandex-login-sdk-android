package com.yandex.authsdk.internal.provider

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.Signature
import android.os.Bundle
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.internal.PackageManagerHelper
import com.yandex.authsdk.internal.provider.ProviderClient
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ProviderClientResolverTest {

    private lateinit var packageManagerHelper: PackageManagerHelper

    @Mock
    private lateinit var packageManager: PackageManager

    @Mock
    private lateinit var options: YandexAuthOptions

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        packageManagerHelper = PackageManagerHelper(
            RuntimeEnvironment.application.packageName,
            packageManager,
            options,
        )

        Mockito.`when`(packageManager.queryIntentActivities(any(), anyInt()))
            .thenReturn(listOf(ResolveInfo()))

        val packageInfo = PackageInfo()
        packageInfo.signatures = arrayOf(Signature(YANDEX_FINGERPRINT))
        Mockito.doReturn(packageInfo).`when`(packageManager)
            .getPackageInfo(ArgumentMatchers.anyString(), anyInt())
    }

    @Test
    @Throws(Exception::class)
    fun testCreateProviderClientV2IfApplicationV2Found() {
        val providerClientResolver = ProviderClientResolver(packageManagerHelper!!)
        Mockito.doReturn(
            listOf(createApplicationInfo("any_package_name", 2, 1, -1))
        ).`when`(packageManager).getInstalledApplications(anyInt())
        Assertions.assertThat(providerClientResolver.createProviderClient(RuntimeEnvironment.application))
            .isInstanceOf(ProviderClient::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testCreateProviderV1IfApplicationNotFound() {
        val providerClientResolver = ProviderClientResolver(packageManagerHelper!!)
        Mockito.doReturn(listOf<ApplicationInfo>()).`when`(packageManager)
            .getInstalledApplications(anyInt())
        Assertions.assertThat(providerClientResolver.createProviderClient(RuntimeEnvironment.application))
            .isNull()
    }

    @Test
    @Throws(Exception::class)
    fun testCreateProviderV1IfApplicationV1Found() {
        val providerClientResolver = ProviderClientResolver(packageManagerHelper!!)
        Mockito.doReturn(
            listOf(createApplicationInfo("any_package_name", 1, 1, -1))
        ).`when`(packageManager).getInstalledApplications(anyInt())
        Assertions.assertThat(providerClientResolver.createProviderClient(RuntimeEnvironment.application))
            .isNull()
    }

    companion object {

        private const val YANDEX_FINGERPRINT =
            "30820328308202e6a00302010202044af98700300b06072a8648ce38040305003076310b3009060355040613025255310f300d060355040813064d6f73636f77310f300d060355040713064d6f73636f7731133011060355040a130a4f4f4f2059616e646578311b3019060355040b13124d6f62696c6520446576656c6f706d656e74311330110603550403130a4f4f4f2059616e6465783020170d3039313131303135333030385a180f32303530313230353135333030385a3076310b3009060355040613025255310f300d060355040813064d6f73636f77310f300d060355040713064d6f73636f7731133011060355040a130a4f4f4f2059616e646578311b3019060355040b13124d6f62696c6520446576656c6f706d656e74311330110603550403130a4f4f4f2059616e646578308201b83082012c06072a8648ce3804013082011f02818100fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c70215009760508f15230bccb292b982a2eb840bf0581cf502818100f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a03818500028181008d46ae68eb83b6ecdbf8b50c4687d839d1b67f6b3bb8fc131fae32d19771ca793f3222d95102aac86f315bb35f731690d395f9c075fcd5586e0d1f596d0ef69e2e64a076faa1f07425b53026d8ee5433d764101d6ded44c5a0775df6972736ca09c1a442aa8e979341cf73b58f41ccab038547e97e5863319e2864648e903a3c300b06072a8648ce3804030500032f00302c021433938f31f9895ae15eb17e737c2592fa8d54009702147eb5146e2b2af743d796abb14b812f952aea6acf"

        private fun createApplicationInfo(
            packageName: String,
            metaSdkVersion: Int?,
            metaAmVersion: Int?,
            metaAmInternalVersion: Int?
        ): ApplicationInfo {
            val info = ApplicationInfo()
            info.packageName = packageName
            if (metaSdkVersion != null || metaAmVersion != null) {
                info.metaData = Bundle()
                if (metaSdkVersion != null) {
                    info.metaData.putInt(PackageManagerHelper.META_SDK_VERSION, metaSdkVersion)
                }
                if (metaAmVersion != null) {
                    info.metaData.putFloat(
                        PackageManagerHelper.META_AM_VERSION,
                        metaAmVersion.toFloat()
                    )
                }
                if (metaAmInternalVersion != null) {
                    info.metaData.putInt(
                        PackageManagerHelper.META_AM_INTERNAL_VERSION,
                        metaAmInternalVersion
                    )
                }
            }
            return info
        }
    }
}
