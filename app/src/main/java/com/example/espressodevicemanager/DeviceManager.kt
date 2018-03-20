package com.example.espressodevicemanager

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * Manager for change system settings and device state.
 */
object DeviceManager {
    private const val TAG = "#EDM DeviceManager"

    /**
     * Open Setting write system settings screen for Android 6+ or app settings screen.
     */
    @TargetApi(Build.VERSION_CODES.M)
    fun openSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                intent.data = Uri.parse("package:" + context.packageName)
                context.startActivity(intent)
            }
        } else {
            // just open app info screen
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + context.packageName))
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * @return true if wife is enabled now.
     */
    fun isWifiEnable(context: Context): Boolean {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifi.isWifiEnabled
    }

    /**
     * Switch WiFi state to [enabled] state.
     * @param context application context.
     */
    fun setWifi(context: Context, enabled: Boolean) {
        Log.d(TAG, "set wifi " + enabled)
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifi.isWifiEnabled = enabled
    }

    /**
     * @return true if airplane mode is activated now.
     */
    fun isAirplaneModeEnable(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 1
        }
        return false
    }

    /**
     * Switch airplane mode to [enabled] state..
     * @param context application context.
     */
    fun setAirplaneMode(context: Context, enabled: Boolean) {
        Log.d(TAG, "set airplane mode " + enabled)
        val mode = if (enabled) {
            "1"
        } else {
            "0"
        }

        exec("su shell settings put global airplane_mode_on " + mode)
        exec("su shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state " + mode)
    }

    /**
     * Execute shell command.
     */
    private fun exec(command: String) {
        Log.d(TAG, "exec() " + command)
        try {
            val process = Runtime.getRuntime().exec(command)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val inputString = bufferedReader.use { it.readText() }

            // Waits for the command to finish.
            process.waitFor()
            Log.d(TAG, inputString)
        } catch (e: IOException) {
            Log.e(TAG, "IOException " + e)
        } catch (e: InterruptedException) {
            Log.e(TAG, "InterruptedException " + e)
        }
    }
}