package com.zacky.docar.sensor

object Kesiapan {
    const val SUDUT_MAKS = 2f
    const val LUX_MIN = 50f
    const val TERANG_MIN = 70f
    const val PUTARAN_MAKS = 0.35f

    /**
     * lux null = perangkat tanpa sensor cahaya, jangan blokir capture. terangFrame
     * adalah rata-rata luma frame (0..255).
     */
    fun siap(
        sudut: Float,
        guncangan: Boolean,
        berputar: Boolean,
        lux: Float?,
        adaDokumen: Boolean = true,
        terangFrame: Float? = null,
        sudutMaks: Float = SUDUT_MAKS,
    ): Boolean =
        sudut <= sudutMaks &&
            !guncangan &&
            !berputar &&
            adaDokumen &&
            (lux == null || lux >= LUX_MIN) &&
            (terangFrame == null || terangFrame >= TERANG_MIN)
}
