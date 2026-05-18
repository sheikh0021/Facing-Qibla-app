package com.application.myapplication.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs


class CompassProvider(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth = _azimuth.asStateFlow()

    private var hasSmoothedAzimuth = false
    private var smoothedAzimuth = 0f

    fun start(){
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop(){
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
  when (event.sensor.type) {
      Sensor.TYPE_ACCELEROMETER -> {
          System.arraycopy(event.values, 0 , gravity, 0 , 3)
      }

      Sensor.TYPE_MAGNETIC_FIELD -> {
          System.arraycopy(event.values, 0, geomagnetic, 0, 3)
      }
  }
        val rotationMatrix = FloatArray(9)
        val inclinationMatrix = FloatArray(9)

        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)) {
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            var rawDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (rawDegrees < 0f){
                rawDegrees += 360f
            }

            val stableDegrees = smoothAzimuth(rawDegrees)
            _azimuth.value = stableDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun smoothAzimuth(newAzimuth: Float): Float {
        if (!hasSmoothedAzimuth) {
            smoothedAzimuth = newAzimuth
            hasSmoothedAzimuth = true
            return smoothedAzimuth
        }

        val difference = shortestAngleDifference(smoothedAzimuth, newAzimuth)
        smoothedAzimuth = normalizeDegrees(smoothedAzimuth + (SMOOTHING_ALPHA * difference))
        return smoothedAzimuth
    }
    private fun shortestAngleDifference(from: Float, to: Float): Float {
        var difference = (to - from + 540f) % 360f - 180f
        if (abs(difference) < MINIMUM_VISIBLE_CHANGE_DEGREES) {
            difference = 0f
        }
        return  difference
    }

    private fun normalizeDegrees(value: Float) : Float {
        return (value + 360f) % 360f
    }

    companion object {
        private const val SMOOTHING_ALPHA = 0.12f
        private const val MINIMUM_VISIBLE_CHANGE_DEGREES = 0.5f
    }
}