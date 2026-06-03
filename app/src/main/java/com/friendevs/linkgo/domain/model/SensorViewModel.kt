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
    var headingDeg by mutableStateOf(0f)
    var isFaceDown by mutableStateOf(false)

    private var accel = 0f
    private var accelCurrent = SensorManager.GRAVITY_EARTH
    private var accelLast = SensorManager.GRAVITY_EARTH
    private val SHAKE_THRESHOLD = 8f

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)


    init {
        val sensors = listOf(
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_PROXIMITY,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_GYROSCOPE
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

                // Guarda la gravedad para el calculo de rumbo (brujula).
                gravity[0] = 0.9f * gravity[0] + 0.1f * x
                gravity[1] = 0.9f * gravity[1] + 0.1f * y
                gravity[2] = 0.9f * gravity[2] + 0.1f * z

                accelLast = accelCurrent
                accelCurrent = sqrt(x * x + y * y + z * z)

                val delta = accelCurrent - accelLast
                accel = accel * 0.9f + delta

                if (accel > 12f) {
                    shakeDetected = true
                }
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic[0] = event.values[0]
                geomagnetic[1] = event.values[1]
                geomagnetic[2] = event.values[2]

                val rotation = FloatArray(9)
                if (SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic)) {
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotation, orientation)
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    headingDeg = (azimuth + 360f) % 360f
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                // El eje Z del giroscopio apunta hacia arriba cuando el telefono esta boca arriba.
                // Acumulamos la inclinacion sobre el eje X para detectar si esta boca abajo.
                // Usamos el acelerometro ya filtrado (gravity[]) para determinar orientacion.
                // gravity[2] < -7 significa que la pantalla mira hacia el suelo (boca abajo).
                isFaceDown = gravity[2] < -7f
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