package com.buuzz.docscanar.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.buuzz.docscanar.ocr.KotakTeks
import com.buuzz.docscanar.ocr.OcrEngine
import com.buuzz.docscanar.pdf.HalamanPdf
import com.buuzz.docscanar.pdf.PdfBuat
import java.io.File
import java.io.FileOutputStream

/**
 * Menyatukan database, berkas halaman, dan PDF. PDF selalu dibangun ulang dari
 * halaman, jadi urutan dan penghapusan cukup mengubah tabel.
 */
class Repositori(private val context: Context, private val db: DokumenDb) {

    private val folderHalaman = File(context.filesDir, "halaman").apply { mkdirs() }
    private val folderPdf = File(context.filesDir, "pdf").apply { mkdirs() }

    fun dokumen(): List<Dokumen> = db.dokumen()

    fun cari(kata: String): List<Dokumen> = db.cari(kata)

    fun dokumenById(id: Long): Dokumen? = db.dokumenById(id)

    fun halaman(dokumenId: Long): List<Halaman> = db.halaman(dokumenId)

    fun buatDokumen(nama: String): Long = db.tambahDokumen(nama)

    fun ubahNama(id: Long, nama: String) = db.ubahNama(id, nama)

    suspend fun tambahHalaman(dokumenId: Long, bitmap: Bitmap): Long {
        val baris = OcrEngine.baca(bitmap)
        val dasar = "h_${System.currentTimeMillis()}"

        val gambar = File(folderHalaman, "$dasar.jpg")
        FileOutputStream(gambar).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }

        val kecil = Bitmap.createScaledBitmap(
            bitmap,
            240,
            (240f * bitmap.height / bitmap.width).toInt().coerceAtLeast(1),
            true,
        )
        val thumb = File(folderHalaman, "${dasar}_t.jpg")
        FileOutputStream(thumb).use { kecil.compress(Bitmap.CompressFormat.JPEG, 80, it) }

        val id = db.tambahHalaman(
            dokumenId = dokumenId,
            gambar = gambar.absolutePath,
            thumb = thumb.absolutePath,
            teks = baris.joinToString("\n") { it.teks },
            kotak = KotakTeks.keJson(baris),
        )
        bangunPdf(dokumenId)
        return id
    }

    suspend fun hapusHalaman(halaman: Halaman) {
        db.hapusHalaman(halaman)
        runCatching { File(halaman.gambar).delete() }
        runCatching { File(halaman.thumb).delete() }
        bangunPdf(halaman.dokumenId)
    }

    suspend fun urutkanUlang(dokumenId: Long, urutanBaru: List<Long>) {
        db.urutkanUlang(dokumenId, urutanBaru)
        bangunPdf(dokumenId)
    }

    suspend fun hapusDokumen(id: Long) {
        db.halaman(id).forEach { h ->
            runCatching { File(h.gambar).delete() }
            runCatching { File(h.thumb).delete() }
        }
        db.hapusDokumen(id)
        runCatching { pdfDari(id).delete() }
    }

    fun pdfDari(dokumenId: Long): File = File(folderPdf, "dok_$dokumenId.pdf")

    suspend fun bangunPdf(dokumenId: Long): File? {
        val daftar = db.halaman(dokumenId)
        if (daftar.isEmpty()) {
            runCatching { pdfDari(dokumenId).delete() }
            return null
        }

        val halaman = daftar.mapNotNull { h ->
            val bitmap = BitmapFactory.decodeFile(h.gambar) ?: return@mapNotNull null
            HalamanPdf(bitmap, KotakTeks.dariJson(h.kotak))
        }
        if (halaman.isEmpty()) return null

        val file = pdfDari(dokumenId)
        PdfBuat.buat(context, halaman, file)
        return file
    }
}
