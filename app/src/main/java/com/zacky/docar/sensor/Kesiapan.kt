package com.zacky.docar.sensor

object Kesiapan {
    const val SUDUT_MAKS = 8f
    const val LUX_MIN = 50f
    const val TERANG_MIN = 70f
    const val PUTARAN_MAKS = 0.35f

    /** Batas keras untuk auto-capture; [SUDUT_MAKS] hanya peringatan di layar. */
    const val SUDUT_BLOKIR = 15f

    fun siap(
        lux: Float?,
        terangFrame: Float? = null,
        sudut: Float = 0f,
    ): Boolean =
        sudut <= SUDUT_BLOKIR &&
            (lux == null || lux >= LUX_MIN) &&
            (terangFrame == null || terangFrame >= TERANG_MIN)

    /** Cukup datar untuk hasil OCR yang baik; dipakai untuk mewarnai telemetri. */
    fun cukupDatar(sudut: Float, sudutMaks: Float = SUDUT_MAKS): Boolean = sudut <= sudutMaks
}
