package com.example.espressodevicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


/**
 * App main screen.
 * Contains buttons for manually check ability to change settings.
 */
class MainActivity : AppCompatActivity() {
    private val tag = "#TEST MainActivity"
    /**
     * Broadcast receiver for display changes after receive commands.
     */
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(tag, "onReceive() $intent")
            val action = intent.action
            if (action != null) {
                if (Intent.ACTION_HEADSET_PLUG == action) {
                    DeviceManager.headphonesSetCurrentState(intent.getIntExtra("state", -1) == 1)
                }
            }
            Toast.makeText(applicationContext, "Received: $intent", Toast.LENGTH_SHORT).show()
            displayStates()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate()")
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //openSettingsButton.visibility = View.GONE
        } else {
            openSettingsButton.setOnClickListener {
                DeviceManager.openSettings(applicationContext)
            }
        }

        headphonesSwitch.setOnCheckedChangeListener{ _, b -> DeviceManager.setHeadphonesPlugged(b)}
        wifiSwitch.setOnCheckedChangeListener { _, b -> DeviceManager.setWifi(applicationContext, b) }
        airplaneModeSwitch.setOnCheckedChangeListener { _, b -> DeviceManager.setAirplaneMode(b) }

        // request root access
        try {
            Runtime.getRuntime().exec("su")
        } catch (e: Exception) {
            Log.w(tag, "onCreate() cannot get the root access! $e")
        }

        DeviceManager.setTime("11191520")
    }

    override fun onStart() {
        super.onStart()
        Log.d(tag, "onStart()")
        displayStates()

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.example.espressodevicemanager.refresh")
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG)
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY) // unplugged
        intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
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
        headphonesSwitch.isChecked = DeviceManager.isHeadphonesEnable()
        wifiSwitch.isChecked = DeviceManager.isWifiEnable(applicationContext)
        airplaneModeSwitch.isChecked = DeviceManager.isAirplaneModeEnable(applicationContext)
    }
}
