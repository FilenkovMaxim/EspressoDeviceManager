package com.example.espressodevicemanager

import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.util.Log

/**
 * Foreground local service for upload log files.
 */
class ForegroundService : Service() {
    private val tag = "#TEST ForegroundService"
    var mockLocation = false
    var latitude = .0
    var longitude = .0

    override fun onCreate() {
        Log.d(tag, "onCreate()")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand()")
        if (intent.getBooleanExtra("stop", false)) {
            mockLocation = false
            stopSelf()
            return Service.START_STICKY
        }

        // initialize
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
        Log.d(tag, "onDestroy()")
        DeviceManager.stopMockLocation()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(tag, "onBind()")
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
    }
}