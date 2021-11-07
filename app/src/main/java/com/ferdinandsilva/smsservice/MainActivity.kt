package com.ferdinandsilva.smsservice

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, SMSService::class.java))
        finish()
    }

    override fun onStop() {
        super.onStop()
        stopService(Intent(this, SMSService::class.java))
    }
}