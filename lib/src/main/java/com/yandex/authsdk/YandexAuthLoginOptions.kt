package com.yandex.authsdk

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class YandexAuthLoginOptions internal constructor(
        val scopes: ArrayList<String>?,
        val uid: Long?,
        val loginHint: String?,
        val isForceConfirm: Boolean
) : Parcelable {

    class Builder {
        private var scopes: ArrayList<String>? = null
        private var uid: Long? = null
        private var loginHint: String? = null
        private var isForceConfirm: Boolean = true

        fun setScopes(scopes: Set<String>?): Builder {
            this.scopes = if (scopes == null) null else ArrayList(scopes)
            return this
        }

        fun setUid(uid: Long?): Builder {
            this.uid = uid
            return this
        }

        fun setLoginHint(loginHint: String?): Builder {
            this.loginHint = loginHint
            return this
        }

        fun setForceConfirm(forceConfirm: Boolean): Builder {
            this.isForceConfirm = forceConfirm
            return this
        }

        fun build(): YandexAuthLoginOptions {
            return YandexAuthLoginOptions(scopes, uid, loginHint, isForceConfirm)
        }
    }
}
