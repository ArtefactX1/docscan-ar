package com.buuzz.docscanar.sensor

object Kesiapan {
    const val SUDUT_MAKS = 2f
    const val LUX_MIN = 50f
    const val PUTARAN_MAKS = 0.35f

    /**
     * lux null berarti perangkat tidak punya sensor cahaya; jangan blokir capture karenanya.
     */
    fun siap(
        sudut: Float,
        guncangan: Boolean,
        berputar: Boolean,
        lux: Float?,
        adaDokumen: Boolean = true,
    ): Boolean =
        sudut <= SUDUT_MAKS &&
            !guncangan &&
            !berputar &&
            adaDokumen &&
            (lux == null || lux >= LUX_MIN)
}
