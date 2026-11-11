package com.zacky.docar.gambar

import org.junit.Assert.assertEquals
import org.junit.Test

class UrutSudutTest {
    @Test
    fun urutan_acak_jadi_searah_jarum_jam() {
        val acak = listOf(
            Titik(300f, 400f),
            Titik(20f, 30f),
            Titik(310f, 25f),
            Titik(15f, 410f),
        )
        val hasil = UrutSudut.urut(acak)

        assertEquals(Titik(20f, 30f), hasil[0])
        assertEquals(Titik(310f, 25f), hasil[1])
        assertEquals(Titik(300f, 400f), hasil[2])
        assertEquals(Titik(15f, 410f), hasil[3])
    }

    @Test
    fun kemiringan_kecil_tetap_terurut() {
        val acak = listOf(
            Titik(100f, 200f),
            Titik(10f, 12f),
            Titik(105f, 15f),
            Titik(8f, 205f),
        )
        val hasil = UrutSudut.urut(acak)

        assertEquals(Titik(10f, 12f), hasil[0])
        assertEquals(Titik(105f, 15f), hasil[1])
        assertEquals(Titik(100f, 200f), hasil[2])
        assertEquals(Titik(8f, 205f), hasil[3])
    }
}
