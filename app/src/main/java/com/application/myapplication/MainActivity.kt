package com.application.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.data.LocationProvider
import com.application.myapplication.data.LocationSettingsHelper
import com.application.myapplication.location.LocationGateState
import com.application.myapplication.ui.screens.QiblaScreen
import com.application.myapplication.ui.screens.QiblaViewModel


class MainActivity : ComponentActivity() {

    private lateinit var compassProvider: CompassProvider
    private lateinit var locationProvider: LocationProvider
    private lateinit var locationSettingsHelper: LocationSettingsHelper
    private lateinit var viewModel: QiblaViewModel

    private var locationGateState by mutableStateOf(LocationGateState.PERMISSION_REQUIRED)

    // Step 1 dialog: "Allow Facing Qibla to access location?"
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            onLocationPermissionGranted()
        } else {
            locationGateState = LocationGateState.PERMISSION_REQUIRED
        }
    }

    // Step 2 dialog: Google's "Turn on location?" popup
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            beginLocationUpdates()
        } else {
            locationGateState = LocationGateState.LOCATION_DISABLED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        compassProvider = CompassProvider(this)
        locationProvider = LocationProvider(this)
        locationSettingsHelper = LocationSettingsHelper(this)
        viewModel = QiblaViewModel(compassProvider, locationProvider)

        enableEdgeToEdge()
        setContent {
            QiblaScreen(
                viewModel = viewModel,
                locationGateState = locationGateState,
                onRequestLocationPermission = ::requestLocationPermission,
                onEnableDeviceLocation = ::ensureDeviceLocationEnabled,
                onOpenAppSettings = ::openAppSettings
            )
        }

        refreshLocationGate()
    }

    override fun onResume() {
        super.onResume()
        compassProvider.start()
        refreshLocationGate()
    }

    override fun onPause() {
        super.onPause()
        compassProvider.stop()
    }

    /** Called on app open and when returning from settings. */
    private fun refreshLocationGate() {
        if (!hasLocationPermission()) {
            locationGateState = LocationGateState.PERMISSION_REQUIRED
            return
        }

        if (locationSettingsHelper.isDeviceLocationEnabled()) {
            beginLocationUpdates()
        } else {
            locationGateState = LocationGateState.LOCATION_DISABLED
        }
    }

    /** Runs right after user taps Allow on permission dialog. */
    private fun onLocationPermissionGranted() {
        ensureDeviceLocationEnabled()
    }

    /** Shows Google "Turn on location?" dialog OR opens settings as fallback. */
    private fun ensureDeviceLocationEnabled() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        locationSettingsHelper.checkLocationSettings(
            onReady = { beginLocationUpdates() },
            onResolvable = { request -> locationSettingsLauncher.launch(request) },
            onFailure = {
                locationGateState = LocationGateState.LOCATION_DISABLED
                startActivity(locationSettingsHelper.buildOpenLocationSettingsIntent())
            }
        )
    }

    private fun openAppSettings() {
        startActivity(locationSettingsHelper.buildOpenAppSettingsIntent())
    }

    private fun beginLocationUpdates() {
        locationGateState = LocationGateState.READY
        viewModel.startLocationUpdates()
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }
}