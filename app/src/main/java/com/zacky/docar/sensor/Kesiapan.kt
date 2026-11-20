package com.zacky.docar.sensor

object Kesiapan {
    const val SUDUT_MAKS = 8f
    const val LUX_MIN = 50f
    const val TERANG_MIN = 70f
    const val PUTARAN_MAKS = 0.35f

    /**
     * Tidak ada syarat gerak: auto-capture hanya perlu kertas terdeteksi dan quad
     * tidak berubah antar frame (lihat [com.zacky.docar.kamera.MesinCapture]).
     * Syarat sensor gerak dulu memblokir karena denyut tangan selalu terdeteksi.
     */
    fun siap(
        lux: Float?,
        terangFrame: Float? = null,
    ): Boolean =
        (lux == null || lux >= LUX_MIN) &&
            (terangFrame == null || terangFrame >= TERANG_MIN)

    /** Cukup datar untuk hasil OCR yang baik; dipakai untuk mewarnai chip, bukan gerbang. */
    fun cukupDatar(sudut: Float, sudutMaks: Float = SUDUT_MAKS): Boolean = sudut <= sudutMaks
}
