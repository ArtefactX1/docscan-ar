package com.zacky.docar.kamera

/**
 * Cari kertas → dekatkan → tahan diam → siap. Boolean tunggal membuat pengguna
 * tidak tahu apa yang kurang.
 */
enum class StatusCapture { SEARCH, CLOSER, DO_NOT_MOVE, SIAP }

/**
 * Butuh posisi stabil selama [tahanMs]; satu frame bergeser me-reset hitungan.
 * Waktu dipakai, bukan jumlah frame, supaya fps analyzer yang berubah-ubah tidak
 * mengubah lama tahan.
 */
class MesinCapture(
    private val luasMin: Float = 0.08f,
    private val tahanMs: Long = 700L,
) {
    private var mulaiStabil: Long? = null

    var progres: Float = 0f
        private set

    fun perbarui(
        adaKertas: Boolean,
        luasRasio: Float,
        posisiStabil: Boolean,
        sensorSiap: Boolean,
        sekarang: Long,
    ): StatusCapture {
        if (!adaKertas) {
            reset()
            return StatusCapture.SEARCH
        }
        val dekat = luasRasio >= luasMin
        if (!posisiStabil || !sensorSiap) {
            // Goyang sesaat memotong sebagian progres, bukan menghapus semuanya:
            // reset penuh membuat jitter satu frame menggagalkan capture terus-menerus.
            val awal = mulaiStabil
            if (awal != null) {
                val sisa = (sekarang - awal - 250L).coerceAtLeast(0L)
                mulaiStabil = sekarang - sisa
                progres = (sisa.toFloat() / tahanMs).coerceIn(0f, 1f)
            }
            return if (dekat) StatusCapture.DO_NOT_MOVE else StatusCapture.CLOSER
        }
        val awal = mulaiStabil ?: sekarang.also { mulaiStabil = it }
        val lama = sekarang - awal
        progres = (lama.toFloat() / tahanMs).coerceIn(0f, 1f)
        if (lama >= tahanMs) return StatusCapture.SIAP
        return if (dekat) StatusCapture.DO_NOT_MOVE else StatusCapture.CLOSER
    }

    fun reset() {
        mulaiStabil = null
        progres = 0f
    }
}
