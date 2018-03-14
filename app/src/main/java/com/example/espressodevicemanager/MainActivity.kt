package com.example.espressodevicemanager

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

/**
 * App main screen.
 * Contains buttons for manually check ability to change settings.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.System.canWrite(applicationContext)) {
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

    override fun onResume() {
        super.onResume()
        wifiSwitch.isChecked = DeviceManager.isWifiEnable(applicationContext)
    }
}
