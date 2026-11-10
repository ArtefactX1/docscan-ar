package com.buuzz.docscanar.kamera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.buuzz.docscanar.gambar.Pemindai
import com.buuzz.docscanar.gambar.Titik
import com.buuzz.docscanar.gambar.UrutSudut
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/** Segi empat dokumen dalam koordinat layar ternormalisasi (0..1) plus rasio gambar. */
data class SegiLayar(val titik: List<Titik>, val rasio: Float)

/**
 * Deteksi tepi pada frame kamera langsung. Dijalankan maksimal ~8 kali per detik
 * pada lebar 480 px supaya tidak memblokir preview.
 */
class AnalisDokumen(
    private val onSegi: (SegiLayar?) -> Unit,
) : ImageAnalysis.Analyzer {

    private var terakhir = 0L

    override fun analyze(image: ImageProxy) {
        val sekarang = System.currentTimeMillis()
        if (sekarang - terakhir < 120) {
            image.close()
            return
        }
        terakhir = sekarang

        try {
            onSegi(cari(image))
        } catch (t: Throwable) {
            onSegi(null)
        } finally {
            image.close()
        }
    }

    private fun cari(image: ImageProxy): SegiLayar? {
        if (!Pemindai.pastikanOpenCv()) return null

        val lebar = image.width
        val tinggi = image.height
        if (lebar <= 0 || tinggi <= 0) return null

        val abu = Mat(tinggi, lebar, CvType.CV_8UC1)
        abu.put(0, 0, ambilY(image))

        val skala = 480.0 / lebar
        val kecil = Mat()
        Imgproc.resize(abu, kecil, Size(lebar * skala, tinggi * skala))

        val segi = Pemindai.cariSegiEmpat(kecil) ?: return null

        val derajat = image.imageInfo.rotationDegrees
        val ternormal = segi.map { Titik(it.x / kecil.cols(), it.y / kecil.rows()) }
        val diputar = when (derajat) {
            90 -> ternormal.map { Titik(1f - it.y, it.x) }
            180 -> ternormal.map { Titik(1f - it.x, 1f - it.y) }
            270 -> ternormal.map { Titik(it.y, 1f - it.x) }
            else -> ternormal
        }

        val rasio = if (derajat == 90 || derajat == 270) {
            kecil.rows().toFloat() / kecil.cols()
        } else {
            kecil.cols().toFloat() / kecil.rows()
        }

        return SegiLayar(UrutSudut.urut(diputar), rasio)
    }

    private fun ambilY(image: ImageProxy): ByteArray {
        val lebar = image.width
        val tinggi = image.height
        val bidang = image.planes[0]
        val buffer = bidang.buffer
        val baris = bidang.rowStride
        val piksel = bidang.pixelStride
        val keluar = ByteArray(lebar * tinggi)

        if (piksel == 1 && baris == lebar) {
            buffer.rewind()
            buffer.get(keluar, 0, minOf(keluar.size, buffer.remaining()))
            return keluar
        }

        val satuBaris = ByteArray(baris)
        var tulis = 0
        for (y in 0 until tinggi) {
            buffer.position(y * baris)
            val panjang = minOf(baris, buffer.remaining())
            buffer.get(satuBaris, 0, panjang)
            for (x in 0 until lebar) {
                keluar[tulis++] = satuBaris[x * piksel]
            }
        }
        return keluar
    }
}
