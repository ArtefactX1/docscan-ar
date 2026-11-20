package com.zacky.docar.kamera

/**
 * Satu frame gagal deteksi jangan langsung menghapus overlay: Canny pada frame
 * 480 px sering miss sesaat karena cahaya atau gerakan. Quad terakhir ditahan
 * sampai [maksGagal] frame gagal berturut, baru benar-benar dilepas.
 */
class PenyanggaDeteksi(private val maksGagal: Int = 5) {
    private var terakhir: SegiLayar? = null
    private var gagal = 0

    fun terima(hasil: SegiLayar?): SegiLayar? {
        if (hasil != null) {
            terakhir = hasil
            gagal = 0
            return hasil
        }
        gagal++
        if (gagal > maksGagal) {
            terakhir = null
            return null
        }
        return terakhir
    }

    fun reset() {
        terakhir = null
        gagal = 0
    }
}
