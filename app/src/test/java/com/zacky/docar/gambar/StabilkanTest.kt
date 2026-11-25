package com.zacky.docar.gambar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StabilkanTest {
    private val kotak = listOf(
        Titik(0f, 0f),
        Titik(10f, 0f),
        Titik(10f, 10f),
        Titik(0f, 10f),
    )

    @Test
    fun luas_kotak_sepuluh() {
        assertEquals(100f, Stabilkan.luas(kotak), 0.01f)
    }

    @Test
    fun iou_identik_satu() {
        assertEquals(1f, Stabilkan.iou(kotak, kotak), 0.001f)
    }

    @Test
    fun iou_terpisah_nol() {
        val jauh = kotak.map { Titik(it.x + 50f, it.y) }
        assertEquals(0f, Stabilkan.iou(kotak, jauh), 0.001f)
    }

    @Test
    fun iou_setengah_tumpang() {
        val geser = kotak.map { Titik(it.x + 5f, it.y) }
        // irisan 50, gabungan 150
        assertEquals(1f / 3f, Stabilkan.iou(kotak, geser), 0.01f)
    }

    @Test
    fun halus_mengambil_jalur_tengah() {
        val baru = kotak.map { Titik(it.x + 10f, it.y) }
        val hasil = Stabilkan.halus(kotak, baru, 0.5f)
        assertEquals(5f, hasil[0].x, 0.01f)
    }

    @Test
    fun lonjong_a4_mendekati_satu_koma_empat() {
        val a4 = listOf(
            Titik(0f, 0f),
            Titik(124f, 0f),
            Titik(124f, 175f),
            Titik(0f, 175f),
        )
        assertEquals(1.41f, Stabilkan.lonjong(a4), 0.01f)
    }

    @Test
    fun lonjong_gepeng_besar() {
        val gepeng = listOf(
            Titik(0f, 0f),
            Titik(60f, 0f),
            Titik(60f, 6f),
            Titik(0f, 6f),
        )
        assertEquals(10f, Stabilkan.lonjong(gepeng), 0.01f)
    }

    @Test
    fun bingkai_menempel_dihitung() {
        val penuh = listOf(
            Titik(0f, 0f),
            Titik(100f, 0f),
            Titik(100f, 100f),
            Titik(0f, 100f),
        )
        assertEquals(4, Stabilkan.menempelBingkai(penuh, 100f, 100f))
    }

    @Test
    fun kertas_di_tengah_tidak_menempel() {
        val tengah = listOf(
            Titik(20f, 20f),
            Titik(80f, 20f),
            Titik(80f, 80f),
            Titik(20f, 80f),
        )
        assertEquals(0, Stabilkan.menempelBingkai(tengah, 100f, 100f))
    }

    @Test
    fun titik_kembar_ditolak() {
        val rusak = listOf(Titik(0f, 0f), Titik(0f, 0f), Titik(10f, 10f), Titik(0f, 10f))
        assertFalse(Stabilkan.sah(rusak, 100f, 100f))
    }

    @Test
    fun quad_wajar_diterima() {
        assertTrue(Stabilkan.sah(kotak, 100f, 100f))
    }

    @Test
    fun quad_di_luar_frame_ditolak() {
        val luar = kotak.map { Titik(it.x + 200f, it.y) }
        assertFalse(Stabilkan.sah(luar, 100f, 100f))
    }
}
