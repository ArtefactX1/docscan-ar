package com.zacky.docar.sensor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SudutHitungTest {
    @Test
    fun datar_menghasilkan_nol() {
        assertEquals(0f, SudutHitung.kemiringan(0f, 0f, -9.81f), 1f)
    }

    @Test
    fun tegak_menghasilkan_sembilan_puluh() {
        assertEquals(90f, SudutHitung.kemiringan(9.81f, 0f, 0f), 1f)
    }

    @Test
    fun vektor_nol_tidak_nan() {
        assertEquals(0f, SudutHitung.kemiringan(0f, 0f, 0f), 0.01f)
    }
}

class GuncanganTest {
    @Test
    fun diam_tidak_guncang() {
        val g = Guncangan()
        repeat(50) { assertFalse(g.perbarui(0f, 0f, 9.81f)) }
    }

    @Test
    fun hentakan_terdeteksi() {
        val g = Guncangan()
        repeat(50) { g.perbarui(0f, 0f, 9.81f) }
        assertTrue(g.perbarui(0f, 0f, 14.5f))
    }
}

class KesiapanTest {
    @Test
    fun semua_terpenuhi_siap() {
        assertTrue(Kesiapan.siap(sudut = 1f, guncangan = false, berputar = false, lux = 200f))
    }

    @Test
    fun sudut_terlalu_besar_tidak_siap() {
        assertFalse(Kesiapan.siap(sudut = 7f, guncangan = false, berputar = false, lux = 200f))
    }

    @Test
    fun guncangan_menghalangi() {
        assertFalse(Kesiapan.siap(sudut = 1f, guncangan = true, berputar = false, lux = 200f))
    }

    @Test
    fun ruangan_redup_menghalangi() {
        assertFalse(Kesiapan.siap(sudut = 1f, guncangan = false, berputar = false, lux = 20f))
    }

    @Test
    fun tanpa_sensor_cahaya_tidak_menghalangi() {
        assertTrue(Kesiapan.siap(sudut = 1f, guncangan = false, berputar = false, lux = null))
    }

    @Test
    fun frame_gelap_menghalangi() {
        assertFalse(
            Kesiapan.siap(
                sudut = 1f,
                guncangan = false,
                berputar = false,
                lux = null,
                terangFrame = 20f,
            )
        )
    }

    @Test
    fun ambang_sudut_kustom_dipakai() {
        assertTrue(
            Kesiapan.siap(
                sudut = 4f,
                guncangan = false,
                berputar = false,
                lux = 200f,
                sudutMaks = 5f,
            )
        )
    }

    @Test
    fun tanpa_dokumen_tidak_siap() {
        assertFalse(
            Kesiapan.siap(
                sudut = 1f,
                guncangan = false,
                berputar = false,
                lux = 200f,
                adaDokumen = false,
            )
        )
    }
}
