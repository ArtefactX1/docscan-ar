package com.buuzz.docscanar.sensor

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Deteksi hentakan dari akselerometer: bandingkan magnitudo sesaat dengan
 * rata-rata bergerak (low-pass). Selisih di atas ambang berarti tangan goyah.
 */
class Guncangan(
    private val ambang: Float = 2.0f,
    private val alpha: Float = 0.1f,
) {
    private var dasar = 9.81f

    fun perbarui(ax: Float, ay: Float, az: Float): Boolean {
        val magnitudo = sqrt(ax * ax + ay * ay + az * az)
        dasar += (magnitudo - dasar) * alpha
        return abs(magnitudo - dasar) > ambang
    }
}
