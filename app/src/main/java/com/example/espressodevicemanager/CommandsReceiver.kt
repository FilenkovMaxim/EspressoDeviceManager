package com.example.espressodevicemanager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Broadcast receiver for listen commands from tested app.
 *
 * Java usage example:
 * Intent intent = new Intent();
 * intent.setAction("com.example.espressodevicemanager.action");
 * intent.putExtra("option","wifi");
 * intent.putExtra("value",true);
 * sendBroadcast(intent);
 *
 * Console usage example:
 * adb -s 192.168.56.101 shell am broadcast -n com.example.espressodevicemanager/.CommandsReceiver
 *   -a com.example.espressodevicemanager.action --es option wifi --ez value true
 */
class CommandsReceiver : BroadcastReceiver() {
    private val tag = "#EDM CommandsReceiver"
    private val action = "com.example.espressodevicemanager.action"
    private val optionName = "option"
    private val valueName = "value"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(tag, "onReceive()")
        if (context == null || intent == null) {
            Log.w(tag, "onReceive() exit: null")
            return
        }

        if (action != intent.action) {
            Log.w(tag, "onReceive() exit: unknown action " + intent.action)
            return
        }
        val option = intent.getStringExtra(optionName) ?: return

        when (option) {
            "wifi" -> {
                val value = intent.getBooleanExtra(valueName, false)
                DeviceManager.setWifi(context.applicationContext, value)
            }
        }

        // Send local broadcast for update screen indicators
        val refreshIntent = Intent("com.example.espressodevicemanager.refresh")
        refreshIntent.putExtra("option", option)
        context.sendBroadcast(refreshIntent)
    }
}