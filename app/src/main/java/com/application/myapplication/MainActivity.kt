package com.application.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.data.LocationProvider
import com.application.myapplication.ui.screens.QiblaScreen
import com.application.myapplication.ui.screens.QiblaViewModel
import com.application.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var compassProvider: CompassProvider
    private lateinit var locationProvider: LocationProvider
    private lateinit var viewModel: QiblaViewModel
    private val hasLocationPermission = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val granted = fineGranted || coarseGranted
        hasLocationPermission.value = granted
        if (granted){
            viewModel.startLocationUpdates()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compassProvider = CompassProvider(this)
        locationProvider = LocationProvider(this)
        viewModel = QiblaViewModel(compassProvider, locationProvider)
        hasLocationPermission.value = checkLocationPermission()

        if (hasLocationPermission.value) {
            viewModel.startLocationUpdates()
        }

        enableEdgeToEdge()
        setContent {
            QiblaScreen(viewModel = viewModel,
                hasLocationPermission = hasLocationPermission.value,
                onRequestLocationPermission = ::requestLocationPermission
            )
        }
        if (!hasLocationPermission.value){
            requestLocationPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        compassProvider.start()
    }

    override fun onPause() {
        super.onPause()
        compassProvider.stop()
    }

    private fun checkLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun requestLocationPermission(){
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}

