package com.example.espressodevicemanager

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log


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
     * Switch WiFi state to to [enabled] state..
     * @param context application context.
     */
    fun setWifi(context: Context, enabled: Boolean) {
        Log.d(TAG, "set wifi " + enabled)
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifi.isWifiEnabled = enabled
    }
}