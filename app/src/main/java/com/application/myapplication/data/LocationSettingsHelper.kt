package com.application.myapplication.data

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.location.LocationServices


class LocationSettingsHelper(
    context: Context
){
    private val appContext = context.applicationContext
    private val settingsClient = LocationServices.getSettingsClient(appContext)

    fun isDeviceLocationEnabled(): Boolean {
        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }

}