package com.buuzz.docscanar.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

data class BarisOcr(
    val teks: String,
    val kiri: Float,
    val atas: Float,
    val kanan: Float,
    val bawah: Float,
)

object OcrEngine {
    private val klien by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /** Baris teks beserta kotak batasnya, dalam koordinat piksel bitmap masukan. */
    suspend fun baca(bitmap: Bitmap): List<BarisOcr> {
        val hasil = klien.process(InputImage.fromBitmap(bitmap, 0)).await()
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
}
