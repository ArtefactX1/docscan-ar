package com.zacky.docar.gambar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CahayaTest {
    @Test
    fun rata_rata_putih_penuh() {
        val y = ByteArray(100) { 0xFF.toByte() }
        assertEquals(255f, Cahaya.rataRata(y), 0.01f)
    }

    @Test
    fun rata_rata_kosong_nol() {
        assertEquals(0f, Cahaya.rataRata(ByteArray(0)), 0.01f)
    }

    @Test
    fun frame_seragam_tanpa_bayangan() {
        val y = ByteArray(30 * 10) { 120.toByte() }
        assertFalse(Cahaya.bayanganKiriKanan(y, 30, 10))
    }

    @Test
    fun sisi_kiri_gelap_terdeteksi() {
        val lebar = 30
        val tinggi = 10
        val y = ByteArray(lebar * tinggi) { 200.toByte() }
        for (baris in 0 until tinggi) {
            for (x in 0 until lebar / 3) y[baris * lebar + x] = 20.toByte()
        }
        assertTrue(Cahaya.bayanganKiriKanan(y, lebar, tinggi))
    }

    @Test
    fun ukuran_tidak_cocok_tidak_melempar() {
        assertFalse(Cahaya.bayanganKiriKanan(ByteArray(5), 30, 10))
    }
}
