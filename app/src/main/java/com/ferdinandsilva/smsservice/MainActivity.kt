package com.ferdinandsilva.smsservice

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS), 1)
        startService(Intent(this, SMSService::class.java))
        finish()
    }

    override fun onStop() {
        super.onStop()
        stopService(Intent(this, SMSService::class.java))
    }
}