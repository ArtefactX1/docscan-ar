package com.zacky.docar.gambar

/**
 * Rotasi terbaik dipilih dari jumlah karakter OCR, bukan dari analisis tepi.
 */
object Orientasi {
    /** Ambang karakter sebelum rotasi lain dicoba. */
    const val AMBANG = 20

    /** Jumlah karakter untuk rotasi 0°, 90°, 180°, 270°; kembalikan indeks terbanyak. */
    fun terbaik(jumlah: IntArray): Int {
        if (jumlah.isEmpty()) return 0
        var indeks = 0
        for (i in jumlah.indices) if (jumlah[i] > jumlah[indeks]) indeks = i
        return indeks
    }

    /** Rotasi hanya dicoba kalau teks awal terlalu sedikit untuk dipercaya. */
    fun perluCobaLain(awal: Int): Boolean = awal < AMBANG
}
