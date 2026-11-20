package com.zacky.docar.kamera

import com.zacky.docar.gambar.Titik
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PenyanggaDeteksiTest {
    private val contoh = SegiLayar(
        titik = listOf(Titik(0f, 0f), Titik(1f, 0f), Titik(1f, 1f), Titik(0f, 1f)),
        rasio = 1f,
        luas = 1f,
        stabil = true,
    )

    @Test
    fun hasil_terdeteksi_langsung_diteruskan() {
        assertEquals(contoh, PenyanggaDeteksi().terima(contoh))
    }

    @Test
    fun satu_frame_gagal_tidak_menghapus_overlay() {
        val p = PenyanggaDeteksi()
        p.terima(contoh)
        assertEquals(contoh, p.terima(null))
    }

    @Test
    fun gagal_terus_melewati_batas_baru_dilepas() {
        val p = PenyanggaDeteksi(maksGagal = 3)
        p.terima(contoh)
        assertEquals(contoh, p.terima(null))
        assertEquals(contoh, p.terima(null))
        assertEquals(contoh, p.terima(null))
        assertNull(p.terima(null))
    }

    @Test
    fun deteksi_kembali_mereset_hitungan_gagal() {
        val p = PenyanggaDeteksi(maksGagal = 2)
        p.terima(contoh)
        p.terima(null)
        p.terima(null)
        p.terima(contoh)
        assertEquals(contoh, p.terima(null))
    }

    @Test
    fun reset_menghapus_quad() {
        val p = PenyanggaDeteksi()
        p.terima(contoh)
        p.reset()
        assertNull(p.terima(null))
    }
}
