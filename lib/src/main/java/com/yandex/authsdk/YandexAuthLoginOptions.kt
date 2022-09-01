package com.yandex.authsdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YandexAuthLoginOptions internal constructor(
        val uid: Long?,
        val loginHint: String?,
        val isForceConfirm: Boolean,
        val requiredScopes: ArrayList<String>?,
        val optionalScopes: ArrayList<String>?,
) : Parcelable {

    class Builder {
        private var uid: Long? = null
        private var loginHint: String? = null
        private var isForceConfirm: Boolean = true
        private var requiredScopes: ArrayList<String>? = null
        private var optionalScopes: ArrayList<String>? = null

        fun setOptionalScopes(scopes: Set<String>?): Builder {
            this.optionalScopes = scopes?.toValidArrayList()
            return this
        }

        fun setRequiredScopes(scopes: Set<String>?): Builder {
            this.requiredScopes = scopes?.toValidArrayList()
            return this
        }

        private fun Set<String>.toValidArrayList(): ArrayList<String> =
            ArrayList(this.filter { it.isNotEmpty() })

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
            return YandexAuthLoginOptions(
                uid = uid,
                loginHint = loginHint,
                isForceConfirm = isForceConfirm,
                requiredScopes = requiredScopes,
                optionalScopes = optionalScopes,
            )
        }
    }
}
