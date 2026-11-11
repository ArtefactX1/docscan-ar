package com.zacky.docar.kamera

import org.junit.Assert.assertEquals
import org.junit.Test

class MesinCaptureTest {
    private fun mesin() = MesinCapture(luasMin = 0.15f, frameStabil = 3)

    @Test
    fun tanpa_kertas_mencari() {
        assertEquals(
            StatusCapture.SEARCH,
            mesin().perbarui(adaKertas = false, luasRasio = 0f, posisiStabil = false, sensorSiap = true),
        )
    }

    @Test
    fun kertas_kecil_minta_dekat() {
        assertEquals(
            StatusCapture.CLOSER,
            mesin().perbarui(adaKertas = true, luasRasio = 0.05f, posisiStabil = true, sensorSiap = true),
        )
    }

    @Test
    fun goyang_tidak_pernah_siap() {
        val m = mesin()
        repeat(10) {
            assertEquals(
                StatusCapture.DO_NOT_MOVE,
                m.perbarui(adaKertas = true, luasRasio = 0.5f, posisiStabil = false, sensorSiap = true),
            )
        }
    }

    @Test
    fun siap_setelah_tiga_frame_stabil() {
        val m = mesin()
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true))
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true))
        assertEquals(StatusCapture.SIAP, m.perbarui(true, 0.5f, true, true))
    }

    @Test
    fun satu_frame_goyang_mereset_hitungan() {
        val m = mesin()
        m.perbarui(true, 0.5f, true, true)
        m.perbarui(true, 0.5f, true, true)
        m.perbarui(true, 0.5f, false, true)
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true))
        assertEquals(StatusCapture.DO_NOT_MOVE, m.perbarui(true, 0.5f, true, true))
        assertEquals(StatusCapture.SIAP, m.perbarui(true, 0.5f, true, true))
    }

    @Test
    fun sensor_belum_siap_menahan() {
        val m = mesin()
        repeat(5) {
            assertEquals(
                StatusCapture.DO_NOT_MOVE,
                m.perbarui(adaKertas = true, luasRasio = 0.5f, posisiStabil = true, sensorSiap = false),
            )
        }
    }
}
