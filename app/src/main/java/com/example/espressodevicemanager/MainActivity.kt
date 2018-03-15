package com.example.espressodevicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

/**
 * App main screen.
 * Contains buttons for manually check ability to change settings.
 */
class MainActivity : AppCompatActivity() {
    private val tag = "#EDM MainActivity"
    /**
     * Broadcast receiver for display changes after receive commands.
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(tag, "onReceive()")
            Toast.makeText(applicationContext, "Received command: " + intent, Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ displayStates() }, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()")
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openSettingsButton.visibility = View.GONE
        } else {
            openSettingsButton.setOnClickListener {
                DeviceManager.openSettings(applicationContext)
            }
        }

        wifiSwitch.setOnCheckedChangeListener { _, b ->
            run {
                DeviceManager.setWifi(applicationContext, b)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart()")
        displayStates()

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.espressodevicemanager.refresh")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag, "onStop()")
        unregisterReceiver(broadcastReceiver)
    }

    /**
     * Display current states.
     */
    fun displayStates() {
        Log.d(tag, "displayStates()")
        wifiSwitch.isChecked = DeviceManager.isWifiEnable(applicationContext)
    }
}
