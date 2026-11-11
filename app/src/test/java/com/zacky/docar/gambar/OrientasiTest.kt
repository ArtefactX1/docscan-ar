package com.zacky.docar.gambar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrientasiTest {
    @Test
    fun terbaik_memilih_teks_terbanyak() {
        assertEquals(2, Orientasi.terbaik(intArrayOf(5, 8, 120, 3)))
    }

    @Test
    fun terbaik_tegak_tetap_nol() {
        assertEquals(0, Orientasi.terbaik(intArrayOf(300, 10, 20, 5)))
    }

    @Test
    fun terbaik_array_kosong_aman() {
        assertEquals(0, Orientasi.terbaik(IntArray(0)))
    }

    @Test
    fun teks_tipis_perlu_coba_rotasi() {
        assertTrue(Orientasi.perluCobaLain(3))
    }

    @Test
    fun teks_banyak_tidak_perlu_rotasi() {
        assertFalse(Orientasi.perluCobaLain(200))
    }
}
