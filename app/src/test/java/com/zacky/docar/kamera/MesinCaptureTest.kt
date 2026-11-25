package com.zacky.docar.kamera

import org.junit.Assert.assertEquals
import org.junit.Test

class MesinCaptureTest {
    private fun mesin() = MesinCapture(luasMin = 0.08f, tahanMs = 700L)

    @Test
    fun tanpa_kertas_mencari() {
        assertEquals(
            StatusCapture.SEARCH,
            mesin().perbarui(false, 0f, false, true, 0),
        )
    }

    @Test
    fun kertas_kecil_minta_dekat() {
        assertEquals(
            StatusCapture.CLOSER,
            mesin().perbarui(true, 0.05f, true, true, 0),
        )
    }

    @Test
    fun goyang_tidak_pernah_siap() {
        val m = mesin()
        repeat(10) { i ->
            assertEquals(
                StatusCapture.DO_NOT_MOVE,
                m.perbarui(true, 0.5f, false, true, i * 100L),
            )
        }
    }

    @Test
    fun siap_setelah_tahan_tujuh_ratus_ms() {
        val m = mesin()
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true, 0))
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true, 400))
        assertEquals(StatusCapture.SIAP, m.perbarui(true, 0.5f, true, true, 700))
    }

    @Test
    fun satu_frame_goyang_mereset_waktu() {
        val m = mesin()
        m.perbarui(true, 0.5f, true, true, 0)
        m.perbarui(true, 0.5f, true, true, 500)
        m.perbarui(true, 0.5f, false, true, 600)
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true, 700))
        assertEquals(StatusCapture.SIAP, m.perbarui(true, 0.5f, true, true, 1400))
    }

    @Test
    fun progres_terisi_seiring_waktu() {
        val m = mesin()
        m.perbarui(true, 0.5f, true, true, 0)
        m.perbarui(true, 0.5f, true, true, 350)
        assertEquals(0.5f, m.progres, 0.02f)
    }

    @Test
    fun sensor_belum_siap_menahan() {
        val m = mesin()
        repeat(5) { i ->
            assertEquals(
                StatusCapture.DO_NOT_MOVE,
                m.perbarui(true, 0.5f, true, false, i * 100L),
            )
        }
    }
}
