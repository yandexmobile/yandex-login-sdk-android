package com.yandex.authsdk.internal

import android.app.Activity
import android.content.Intent
import android.os.Bundle

internal class BrowserDataActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, BrowserLoginActivity::class.java).apply {
            data = intent.data
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }
}
