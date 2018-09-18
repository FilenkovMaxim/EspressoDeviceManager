package com.example.espressodevicemanager

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager

/**
 * Foreground local service for upload log files.
 */
class ForegroundService : Service() {
    var mockLocation = false
    var latitude = .0
    var longitude = .0

    private val broadCastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if ("com.example.espressodevicemanager.action" != action) {
                return
            }
            val option = intent.getStringExtra("option") ?: return
            when (option) {
                "location" -> {
                    latitude = intent.getStringExtra("lat").toDouble()
                    longitude = intent.getStringExtra("lng").toDouble()
                }
                "stop" -> {
                    mockLocation = false
                    stopForeground(true)
                    stopSelf()
                }
            }
        }
    }

    override fun onCreate() {
        isRunning = true
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(broadCastReceiver, IntentFilter("com.example.espressodevicemanager.action"))
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // initialize
        isRunning = true
        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("fake location")
                .setAutoCancel(true)
                .setProgress(0, 0, true)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build()
        startForeground(NOTIFICATION_ID, notification)
        latitude = intent.getDoubleExtra("latitude", .0)
        longitude = intent.getDoubleExtra("longitude", .0)
        mockLocation = true
        Thread(Runnable {
            while (mockLocation) {
                DeviceManager.setMockLocation(applicationContext, latitude, longitude)
                Thread.sleep(500)
            }
        }).start()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver)
        DeviceManager.stopMockLocation()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        /**
         * Channel id for notifications about uploads.
         */
        private const val NOTIFICATION_CHANNEL_ID = "com.example.espressodevicemanager.channel"
        /**
         * Notification id for startForeground().
         */
        private const val NOTIFICATION_ID = 3000
        /**
         * Running flag.
         */
        @get:Synchronized
        var isRunning = false
    }
}