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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class QiblaViewModel(private val compassProvider: CompassProvider, private val locationProvider: LocationProvider): ViewModel() {
    private val _locationStarted = MutableStateFlow(false)
    val azimuth = compassProvider.azimuth
    val userLocation: StateFlow<Location?> = _locationStarted.flatMapLatest { started ->
        if (started) {
            locationProvider.getLocationUpdates()
        } else  {
            kotlinx.coroutines.flow.flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startLocationUpdates(){
        _locationStarted.value = true
    }

    val qiblaBearing = userLocation.map { location ->
        if (location != null) {
            QiblaCalculator.calculateBearing(location.latitude, location.longitude)
        }else {
            286f
        }

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 286f)

    val needleRotation: StateFlow<Float> = combine(azimuth, qiblaBearing) {az, bear ->
        var rotation = bear - az
        rotation = (rotation + 360f) % 360f
        rotation
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0f
    )

    val isFacingQibla: StateFlow<Boolean> = needleRotation.map { rotation ->
        val diff = minOf(rotation, 360f - rotation)
        diff <= 8f
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )
}