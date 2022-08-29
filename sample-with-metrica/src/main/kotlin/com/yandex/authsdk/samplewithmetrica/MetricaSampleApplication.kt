package com.yandex.authsdk.samplewithmetrica

import android.app.Application
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

class MetricaSampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val config =
            YandexMetricaConfig.newConfigBuilder("8e543774-6e21-4a1e-a1e5-d9d6316c36cb").build()
        YandexMetrica.activate(applicationContext, config)
        YandexMetrica.enableActivityAutoTracking(this)
    }
}
