package com.zacky.docar.sensor

object Kesiapan {
    const val SUDUT_MAKS = 8f
    const val LUX_MIN = 50f
    const val TERANG_MIN = 70f
    const val PUTARAN_MAKS = 0.35f

    /**
     * Sudut tidak menghalangi capture: koreksi perspektif menangani kemiringan.
     * Sudut ekstrem hanya membuat kertas memendek dan OCR turun, jadi cukup
     * jadi peringatan di HUD.
     */
    fun siap(
        guncangan: Boolean,
        berputar: Boolean,
        lux: Float?,
        terangFrame: Float? = null,
    ): Boolean =
        !guncangan &&
            !berputar &&
            (lux == null || lux >= LUX_MIN) &&
            (terangFrame == null || terangFrame >= TERANG_MIN)

    /** Cukup datar untuk hasil OCR yang baik; dipakai untuk mewarnai chip, bukan gerbang. */
    fun cukupDatar(sudut: Float, sudutMaks: Float = SUDUT_MAKS): Boolean = sudut <= sudutMaks
}
