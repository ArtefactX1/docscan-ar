package com.zacky.docar.gambar

import kotlin.math.abs

/**
 * Statistik kecerahan dari bidang Y (luma) frame kamera. Rentang nilai 0..255.
 */
object Cahaya {
    fun rataRata(y: ByteArray): Float {
        if (y.isEmpty()) return 0f
        var jumlah = 0L
        for (b in y) jumlah += b.toInt() and 0xFF
        return jumlah.toFloat() / y.size
    }

    /**
     * Bayangan kiri-kanan: selisih rata-rata sepertiga kiri dan kanan dibagi
     * rata-rata frame.
     */
    fun bayanganKiriKanan(
        y: ByteArray,
        lebar: Int,
        tinggi: Int,
        ambang: Float = 0.25f,
    ): Boolean {
        if (lebar < 3 || tinggi <= 0 || y.size < lebar * tinggi) return false
        val sisi = lebar / 3
        if (sisi == 0) return false

        var kiri = 0L
        var kanan = 0L
        var n = 0
        for (baris in 0 until tinggi) {
            val dasar = baris * lebar
            for (x in 0 until sisi) {
                kiri += y[dasar + x].toInt() and 0xFF
                kanan += y[dasar + lebar - 1 - x].toInt() and 0xFF
                n++
            }
        }
        if (n == 0) return false

        val tengah = rataRata(y)
        if (tengah <= 0f) return false
        return abs(kiri.toFloat() / n - kanan.toFloat() / n) / tengah > ambang
    }
}
