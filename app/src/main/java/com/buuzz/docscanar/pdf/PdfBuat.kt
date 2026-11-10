package com.buuzz.docscanar.pdf

import android.content.Context
import android.graphics.Bitmap
import com.buuzz.docscanar.ocr.BarisOcr
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.graphics.state.RenderingMode
import java.io.ByteArrayOutputStream
import java.io.File

data class HalamanPdf(val gambar: Bitmap, val baris: List<BarisOcr>)

object PdfBuat {
    private var siap = false

    fun buat(context: Context, halaman: List<HalamanPdf>, file: File) {
        if (!siap) {
            PDFBoxResourceLoader.init(context.applicationContext)
            siap = true
        }

        PDDocument().use { dokumen ->
            halaman.forEach { h ->
                val halamanPdf = PDPage(PDRectangle.A4)
                dokumen.addPage(halamanPdf)

                val jpeg = ByteArrayOutputStream().use { aliran ->
                    h.gambar.compress(Bitmap.CompressFormat.JPEG, 92, aliran)
                    aliran.toByteArray()
                }
                val gambar = PDImageXObject.createFromByteArray(dokumen, jpeg, "halaman")

                PDPageContentStream(dokumen, halamanPdf).use { konten ->
                    konten.drawImage(gambar, 0f, 0f, PDRectangle.A4.width, PDRectangle.A4.height)

                    // Teks tak terlihat: bisa dicari dan disalin, tidak menutupi gambar.
                    konten.setRenderingMode(RenderingMode.NEITHER)

                    val skalaX = PDRectangle.A4.width / h.gambar.width
                    val skalaY = PDRectangle.A4.height / h.gambar.height

                    h.baris.forEach { b ->
                        val teks = bersihkan(b.teks)
                        if (teks.isEmpty()) return@forEach

                        val tinggi = ((b.bawah - b.atas) * skalaY * 0.8f).coerceIn(4f, 24f)
                        konten.beginText()
                        konten.setFont(PDType1Font.HELVETICA, tinggi)
                        konten.newLineAtOffset(
                            b.kiri * skalaX,
                            PDRectangle.A4.height - b.bawah * skalaY,
                        )
                        konten.showText(teks)
                        konten.endText()
                    }
                }
            }

            dokumen.save(file)
        }
    }

    /** Font standar PDF hanya mendukung WinAnsi; ganti karakter di luar itu. */
    private fun bersihkan(teks: String): String = teks
        .map { if (it.code in 32..255) it else ' ' }
        .joinToString("")
        .trim()
}
