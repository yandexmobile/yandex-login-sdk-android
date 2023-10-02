package com.yandex.authsdk.internal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

internal class ChromeTabDataActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.extras?.let { extras ->
            if (!extras.isEmpty) {
                Log.e(TAG, "Extras in this intent mustn't exists due to security reasons")
                extras.keySet().forEach { intent.removeExtra(it) }
            }
        }

        val intent = Intent(this, ChromeTabLoginActivity::class.java).apply {
            data = intent.data
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startActivity(intent)
        finish()
    }

    internal companion object {

        val TAG: String = ChromeTabDataActivity::class.java.simpleName
    }
}
