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
        assertTrue(Kesiapan.siap(guncangan = false, berputar = false, lux = 200f))
    }

    @Test
    fun guncangan_menghalangi() {
        assertFalse(Kesiapan.siap(guncangan = true, berputar = false, lux = 200f))
    }

    @Test
    fun berputar_menghalangi() {
        assertFalse(Kesiapan.siap(guncangan = false, berputar = true, lux = 200f))
    }

    @Test
    fun ruangan_redup_menghalangi() {
        assertFalse(Kesiapan.siap(guncangan = false, berputar = false, lux = 20f))
    }

    @Test
    fun tanpa_sensor_cahaya_tidak_menghalangi() {
        assertTrue(Kesiapan.siap(guncangan = false, berputar = false, lux = null))
    }

    @Test
    fun frame_gelap_menghalangi() {
        assertFalse(
            Kesiapan.siap(
                guncangan = false,
                berputar = false,
                lux = null,
                terangFrame = 20f,
            )
        )
    }

    @Test
    fun sudut_tidak_lagi_menghalangi_capture() {
        // Kemiringan dikoreksi warpPerspective; cukup jadi peringatan di HUD.
        assertTrue(Kesiapan.siap(guncangan = false, berputar = false, lux = 200f))
    }

    @Test
    fun cukup_datar_untuk_chip_hud() {
        assertTrue(Kesiapan.cukupDatar(5f, sudutMaks = 8f))
        assertFalse(Kesiapan.cukupDatar(12f, sudutMaks = 8f))
    }

    @Test
    fun tanpa_dokumen_tidak_lagi_menghalangi() {
        assertTrue(Kesiapan.siap(guncangan = false, berputar = false, lux = 200f))
    }
}
