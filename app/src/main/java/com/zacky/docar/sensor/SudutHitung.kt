package com.zacky.docar.sensor

import kotlin.math.acos
import kotlin.math.sqrt

object SudutHitung {
    /**
     * Sudut (derajat) antara vektor gravitasi dan arah layar; 0° berarti HP
     * sejajar kertas dengan layar menghadap kertas.
     */
    fun kemiringan(ax: Float, ay: Float, az: Float): Float {
        val panjang = sqrt(ax * ax + ay * ay + az * az)
        if (panjang < 0.1f) return 0f
        val cos = (-az / panjang).coerceIn(-1f, 1f)
        return Math.toDegrees(acos(cos).toDouble()).toFloat()
    }
}
