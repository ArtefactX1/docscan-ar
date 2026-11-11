package com.zacky.docar.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.zacky.docar.gambar.Orientasi
import com.zacky.docar.gambar.Pemindai
import com.zacky.docar.ocr.BahasaOcr
import com.zacky.docar.ocr.BarisOcr
import com.zacky.docar.ocr.Klasifikasi
import com.zacky.docar.ocr.KotakTeks
import com.zacky.docar.ocr.OcrEngine
import com.zacky.docar.pdf.HalamanPdf
import com.zacky.docar.pdf.PdfBuat
import java.io.File
import java.io.FileOutputStream

/**
 * PDF selalu dibangun ulang dari halaman, jadi urutan dan penghapusan cukup
 * mengubah tabel.
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

    suspend fun tambahHalaman(
        dokumenId: Long,
        bitmap: Bitmap,
        bahasa: BahasaOcr = BahasaOcr.LATIN,
    ): Long {
        val (gambar, baris) = bacaTegak(bitmap, bahasa)
        val dasar = "h_${System.currentTimeMillis()}"

        val berkas = File(folderHalaman, "$dasar.jpg")
        FileOutputStream(berkas).use { gambar.compress(Bitmap.CompressFormat.JPEG, 92, it) }

        val kecil = Bitmap.createScaledBitmap(
            gambar,
            240,
            (240f * gambar.height / gambar.width).toInt().coerceAtLeast(1),
            true,
        )
        val thumb = File(folderHalaman, "${dasar}_t.jpg")
        FileOutputStream(thumb).use { kecil.compress(Bitmap.CompressFormat.JPEG, 80, it) }

        val id = db.tambahHalaman(
            dokumenId = dokumenId,
            gambar = berkas.absolutePath,
            thumb = thumb.absolutePath,
            teks = baris.joinToString("\n") { it.teks },
            kotak = KotakTeks.keJson(baris),
        )
        // Halaman pertama: beri nama dokumen sesuai isinya kalau jenisnya dikenali.
        if (db.halaman(dokumenId).size == 1) {
            val jenis = Klasifikasi.jenis(baris.joinToString(" ") { it.teks })
            val lama = dokumenById(dokumenId)?.nama.orEmpty()
            if (jenis != Klasifikasi.Jenis.UMUM && lama.startsWith("Scan ")) {
                db.ubahNama(dokumenId, jenis.label + lama.removePrefix("Scan"))
            }
        }
        bangunPdf(dokumenId)
        return id
    }

    /**
     * Halaman miring menghasilkan teks OCR jauh lebih sedikit. Kalau hasil awal
     * tipis, tiga rotasi dicoba pada versi kecil lalu OCR diulang pada bitmap final.
     */
    private suspend fun bacaTegak(
        bitmap: Bitmap,
        bahasa: BahasaOcr,
    ): Pair<Bitmap, List<BarisOcr>> {
        val awal = OcrEngine.baca(bitmap, bahasa)
        if (!Orientasi.perluCobaLain(awal.sumOf { it.teks.length })) return bitmap to awal

        val kecil = Pemindai.kecilkan(bitmap, 480)
        val jumlah = IntArray(4)
        jumlah[0] = awal.sumOf { it.teks.length }
        for (i in 1..3) {
            val putar = Pemindai.putar(kecil, i * 90)
            jumlah[i] = OcrEngine.baca(putar, bahasa).sumOf { it.teks.length }
        }

        val pilihan = Orientasi.terbaik(jumlah)
        if (pilihan == 0) return bitmap to awal

        val tegak = Pemindai.putar(bitmap, pilihan * 90)
        return tegak to OcrEngine.baca(tegak, bahasa)
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

    /**
     * [sorot] hanya untuk ekspor, jadi hasilnya file terpisah dan PDF arsip tetap bersih.
     */
    suspend fun bangunPdf(dokumenId: Long, sorot: String? = null): File? {
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

        val file = if (sorot.isNullOrBlank()) {
            pdfDari(dokumenId)
        } else {
            File(folderPdf, "dok_${dokumenId}_sorot.pdf")
        }
        PdfBuat.buat(
            context,
            halaman,
            file,
            dokumenById(dokumenId)?.nama ?: "Dokumen",
            sorot,
        )
        return file
    }
}
