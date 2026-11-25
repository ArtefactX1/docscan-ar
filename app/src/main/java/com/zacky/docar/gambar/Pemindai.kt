package com.zacky.docar.gambar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File
import kotlin.math.hypot
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.MatOfPoint2f
import org.opencv.core.Point
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

enum class ModeWarna { ASLI, ABU, HITAM_PUTIH, LINEAR }

data class HasilScan(val halaman: Bitmap, val sudut: List<Titik>)

object Pemindai {
    private const val SISI_MAKS = 2000

    /** Kertas 8–80% frame; di luar itu bukan halaman yang wajar. */
    private const val LUAS_MIN = 0.08f
    private const val LUAS_MAKS = 0.80f

    /** Batas kebulatan quad dan dukungan tepi sebelum dianggap kertas. */
    private const val LONJONG_MAKS = 4.5f
    private const val TEPI_MIN = 0.5f

    private val EPSILON = listOf(0.02, 0.03, 0.05)
    private const val SISI_DETEKSI = 1200
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

    /**
     * Deteksi pasca-capture. Bitmap diperkecil ke 1200 px dulu: Canny 50/150 pada
     * 2000 px memecah tepi kertas jadi kontur kecil karena tekstur, sehingga
     * approxPolyDP tidak lagi menghasilkan 4 titik. Koordinat dikembalikan ke
     * skala bitmap asli.
     */
    fun cariTepi(bitmap: Bitmap): List<Titik>? {
        if (!pastikanOpenCv()) return null

        val rgba = Mat()
        Utils.bitmapToMat(bitmap, rgba)
        val abu = Mat()
        Imgproc.cvtColor(rgba, abu, Imgproc.COLOR_RGBA2GRAY)

        val sisiMaks = maxOf(abu.cols(), abu.rows())
        val faktor = if (sisiMaks > SISI_DETEKSI) SISI_DETEKSI.toDouble() / sisiMaks else 1.0
        val kecil = Mat()
        if (faktor < 1.0) {
            Imgproc.resize(abu, kecil, Size(abu.cols() * faktor, abu.rows() * faktor))
        } else {
            abu.copyTo(kecil)
        }

        val segi = cariSegiEmpatKandidat(kecil, 3).firstOrNull() ?: return null
        val pembesar = bitmap.width.toFloat() / kecil.cols()
        return UrutSudut.urut(segi.map { Titik(it.x * pembesar, it.y * pembesar) })
    }

    /**
     * Koordinat hasil memakai skala matriks masukan, bukan bitmap asal.
     */
    fun cariSegiEmpat(abu: Mat): List<Titik>? = cariSegiEmpatKandidat(abu, 1).firstOrNull()

