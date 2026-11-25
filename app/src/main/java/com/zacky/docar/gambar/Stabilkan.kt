package com.zacky.docar.gambar

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sign

/**
 * Tepi mentah bergetar 1–3 px tiap frame; tanpa ini overlay ikut bergetar dan
 * auto-capture tidak pernah stabil.
 */
object Stabilkan {
    /** Luas poligon (shoelace). Nilai absolut, satuan sama dengan koordinat. */
    fun luas(t: List<Titik>): Float {
        if (t.size < 3) return 0f
        var jumlah = 0f
        for (i in t.indices) {
            val a = t[i]
            val b = t[(i + 1) % t.size]
            jumlah += a.x * b.y - b.x * a.y
        }
        return abs(jumlah) / 2f
    }

    /** Intersection-over-union dua poligon konveks dengan urutan titik sama. */
    fun iou(a: List<Titik>, b: List<Titik>): Float {
        if (a.size != 4 || b.size != 4) return 0f
        val irisan = luas(potong(a, b))
        val gabung = luas(a) + luas(b) - irisan
        if (gabung <= 0f) return 0f
        return irisan / gabung
    }

    /** Interpolasi linear per titik; alpha kecil = lebih halus tapi lebih lambat mengejar. */
    fun halus(lama: List<Titik>, baru: List<Titik>, alpha: Float): List<Titik> {
        if (lama.size != baru.size) return baru
        val a = alpha.coerceIn(0f, 1f)
        return baru.mapIndexed { i, t ->
            Titik(
                lama[i].x + (t.x - lama[i].x) * a,
                lama[i].y + (t.y - lama[i].y) * a,
            )
        }
    }

    /**
     * Perbandingan sisi terpanjang dan terpendek. Kertas A4 ±1,41; struk bisa
     * sampai 3,5. Di atas [batas] bentuknya terlalu gepeng untuk jadi halaman.
     */
    fun lonjong(t: List<Titik>): Float {
        if (t.size != 4) return Float.MAX_VALUE
        var terpendek = Float.MAX_VALUE
        var terpanjang = 0f
        for (i in t.indices) {
            val a = t[i]
            val b = t[(i + 1) % 4]
            val sisi = hypot(b.x - a.x, b.y - a.y)
            if (sisi < terpendek) terpendek = sisi
            if (sisi > terpanjang) terpanjang = sisi
        }
        if (terpendek <= 0f) return Float.MAX_VALUE
        return terpanjang / terpendek
    }

    /** Jumlah sisi yang hampir menyentuh tepi frame; kertas biasanya tidak menempel. */
    fun menempelBingkai(t: List<Titik>, lebar: Float, tinggi: Float): Int {
        if (t.size != 4) return 0
        val tepiX = lebar * 0.02f
        val tepiY = tinggi * 0.02f
        var jumlah = 0
        for (i in t.indices) {
            val a = t[i]
            val b = t[(i + 1) % 4]
            val diKiri = a.x < tepiX && b.x < tepiX
            val diKanan = a.x > lebar - tepiX && b.x > lebar - tepiX
            val diAtas = a.y < tepiY && b.y < tepiY
            val diBawah = a.y > tinggi - tepiY && b.y > tinggi - tepiY
            if (diKiri || diKanan || diAtas || diBawah) jumlah++
        }
        return jumlah
    }

    /**
     * Tolak quad merosot: titik kembar, sisi pendek, atau jauh di luar frame.
     * Detektor kadang mengembalikan quad nol.
     */
    fun sah(t: List<Titik>, lebar: Float, tinggi: Float, sisiMin: Float = 8f): Boolean {
        if (t.size != 4) return false
        if (luas(t) <= 0f) return false
        for (i in t.indices) {
            val a = t[i]
            val b = t[(i + 1) % 4]
            if (hypot(b.x - a.x, b.y - a.y) < sisiMin) return false
            if (a.x < -sisiMin || a.y < -sisiMin) return false
            if (a.x > lebar + sisiMin || a.y > tinggi + sisiMin) return false
        }
        return true
    }

    private fun potong(subjek: List<Titik>, klip: List<Titik>): List<Titik> {
        val arah = sign(bertanda(klip))
        var keluaran = subjek
        for (i in klip.indices) {
            if (keluaran.isEmpty()) return emptyList()
            val a = klip[i]
            val b = klip[(i + 1) % klip.size]
            val masukan = keluaran
            keluaran = emptyList()
            val hasil = ArrayList<Titik>(masukan.size + 2)
            var s = masukan.last()
            for (e in masukan) {
                val diDalam = diDalam(e, a, b, arah)
                val sDiDalam = diDalam(s, a, b, arah)
                if (diDalam) {
                    if (!sDiDalam) hasil += persilangan(s, e, a, b)
                    hasil += e
                } else if (sDiDalam) {
                    hasil += persilangan(s, e, a, b)
                }
                s = e
            }
            keluaran = hasil
        }
        return keluaran
    }

    private fun bertanda(t: List<Titik>): Float {
        var jumlah = 0f
        for (i in t.indices) {
            val a = t[i]
            val b = t[(i + 1) % t.size]
            jumlah += a.x * b.y - b.x * a.y
        }
        return jumlah / 2f
    }

    private fun diDalam(p: Titik, a: Titik, b: Titik, arah: Float): Boolean {
        val silang = (b.x - a.x) * (p.y - a.y) - (b.y - a.y) * (p.x - a.x)
        return silang * arah >= 0f
    }

    private fun persilangan(s: Titik, e: Titik, a: Titik, b: Titik): Titik {
        val r = Titik(e.x - s.x, e.y - s.y)
        val q = Titik(b.x - a.x, b.y - a.y)
        val penyebut = r.x * q.y - r.y * q.x
        if (abs(penyebut) < 1e-6f) return e
        val t = ((a.x - s.x) * q.y - (a.y - s.y) * q.x) / penyebut
        return Titik(s.x + r.x * t, s.y + r.y * t)
    }

    /** Jarak terjauh antara dua quad, dipakai untuk menolak lompatan besar. */
    fun jarakMaks(a: List<Titik>, b: List<Titik>): Float {
        if (a.size != b.size) return Float.MAX_VALUE
        var maks = 0f
        for (i in a.indices) maks = max(maks, hypot(a[i].x - b[i].x, a[i].y - b[i].y))
        return maks
    }

    /**
     * Kandidat yang paling mirip [lama]. null kalau tidak ada yang cukup mirip —
     * artinya lebih baik tahan quad lama daripada melompat ke kontur lain
     * (meja, bayangan) yang muncul sesaat.
     */
    fun pilihTerdekat(
        kandidat: List<List<Titik>>,
        lama: List<Titik>,
        iouGanti: Float,
    ): List<Titik>? {
        if (kandidat.isEmpty()) return null
        val terbaik = kandidat.maxByOrNull { iou(lama, it) } ?: return null
        return if (iou(lama, terbaik) >= iouGanti) terbaik else null
    }
}
