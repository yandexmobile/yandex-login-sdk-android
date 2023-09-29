package com.yandex.authsdk

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.yandex.authsdk.internal.Constants

open class YandexAuthOptions : Parcelable {
    val clientId: String
    val isLoggingEnabled: Boolean

    val context: Context?
    val oauthHost: String

    @JvmOverloads
    constructor(context: Context,
                loggingEnabled: Boolean = false,
                clientIdIndex: Int = 0) {

        val app: ApplicationInfo = try {
            context.packageManager.getApplicationInfo(context.packageName,
                PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }

        val clientId = if (clientIdIndex == 0) app.metaData.getString(Constants.META_CLIENT_ID) else
            app.metaData.getString("${Constants.META_CLIENT_ID}_$clientIdIndex")

        checkNotNull(clientId) {
            String.format("Application should provide client id with index%s in gradle.properties",
                clientIdIndex)
        }
        this.clientId = clientId
        isLoggingEnabled = loggingEnabled
        this.context = context
        oauthHost = app.metaData.getString(Constants.META_OAUTH_HOST)!!
    }

    val isTesting: Boolean
        get() = !TextUtils.equals(oauthHost, Constants.HOST_PRODUCTION)

    protected constructor(`in`: Parcel) {
        clientId = `in`.readString()!!
        isLoggingEnabled = `in`.readByte().toInt() != 0
        oauthHost = `in`.readString()!!
        context = null
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(clientId)
        dest.writeByte((if (isLoggingEnabled) 1 else 0).toByte())
        dest.writeString(oauthHost)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<YandexAuthOptions> {
        override fun createFromParcel(parcel: Parcel): YandexAuthOptions {
            return YandexAuthOptions(parcel)
        }

        override fun newArray(size: Int): Array<YandexAuthOptions?> {
            return arrayOfNulls(size)
        }
    }
}
