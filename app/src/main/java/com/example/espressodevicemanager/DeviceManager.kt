package com.example.espressodevicemanager

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


/**
 * Manager for change system settings and device state.
 */
object DeviceManager {
    private const val TAG = "#TEST DeviceManager"
    private var headphonesPlugged: Boolean = false

    fun headphonesSetCurrentState(enabled: Boolean) {
        headphonesPlugged = enabled
    }

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
     * @return true if headphones are plugged.
     */
    fun isHeadphonesEnable(): Boolean {
        return headphonesPlugged
    }

    private const val RECEIVER = "-n com.dubsapp.devel/receivers.PartnerReceiver"

    /**
     * Emulate headphones plug-in/plug-out event.
     */
    fun setHeadphonesPlugged(plugged: Boolean) {
        Log.d(TAG, "set headphones $plugged")
//        exec("su shell am broadcast $RECEIVER -a com.dubsapp.intent.partner --ei state 1")
        if (plugged) {
            exec("su shell am broadcast $RECEIVER -a android.intent.action.HEADSET_PLUG --ei state 1")
        } else {
            exec("su shell am broadcast $RECEIVER -a android.media.AUDIO_BECOMING_NOISY")
            exec("su shell am broadcast $RECEIVER -a android.intent.action.HEADSET_PLUG --ei state 0")
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
     * @param enabled new state.
     */
    fun setWifi(context: Context, enabled: Boolean) {
        Log.d(TAG, "set wifi $enabled")
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
     * Switch airplane mode to [enabled] state.
     * @param enabled new state.
     */
    fun setAirplaneMode(enabled: Boolean) {
        Log.d(TAG, "set airplane mode $enabled")
        val mode = if (enabled) {
            "1"
        } else {
            "0"
        }

        exec("su shell settings put global airplane_mode_on $mode")
        exec("su shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state $mode")
    }


    /**
     * Location manager for mock location.
     */
    private var mockLocationManager: LocationManager? = null

    /**
     *
     * @param latitude fake latitude.
     * @param longitude fake longitude.
     */
    private fun startMockLocation(context: Context) {
        Log.d(TAG, "start mock location")
        mockLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mockLocationManager?.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false,
                false, true, false, false, 0, 5)
        mockLocationManager?.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
        mockLocationManager?.addTestProvider(LocationManager.NETWORK_PROVIDER, false, false, false,
                false, true, false, false, 0, 5)
        mockLocationManager?.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
    }

    /**
     * Mock location for testing.
     *
     * @param latitude fake latitude.
     * @param longitude fake longitude.
     */
    fun setMockLocation(context: Context, latitude: Double, longitude: Double) {
        Log.d(TAG, "set location to [$latitude, $longitude]")
        if (mockLocationManager == null) {
            startMockLocation(context)
        }

        val gpsLocation = Location(LocationManager.GPS_PROVIDER)
        gpsLocation.latitude = latitude
        gpsLocation.longitude = longitude
        gpsLocation.accuracy = 1.0f
        gpsLocation.altitude = 0.0
        gpsLocation.time = System.currentTimeMillis()
        gpsLocation.bearing = 0.0f

        val networkLocation = Location(LocationManager.NETWORK_PROVIDER)
        networkLocation.latitude = gpsLocation.latitude
        networkLocation.longitude = gpsLocation.longitude
        networkLocation.accuracy = gpsLocation.accuracy
        networkLocation.altitude = gpsLocation.altitude
        networkLocation.time = gpsLocation.time
        networkLocation.bearing = gpsLocation.bearing

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            gpsLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
            networkLocation.elapsedRealtimeNanos = gpsLocation.elapsedRealtimeNanos
        }

        try {
            val method = Location::class.java.getMethod("makeComplete", *arrayOfNulls(0))
            if (method != null) {
                method.invoke(gpsLocation, *arrayOfNulls(0))
                method.invoke(networkLocation, *arrayOfNulls(0))
            }
        } catch (ignored: Exception) {
        }

        mockLocationManager?.setTestProviderLocation(LocationManager.GPS_PROVIDER, gpsLocation)
        mockLocationManager?.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, networkLocation)
    }

    /**
     * Stop fake location.
     */
    fun stopMockLocation() {
        mockLocationManager?.clearTestProviderEnabled(LocationManager.GPS_PROVIDER)
        mockLocationManager?.clearTestProviderLocation(LocationManager.GPS_PROVIDER)
        mockLocationManager?.clearTestProviderStatus(LocationManager.GPS_PROVIDER)
        mockLocationManager?.removeTestProvider(LocationManager.GPS_PROVIDER)

        mockLocationManager?.clearTestProviderEnabled(LocationManager.NETWORK_PROVIDER)
        mockLocationManager?.clearTestProviderLocation(LocationManager.NETWORK_PROVIDER)
        mockLocationManager?.clearTestProviderStatus(LocationManager.NETWORK_PROVIDER)
        mockLocationManager?.removeTestProvider(LocationManager.NETWORK_PROVIDER)

        mockLocationManager = null
    }

    /**
     * Set new date.
     * @param context application context.
     */
    fun setDate(context: Context, date: String) {
        Log.d(TAG, "set date to $date")

    }

    /**
     * Set new time.
     * @param context application context.
     */
    fun setTime(context: Context, time: String) {
        Log.d(TAG, "set date to $time")

    }

    /**
     * Execute shell command.
     */
    private fun exec(command: String) {
        Log.d(TAG, "exec() $command")
        try {
            val process = Runtime.getRuntime().exec(command)
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
            val inputString = bufferedReader.use { it.readText() }

            // Waits for the command to finish.
            process.waitFor()
            Log.d(TAG, inputString)
        } catch (e: IOException) {
            Log.e(TAG, "IOException $e")
        } catch (e: InterruptedException) {
            Log.e(TAG, "InterruptedException $e")
        }
    }
}