package com.buuzz.docscanar.gambar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

enum class ModeWarna { ASLI, ABU }

data class HasilScan(val halaman: Bitmap, val sudut: List<Titik>)

object Pemindai {
    private const val SISI_MAKS = 2000
    private const val LEBAR_A4 = 1240.0
    private const val TINGGI_A4 = 1754.0

    private var openCvSiap = false

    fun pastikanOpenCv(): Boolean {
        if (!openCvSiap) openCvSiap = OpenCVLoader.initLocal()
        return openCvSiap
    }

    fun bacaGambar(file: File): Bitmap? {
        val ukuran = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, ukuran)
        if (ukuran.outWidth <= 0) return null

        var skala = 1
        while (ukuran.outWidth / skala > SISI_MAKS || ukuran.outHeight / skala > SISI_MAKS) {
            skala *= 2
        }

        val bitmap = BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = skala },
        ) ?: return null

        val derajat = when (
            ExifInterface(file.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (derajat == 0f) return bitmap

        val putar = Matrix().apply { postRotate(derajat) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, putar, true)
    }

    /** Cadangan saat deteksi gagal: 8% margin, supaya pengguna tetap bisa menggeser sudut. */
    fun sudutCadangan(lebar: Int, tinggi: Int): List<Titik> {
        val mx = lebar * 0.08f
        val my = tinggi * 0.08f
        return listOf(
            Titik(mx, my),
            Titik(lebar - mx, my),
            Titik(lebar - mx, tinggi - my),
            Titik(mx, tinggi - my),
        )
    }

    fun cariTepi(bitmap: Bitmap): List<Titik>? {
        if (!pastikanOpenCv()) return null

        val asli = Mat()
        Utils.bitmapToMat(bitmap, asli)

        val faktor = 1000.0 / maxOf(asli.cols(), asli.rows())
        val kecil = Mat()
        if (faktor < 1.0) {
            Imgproc.resize(asli, kecil, Size(asli.cols() * faktor, asli.rows() * faktor))
        } else {
            asli.copyTo(kecil)
        }

        val abu = Mat()
        Imgproc.cvtColor(kecil, abu, Imgproc.COLOR_RGBA2GRAY)

        val segi = cariSegiEmpat(abu) ?: return null
        val pembesar = bitmap.width.toFloat() / kecil.cols()
        return UrutSudut.urut(
            segi.map { Titik(it.x * pembesar, it.y * pembesar) }
        )
    }

    /**
     * Cari segi empat terbesar pada matriks grayscale. Koordinat hasil memakai
     * skala matriks masukan, bukan bitmap asal.
     */
    fun cariSegiEmpat(abu: Mat): List<Titik>? {
        if (!pastikanOpenCv()) return null

        val halus = Mat()
        Imgproc.GaussianBlur(abu, halus, Size(5.0, 5.0), 0.0)

        val tepi = Mat()
        Imgproc.Canny(halus, tepi, 50.0, 150.0)
        Imgproc.dilate(
            tepi,
            tepi,
            Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0)),
        )

        val kontur = ArrayList<MatOfPoint>()
        Imgproc.findContours(tepi, kontur, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)

        val luasMin = abu.cols() * abu.rows() * 0.2
        var terbaik: MatOfPoint? = null
        var luasTerbaik = 0.0

        for (k in kontur) {
            val keliling = Imgproc.arcLength(MatOfPoint2f(*k.toArray()), true)
            val perkiraan = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*k.toArray()), perkiraan, 0.02 * keliling, true)

            if (perkiraan.total() != 4L) continue
            val segi = MatOfPoint(*perkiraan.toArray())
            if (!Imgproc.isContourConvex(segi)) continue

            val luas = Imgproc.contourArea(perkiraan)
            if (luas > luasMin && luas > luasTerbaik) {
                luasTerbaik = luas
                terbaik = segi
            }
        }

        val segi = terbaik ?: return null
        return UrutSudut.urut(segi.toList().map { Titik(it.x.toFloat(), it.y.toFloat()) })
    }

    fun luruskan(bitmap: Bitmap, sudut: List<Titik>): Bitmap {
        val asli = Mat()
        Utils.bitmapToMat(bitmap, asli)

        val asal = MatOfPoint2f(
            *sudut.map { Point(it.x.toDouble(), it.y.toDouble()) }.toTypedArray()
        )
        val tujuan = MatOfPoint2f(
            Point(0.0, 0.0),
            Point(LEBAR_A4, 0.0),
            Point(LEBAR_A4, TINGGI_A4),
            Point(0.0, TINGGI_A4),
        )

        val matriks = Imgproc.getPerspectiveTransform(asal, tujuan)
        val keluar = Mat()
        Imgproc.warpPerspective(asli, keluar, matriks, Size(LEBAR_A4, TINGGI_A4))

        val hasil = Bitmap.createBitmap(LEBAR_A4.toInt(), TINGGI_A4.toInt(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(keluar, hasil)
        return hasil
    }

    /**
     * ASLI: hanya koreksi perspektif + kontras lembut; warna dan rona kertas dibiarkan.
     * ABU: bayangan dibuang (bagi dengan blur), abu-abu, lalu CLAHE tajam untuk teks.
     */
    fun perjelas(bitmap: Bitmap, warna: ModeWarna): Bitmap {
        val asli = Mat()
        Utils.bitmapToMat(bitmap, asli)

        val hasil: Mat = when (warna) {
            ModeWarna.ABU -> {
                val abu = Mat()
                Imgproc.cvtColor(asli, abu, Imgproc.COLOR_RGBA2GRAY)
                val latar = Mat()
                Imgproc.medianBlur(abu, latar, 31)
                val rata = Mat()
                Core.divide(abu, latar, rata, 255.0)
                val keluar = Mat()
                Imgproc.createCLAHE(2.5, Size(8.0, 8.0)).apply(rata, keluar)
                keluar
            }

            ModeWarna.ASLI -> {
                val lab = Mat()
                Imgproc.cvtColor(asli, lab, Imgproc.COLOR_RGBA2RGB)
                Imgproc.cvtColor(lab, lab, Imgproc.COLOR_RGB2Lab)

                val kanal = ArrayList<Mat>(3)
                Core.split(lab, kanal)

                val terang = Mat()
                Imgproc.createCLAHE(1.5, Size(8.0, 8.0)).apply(kanal[0], terang)

                Core.merge(listOf(terang, kanal[1], kanal[2]), lab)
                val rgb = Mat()
                Imgproc.cvtColor(lab, rgb, Imgproc.COLOR_Lab2RGB)
                val rgba = Mat()
                Imgproc.cvtColor(rgb, rgba, Imgproc.COLOR_RGB2RGBA)
                rgba
            }
        }

        val bitmapHasil = Bitmap.createBitmap(hasil.cols(), hasil.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(hasil, bitmapHasil)
        return bitmapHasil
    }

    fun rapikan(asli: Bitmap, sudut: List<Titik>, warna: ModeWarna): Bitmap {
        val luas = luasSegi(sudut)
        if (luas < asli.width * asli.height * 0.02) return asli
        return perjelas(luruskan(asli, sudut), warna)
    }

    private fun luasSegi(t: List<Titik>): Float {
        if (t.size != 4) return 0f
        var jumlah = 0f
        for (i in t.indices) {
            val a = t[i]
            val b = t[(i + 1) % 4]
            jumlah += a.x * b.y - b.x * a.y
        }
        return kotlin.math.abs(jumlah) / 2f
    }
}
