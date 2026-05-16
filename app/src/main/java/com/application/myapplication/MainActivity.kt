package com.application.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.ui.screens.QiblaScreen
import com.application.myapplication.ui.screens.QiblaViewModel
import com.application.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var compassProvider: CompassProvider
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        compassProvider = CompassProvider(this)
        val viewModel = QiblaViewModel(compassProvider)
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

