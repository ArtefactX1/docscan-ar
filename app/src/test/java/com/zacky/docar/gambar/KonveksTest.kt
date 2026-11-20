package com.zacky.docar.gambar

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KonveksTest {
    private val segi = listOf(
        Titik(10f, 10f),
        Titik(200f, 12f),
        Titik(198f, 300f),
        Titik(12f, 298f),
    )

    @Test
    fun quad_urut_konveks() {
        assertTrue(Pemindai.konveks(segi))
    }

    @Test
    fun quad_cekung_ditolak() {
        val cekung = segi.toMutableList().also { it[1] = Titik(90f, 160f) }
        assertFalse(Pemindai.konveks(cekung))
    }

    @Test
    fun titik_berurutan_terbalik_tetap_konveks() {
        assertTrue(Pemindai.konveks(segi.reversed()))
    }

    @Test
    fun titik_kolinear_ditolak() {
        val garis = listOf(
            Titik(10f, 10f),
            Titik(100f, 10f),
            Titik(200f, 10f),
            Titik(200f, 200f),
        )
        assertFalse(Pemindai.konveks(garis))
    }

    @Test
    fun jumlah_titik_bukan_empat_ditolak() {
        assertFalse(Pemindai.konveks(segi.take(3)))
    }
}
