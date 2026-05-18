package com.application.myapplication.ui.screens

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.application.myapplication.data.CompassProvider
import com.application.myapplication.data.LocationProvider
import com.application.myapplication.domain.QiblaCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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

    val qiblaBearing: StateFlow<Float> = userLocation.map { location ->
        if (location != null) {
            QiblaCalculator.calculateBearing(location.latitude, location.longitude)
        }else {
            DEFAULT_QIBLA_BEARING
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        DEFAULT_QIBLA_BEARING)

   val needleRotation: StateFlow<Float> = combine(azimuth, qiblaBearing) {
       phoneAzimuth: Float, qiblaBearingDegrees: Float ->
       normalizeDegrees(qiblaBearingDegrees - phoneAzimuth)
   }.stateIn(
       scope = viewModelScope,
       started = SharingStarted.WhileSubscribed(5000),
       initialValue = 0f
   )

    private val isInsideQiblaTolerance = needleRotation.map { rotation ->
        val difference = minOf(rotation, 360f - rotation )
        difference <= FACING_TOLERANCE_DEGREES
    }.distinctUntilChanged()

    val isFacingQibla: StateFlow<Boolean> = isInsideQiblaTolerance.flatMapLatest {
       isInsideTolerance ->
        if (isInsideTolerance) {
            flow {
                delay(LOCK_DELAY_MILLIS)
                emit(true)
            }
        }else {
            flowOf(false)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val pointerRotation: StateFlow<Float> = combine(needleRotation, isFacingQibla) {
        rotation, locked ->
        if (locked) {
            0f
        } else {
            rotation
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    private fun normalizeDegrees(value: Float) : Float{
        return (value + 360f) % 360f
    }

    companion object {
        private const val DEFAULT_QIBLA_BEARING = 286f
        private const val FACING_TOLERANCE_DEGREES = 8f

        private const val LOCK_DELAY_MILLIS = 2_000L
    }
}