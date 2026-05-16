package com.application.myapplication

import android.Manifest
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.data.LocationProvider
import com.application.myapplication.ui.screens.QiblaScreen
import com.application.myapplication.ui.screens.QiblaViewModel
import com.application.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var compassProvider: CompassProvider
    private lateinit var locationProvider: LocationProvider

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true){
            //permission granted.....
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compassProvider = CompassProvider(this)
        locationProvider = LocationProvider(this)
        val viewModel = QiblaViewModel(compassProvider, locationProvider)
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        enableEdgeToEdge()
        setContent {
            QiblaScreen(viewModel)
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
}

