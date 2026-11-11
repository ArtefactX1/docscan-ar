package com.zacky.docar.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class BarisOcr(
    val teks: String,
    val kiri: Float,
    val atas: Float,
    val kanan: Float,
    val bawah: Float,
)

/** Urutan sama dengan nilai tersimpan di SharedPreferences. */
enum class BahasaOcr(val label: String) {
    LATIN("Latin (ID/EN)"),
    CHINA("Tionghoa"),
    JEPANG("Jepang"),
    KOREA("Korea"),
    DEVANAGARI("Devanagari"),
    ;

    companion object {
        fun dari(nilai: Int): BahasaOcr = entries.getOrElse(nilai) { LATIN }
    }
}

object OcrEngine {
    private val klien = HashMap<BahasaOcr, TextRecognizer>()

    private fun klien(bahasa: BahasaOcr): TextRecognizer = klien.getOrPut(bahasa) {
        when (bahasa) {
            BahasaOcr.LATIN -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            BahasaOcr.CHINA ->
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            BahasaOcr.JEPANG ->
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            BahasaOcr.KOREA ->
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            BahasaOcr.DEVANAGARI ->
                TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        }
    }

    /** Baris teks beserta kotak batasnya, dalam koordinat piksel bitmap masukan. */
    suspend fun baca(bitmap: Bitmap, bahasa: BahasaOcr = BahasaOcr.LATIN): List<BarisOcr> {
        val hasil = klien(bahasa).process(InputImage.fromBitmap(bitmap, 0)).await()
        return hasil.textBlocks
            .flatMap { it.lines }
            .mapNotNull { baris ->
                val kotak = baris.boundingBox ?: return@mapNotNull null
                if (baris.text.isBlank()) return@mapNotNull null
                BarisOcr(
                    teks = baris.text,
                    kiri = kotak.left.toFloat(),
                    atas = kotak.top.toFloat(),
                    kanan = kotak.right.toFloat(),
                    bawah = kotak.bottom.toFloat(),
                )
            }
    }

    /**
     * Tidak ada API pra-unduh untuk text recognition, jadi jalankan satu frame
     * kosong sebagai pemicu dan biarkan Play Services mengunduh modelnya.
     */
    suspend fun siapkanModel(bahasa: BahasaOcr): Boolean {
        val kosong = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.WHITE)
        }
        return runCatching {
            klien(bahasa).process(InputImage.fromBitmap(kosong, 0)).await()
            true
        }.getOrDefault(false)
    }
}
