package com.zacky.docar.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.zacky.docar.ocr.BahasaOcr

/**
 * SharedPreferences cukup: beberapa nilai, tanpa alur async.
 * Properti memakai state Compose supaya layar ikut berubah saat disetel.
 */
class Pengaturan(context: Context) {
    private val p = context.getSharedPreferences("pengaturan", Context.MODE_PRIVATE)

    var sudutMaks by mutableFloatStateOf(p.getFloat(KUNCI_SUDUT, 2f))
        private set

    /** 0 = otomatis mengikuti cahaya, 1 = selalu nyala, 2 = selalu mati. */
    var modeSenter by mutableIntStateOf(p.getInt(KUNCI_SENTER, 0))
        private set

    /** Indeks [com.zacky.docar.ocr.BahasaOcr]. */
    var bahasaOcr by mutableIntStateOf(p.getInt(KUNCI_BAHASA, 0))
        private set

    fun ubahSudut(nilai: Float) {
        sudutMaks = nilai.coerceIn(1f, 8f)
        p.edit().putFloat(KUNCI_SUDUT, sudutMaks).apply()
    }

    fun ubahSenter(nilai: Int) {
        modeSenter = nilai.coerceIn(0, 2)
        p.edit().putInt(KUNCI_SENTER, modeSenter).apply()
    }

    fun ubahBahasa(nilai: Int) {
        bahasaOcr = nilai.coerceIn(0, BahasaOcr.entries.lastIndex)
        p.edit().putInt(KUNCI_BAHASA, bahasaOcr).apply()
    }

    private companion object {
        const val KUNCI_SUDUT = "sudut_maks"
        const val KUNCI_SENTER = "mode_senter"
        const val KUNCI_BAHASA = "bahasa_ocr"
    }
}
