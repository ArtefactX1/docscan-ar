package com.zacky.docar.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.sqrt

class SensorKamera {
    var sudut by mutableStateOf(0f)
    var guncangan by mutableStateOf(false)
    var berputar by mutableStateOf(false)
    var lux by mutableStateOf<Float?>(null)
    var tersedia by mutableStateOf(false)
}

@Composable
fun ingatSensorKamera(): SensorKamera {
    val context = LocalContext.current
    val state = remember { SensorKamera() }

    DisposableEffect(context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val cahaya = manager.getDefaultSensor(Sensor.TYPE_LIGHT)
        val deteksi = Guncangan()

        state.tersedia = accel != null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        state.sudut = SudutHitung.kemiringan(x, y, z)
                        state.guncangan = deteksi.perbarui(x, y, z)
                    }

                    Sensor.TYPE_GYROSCOPE -> {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        state.berputar = sqrt(x * x + y * y + z * z) > Kesiapan.PUTARAN_MAKS
                    }

                    Sensor.TYPE_LIGHT -> state.lux = event.values[0]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        accel?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        gyro?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME) }
        cahaya?.let { manager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI) }

        onDispose { manager.unregisterListener(listener) }
    }

    return state
}