    /**
     * Kandidat terbesar dulu, supaya preview bisa memilih yang paling konsisten
     * dengan frame sebelumnya saat ada kertas + meja.
     */
    fun cariSegiEmpatKandidat(abu: Mat, maks: Int = 3): List<List<Titik>> {
        if (!pastikanOpenCv()) return emptyList()

        val halus = Mat()
        Imgproc.GaussianBlur(abu, halus, Size(5.0, 5.0), 0.0)

        val lebar = abu.cols()
        val tinggi = abu.rows()
        val luasFrame = lebar * tinggi
        val luasMin = luasFrame * LUAS_MIN
        val luasMaks = luasFrame * LUAS_MAKS

        // Satu peta tepi gabungan: dipakai mencari kandidat sekaligus menguji
        // apakah sisi quad benar-benar menempel pada tepi nyata.
        val tegas = Mat()
        val longgar = Mat()
        Imgproc.Canny(halus, tegas, 50.0, 150.0)
        Imgproc.Canny(halus, longgar, 30.0, 90.0)
        val tepi = Mat()
        Core.bitwise_or(tegas, longgar, tepi)
        Imgproc.dilate(
            tepi,
            tepi,
            Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0)),
        )
        val peta = ByteArray(lebar * tinggi)
        tepi.get(0, 0, peta)

        val kandidat = LinkedHashMap<String, List<Titik>>()

        // Jalur 1: kontur dari peta tepi.
        val kontur = ArrayList<MatOfPoint>()
        Imgproc.findContours(tepi, kontur, Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
        for (k in kontur) {
            val luas = Imgproc.contourArea(k)
            if (luas <= luasMin) continue
            val keliling = Imgproc.arcLength(MatOfPoint2f(*k.toArray()), true)
            val segi = cariSegiEmpat(k, keliling, halus) ?: continue
            if (!kertasSah(segi, luas, lebar, tinggi, peta, luasMin, luasMaks)) continue
            kandidat[kunci(segi)] = segi
        }

        // Jalur 2: kecerahan. Kertas bidang paling terang, tetap ketemu walau tepi
        // putus. Wajib lolos uji bentuk dan dukungan tepi yang sama.
        kandidatKecerahan(halus, lebar, tinggi, peta, luasMin, luasMaks)
            .forEach { kandidat[kunci(it.second)] = it.second }

        tegas.release()
        longgar.release()
        tepi.release()
        halus.release()

        return kandidat.values
            .sortedByDescending { Stabilkan.luas(it) }
            .take(maks)
    }

    private fun kandidatKecerahan(
        halus: Mat,
        lebar: Int,
        tinggi: Int,
        peta: ByteArray,
        luasMin: Float,
        luasMaks: Float,
    ): List<Pair<Double, List<Titik>>> {
        val biner = Mat()
        Imgproc.threshold(halus, biner, 0.0, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU)

        // Tutup lubang huruf supaya kertas jadi satu bidang, bukan cincin.
        val bidang = Mat()
        Imgproc.morphologyEx(
            biner,
            bidang,
            Imgproc.MORPH_CLOSE,
            Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(9.0, 9.0)),
        )

        val kontur = ArrayList<MatOfPoint>()
        Imgproc.findContours(bidang, kontur, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)
        biner.release()
        bidang.release()

        return kontur.mapNotNull { k ->
            val luas = Imgproc.contourArea(k)
            if (luas <= luasMin) return@mapNotNull null

            val keliling = Imgproc.arcLength(MatOfPoint2f(*k.toArray()), true)
            val segi = cariSegiEmpat(k, keliling, halus)
                ?: kotakDari(k, lebar, tinggi)
                ?: return@mapNotNull null

            if (!kertasSah(segi, luas, lebar, tinggi, peta, luasMin, luasMaks)) return@mapNotNull null
            luas to segi
        }
    }

    /**
     * Saringan bentuk: buang bingkai layar, siluet meja, dan bayangan yang
     * kebetulan terang. Kertas harus kotak, tidak menempel tiga sisi frame, dan
     * sisinya harus menutupi tepi nyata.
     */
    private fun kertasSah(
        segi: List<Titik>,
        luas: Double,
        lebar: Int,
        tinggi: Int,
        peta: ByteArray,
        luasMin: Float,
        luasMaks: Float,
    ): Boolean {
        if (luas < luasMin || luas > luasMaks) return false
        if (Stabilkan.menempelBingkai(segi, lebar.toFloat(), tinggi.toFloat()) >= 3) return false
        if (Stabilkan.lonjong(segi) > LONJONG_MAKS) return false
        return sokongTepi(segi, peta, lebar, tinggi) >= TEPI_MIN
    }

    /** Fraksi titik sampel di sisi quad yang berimpit dengan peta tepi. */
    private fun sokongTepi(segi: List<Titik>, peta: ByteArray, lebar: Int, tinggi: Int): Float {
        val perSisi = 12
        var kena = 0
        var total = 0
        for (i in segi.indices) {
            val a = segi[i]
            val b = segi[(i + 1) % 4]
            for (s in 0 until perSisi) {
                val t = (s + 0.5f) / perSisi
                val x = (a.x + (b.x - a.x) * t).toInt()
                val y = (a.y + (b.y - a.y) * t).toInt()
                total++
                if (dekatTepi(peta, lebar, tinggi, x, y)) kena++
            }
        }
        if (total == 0) return 0f
        return kena.toFloat() / total
    }

    private fun dekatTepi(peta: ByteArray, lebar: Int, tinggi: Int, x: Int, y: Int): Boolean {
        // Toleransi 4 px: quad dari jalur kecerahan bergeser beberapa piksel karena
        // morfologi, tapi tetap harus menempel ke tepi nyata.
        for (dy in -4..4) {
            val py = y + dy
            if (py < 0 || py >= tinggi) continue
            for (dx in -4..4) {
                val px = x + dx
                if (px < 0 || px >= lebar) continue
                if (peta[py * lebar + px].toInt() != 0) return true
            }
        }
        return false
    }

    /** Cadangan: persegi pembatas minimum untuk kontur yang tidak bisa diringkas jadi 4 titik. */
    private fun kotakDari(kontur: MatOfPoint, lebar: Int, tinggi: Int): List<Titik>? {
        val persegi = Imgproc.minAreaRect(MatOfPoint2f(*kontur.toArray()))
        val titik = arrayOfNulls<Point>(4)
        persegi.points(titik)
        val segi = titik.filterNotNull().map { Titik(it.x.toFloat(), it.y.toFloat()) }
        if (segi.size != 4) return null
        val urut = UrutSudut.urut(segi)
        if (!Stabilkan.sah(urut, lebar.toFloat(), tinggi.toFloat())) return null
        return urut
    }

    /** Coba beberapa toleransi aproksimasi: tepi kertas yang bergelombang butuh yang longgar. */
    private fun cariSegiEmpat(kontur: MatOfPoint, keliling: Double, abu: Mat): List<Titik>? {
        for (eps in EPSILON) {
            val perkiraan = MatOfPoint2f()
            Imgproc.approxPolyDP(MatOfPoint2f(*kontur.toArray()), perkiraan, eps * keliling, true)
            if (perkiraan.total() != 4L) continue

            val segi = MatOfPoint(*perkiraan.toArray())
            if (!Imgproc.isContourConvex(segi)) continue

            val urut = UrutSudut.urut(segi.toList().map { Titik(it.x.toFloat(), it.y.toFloat()) })
            if (!Stabilkan.sah(urut, abu.cols().toFloat(), abu.rows().toFloat())) continue
            return urut
        }
        return null
    }

    private fun kunci(t: List<Titik>): String =
        t.joinToString("|") { "${(it.x / 8).toInt()},${(it.y / 8).toInt()}" }

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
     * ABU: bayangan dibuang (bagi dengan blur) lalu CLAHE + unsharp mask.
     * ASLI: hanya koreksi perspektif + kontras lembut; warna dan rona dibiarkan.
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

                // Unsharp mask: tepi huruf yang sudah diratakan CLAHE ditebalkan lagi.
                val kabur = Mat()
                Imgproc.GaussianBlur(keluar, kabur, Size(0.0, 0.0), 3.0)
                Core.addWeighted(keluar, 1.5, kabur, -0.5, 0.0, keluar)
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

            ModeWarna.HITAM_PUTIH -> {
                val abu = Mat()
                Imgproc.cvtColor(asli, abu, Imgproc.COLOR_RGBA2GRAY)
                val latar = Mat()
                Imgproc.medianBlur(abu, latar, 31)
                val rata = Mat()
                Core.divide(abu, latar, rata, 255.0)
                val biner = Mat()
                Imgproc.adaptiveThreshold(
                    rata,
                    biner,
                    255.0,
                    Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                    Imgproc.THRESH_BINARY,
                    25,
                    10.0,
                )
                biner
            }

            ModeWarna.LINEAR -> {
                // Regangkan kontras ke 0..255 tanpa mengubah rona; cukup untuk foto redup.
                val abu = Mat()
                Imgproc.cvtColor(asli, abu, Imgproc.COLOR_RGBA2GRAY)
                val keluar = Mat()
                Core.normalize(abu, keluar, 0.0, 255.0, Core.NORM_MINMAX)
                keluar
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

    /** Putar searah jarum jam sebanyak kelipatan 90°. 0° mengembalikan bitmap yang sama. */
    fun putar(bitmap: Bitmap, derajat: Int): Bitmap {
        val d = ((derajat % 360) + 360) % 360
        if (d == 0) return bitmap
        val matriks = Matrix().apply { postRotate(d.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriks, true)
    }

    /** Perkecil ke lebar tertentu untuk OCR percobaan; cukup untuk menghitung karakter. */
    fun kecilkan(bitmap: Bitmap, lebar: Int): Bitmap {
        if (bitmap.width <= lebar) return bitmap
        val tinggi = (lebar.toFloat() * bitmap.height / bitmap.width).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, lebar, tinggi, true)
    }

    /** Quad harus konveks dan searah; drag yang bikin cekung ditolak. */
    fun konveks(t: List<Titik>): Boolean {
        if (t.size != 4) return false
        var tanda = 0
        for (i in 0 until 4) {
            val a = t[i]
            val b = t[(i + 1) % 4]
            val c = t[(i + 2) % 4]
            val silang = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x)
            val s = when {
                silang > 1e-4f -> 1
                silang < -1e-4f -> -1
                else -> return false
            }
            if (tanda != 0 && s != tanda) return false
            tanda = s
        }
        return true
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
