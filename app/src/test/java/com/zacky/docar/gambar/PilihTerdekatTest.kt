package com.zacky.docar.gambar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PilihTerdekatTest {
    private fun segi(dx: Float, dy: Float) = listOf(
        Titik(10f + dx, 10f + dy),
        Titik(200f + dx, 10f + dy),
        Titik(200f + dx, 300f + dy),
        Titik(10f + dx, 300f + dy),
    )

    private val lama = segi(0f, 0f)

    @Test
    fun kandidat_dekat_dipilih() {
        assertEquals(segi(4f, 2f), Stabilkan.pilihTerdekat(listOf(segi(4f, 2f)), lama, 0.25f))
    }

    @Test
    fun kandidat_jauh_tidak_dipilih() {
        assertNull(Stabilkan.pilihTerdekat(listOf(segi(400f, 400f)), lama, 0.25f))
    }

    @Test
    fun kandidat_kosong_menghasilkan_null() {
        assertNull(Stabilkan.pilihTerdekat(emptyList(), lama, 0.25f))
    }

    @Test
    fun yang_paling_dekat_menang() {
        val dekat = segi(6f, 0f)
        val sedang = segi(20f, 0f)
        assertEquals(dekat, Stabilkan.pilihTerdekat(listOf(sedang, dekat), lama, 0.25f))
    }
}
