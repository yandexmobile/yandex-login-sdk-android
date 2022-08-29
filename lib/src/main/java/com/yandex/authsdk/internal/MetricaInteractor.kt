package com.yandex.authsdk.internal

import android.content.Context
import com.yandex.authsdk.YandexAuthOptions

class MetricaInteractor(val context: Context, val options: YandexAuthOptions) {

    @Suppress("TooGenericExceptionCaught")
    fun sendUid(uid: Long) {
        val value = hashMapOf<String, Any>(Constants.METRICA_UID_KEY to uid)
        try {
            val clazz = Class.forName("com.yandex.metrica.p")
            val method = clazz.getMethod("rlse", Context::class.java, MutableMap::class.java)
            method.invoke(null, context, value)
            Logger.d(options, TAG, "Sending uid to Metrica succeed")
        } catch (error: Throwable) {
            Logger.e(options, TAG, "Sending uid to Metrica failed", error)
        }
    }

    companion object {

        private val TAG = MetricaInteractor::class.java.simpleName
    }
}
