package com.application.myapplication.ui.screens

import androidx.lifecycle.ViewModel
import com.application.myapplication.data.CompassProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class QiblaViewModel(private val compassProvider: CompassProvider): ViewModel() {
    val azimuth = compassProvider.azimuth

    private val _qiblaBearing = MutableStateFlow(286f)
    val qiblaBearing = _qiblaBearing.asStateFlow()

    val needleRotation = combine(azimuth, qiblaBearing) {az, bear ->
        bear - az
    }
    fun updateQiblaBearing(newBearing: Float) {
        _qiblaBearing.value = newBearing
    }
}