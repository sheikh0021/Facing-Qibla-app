package com.application.myapplication.ui.screens

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.data.LocationProvider
import com.application.myapplication.domain.QiblaCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class QiblaViewModel(private val compassProvider: CompassProvider, private val locationProvider: LocationProvider): ViewModel() {
    val azimuth = compassProvider.azimuth
    val userLocation: StateFlow<Location?> = locationProvider.getLocationUpdates().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), null)

    val qiblaBearing = userLocation.map { location ->
        if (location != null) {
            QiblaCalculator.calculateBearing(location.latitude, location.longitude)
        }else {
            286f
        }

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 286f)

    val needleRotation = combine(azimuth, qiblaBearing) {az, bear ->
        bear - az
    }
}