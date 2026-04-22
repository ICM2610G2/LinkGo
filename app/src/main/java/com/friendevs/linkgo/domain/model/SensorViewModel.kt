package com.friendevs.linkgo.model

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlin.math.sqrt

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    var isDarkBySensor by mutableStateOf(false)
    var shakeDetected by mutableStateOf(false)
    var proximityNear by mutableStateOf(false)

    private var lastAccel = 0f
    private var currentAccel = 0f
    private val SHAKE_THRESHOLD = 8f

    init {
        val sensors = listOf(
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_PROXIMITY
        )

        sensors.forEach { type ->
            sensorManager.getDefaultSensor(type)?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                isDarkBySensor = lux < 100f
            }

            Sensor.TYPE_PROXIMITY -> {
                proximityNear = event.values[0] < event.sensor.maximumRange
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                lastAccel = currentAccel
                currentAccel = sqrt(x * x + y * y + z * z)
                val delta = currentAccel - lastAccel

                if (delta > SHAKE_THRESHOLD) {
                    shakeDetected = true
                }
            }
        }
    }

    fun resetShake() {
        shakeDetected = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}