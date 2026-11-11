package com.zacky.docar.kamera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.zacky.docar.gambar.Cahaya
import com.zacky.docar.gambar.Pemindai
import com.zacky.docar.gambar.Stabilkan
import com.zacky.docar.gambar.Titik
import com.zacky.docar.gambar.UrutSudut
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

/** Segi empat dokumen dalam koordinat layar ternormalisasi (0..1) plus rasio gambar. */
data class SegiLayar(
    val titik: List<Titik>,
    val rasio: Float,
    /** Luas quad ternormalisasi (0..1), dipakai untuk menilai kertas terlalu jauh. */
    val luas: Float,
    /** Posisi hampir sama dengan frame sebelumnya (IoU tinggi). */
    val stabil: Boolean,
)

/**
 * Deteksi tepi pada frame kamera langsung, maksimal ~8 kali per detik pada lebar
 * 480 px supaya preview tidak tersendat.
 */
class AnalisDokumen(
    private val onSegi: (SegiLayar?) -> Unit,
    private val onCahaya: (terang: Float, bayangan: Boolean) -> Unit = { _, _ -> },
) : ImageAnalysis.Analyzer {

    private var terakhir = 0L
    private var sebelumnya: List<Titik>? = null

    override fun analyze(image: ImageProxy) {
        val sekarang = System.currentTimeMillis()
        if (sekarang - terakhir < 120) {
            image.close()
            return
        }
        terakhir = sekarang

        try {
            val y = ambilY(image)
            onCahaya(
                Cahaya.rataRata(y),
                Cahaya.bayanganKiriKanan(y, image.width, image.height),
            )
            onSegi(cari(image, y))
        } catch (t: Throwable) {
            onSegi(null)
        } finally {
            image.close()
        }
    }

    private fun cari(image: ImageProxy, y: ByteArray): SegiLayar? {
        if (!Pemindai.pastikanOpenCv()) return null

        val lebar = image.width
        val tinggi = image.height
        if (lebar <= 0 || tinggi <= 0) return null

        val abu = Mat(tinggi, lebar, CvType.CV_8UC1)
        abu.put(0, 0, y)

        val skala = 480.0 / lebar
        val kecil = Mat()
        Imgproc.resize(abu, kecil, Size(lebar * skala, tinggi * skala))

        val kandidat = Pemindai.cariSegiEmpatKandidat(kecil, 3)
        if (kandidat.isEmpty()) {
            sebelumnya = null
            return null
        }

        val derajat = image.imageInfo.rotationDegrees
        val ternormal = kandidat.map { segi ->
            val t = segi.map { Titik(it.x / kecil.cols(), it.y / kecil.rows()) }
            when (derajat) {
                90 -> t.map { Titik(1f - it.y, it.x) }
                180 -> t.map { Titik(1f - it.x, 1f - it.y) }
                270 -> t.map { Titik(it.y, 1f - it.x) }
                else -> t
            }.map { Titik(it.x.coerceIn(0f, 1f), it.y.coerceIn(0f, 1f)) }
        }

        val rasio = if (derajat == 90 || derajat == 270) {
            kecil.rows().toFloat() / kecil.cols()
        } else {
            kecil.cols().toFloat() / kecil.rows()
        }

        val lama = sebelumnya
        // Pilih kandidat yang paling cocok dengan frame sebelumnya, fallback ke yang terluas.
        val pilih = if (lama != null) {
            ternormal.maxByOrNull { Stabilkan.iou(lama, UrutSudut.urut(it)) } ?: ternormal[0]
        } else {
            ternormal[0]
        }
        val urut = UrutSudut.urut(pilih)
        val stabil = lama != null && Stabilkan.iou(lama, urut) >= IOU_STABIL
        val halus = if (stabil) Stabilkan.halus(lama!!, urut, ALPHA_HALUS) else urut
        sebelumnya = halus

        return SegiLayar(halus, rasio, Stabilkan.luas(halus), stabil)
    }

    private companion object {
        const val IOU_STABIL = 0.6f
        const val ALPHA_HALUS = 0.35f
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
