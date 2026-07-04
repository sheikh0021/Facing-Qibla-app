package com.application.myapplication.data

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import com.google.android.gms.location.LocationRequest
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority


class LocationSettingsHelper(context: Context) {
    private val appContext = context.applicationContext
    private val settingsClient = LocationServices.getSettingsClient(appContext)

    fun isDeviceLocationEnabled(): Boolean {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun checkLocationSettings(
        onReady: () -> Unit,
        onResolvable: (IntentSenderRequest) -> Unit,
        onFailure: () -> Unit
    ){
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).build()

        val settingsRequest = com.google.android.gms.location.LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

        settingsClient.checkLocationSettings(settingsRequest)
            .addOnSuccessListener { onReady() }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    val request = IntentSenderRequest.Builder(exception.resolution).build()
                    onResolvable(request)
                } else {
                    onFailure()
                }
            }
    }

    fun buildOpenLocationSettingsIntent(): Intent {
        return Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    }
    fun buildOpenAppSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", appContext.packageName, null)
        }
    }

}