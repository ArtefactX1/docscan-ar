package com.zacky.docar.kamera

/**
 * Cari kertas → dekatkan → tahan diam → siap. Boolean tunggal membuat pengguna
 * tidak tahu apa yang kurang.
 */
enum class StatusCapture { SEARCH, CLOSER, DO_NOT_MOVE, SIAP }

/**
 * Butuh [frameStabil] frame berturut dengan posisi stabil; satu frame goyang
 * me-reset hitungan supaya capture tidak terpicu dini.
 */
class MesinCapture(
    private val luasMin: Float = 0.15f,
    private val frameStabil: Int = 3,
) {
    private var stabilBerturut = 0

    fun perbarui(
        adaKertas: Boolean,
        luasRasio: Float,
        posisiStabil: Boolean,
        sensorSiap: Boolean,
    ): StatusCapture {
        if (!adaKertas) {
            stabilBerturut = 0
            return StatusCapture.SEARCH
        }
        if (luasRasio < luasMin) {
            stabilBerturut = 0
            return StatusCapture.CLOSER
        }
        if (!posisiStabil || !sensorSiap) {
            stabilBerturut = 0
            return StatusCapture.DO_NOT_MOVE
        }
        stabilBerturut++
        return if (stabilBerturut >= frameStabil) StatusCapture.SIAP else StatusCapture.DO_NOT_MOVE
    }

    fun reset() {
        stabilBerturut = 0
    }
}
