package com.zacky.docar.ocr

import org.junit.Assert.assertEquals
import org.junit.Test

class KlasifikasiTest {
    @Test
    fun struk_belanja_terdeteksi() {
        val teks = "TOTAL Rp 45.000\nTUNAI Rp 50.000\nKEMBALIAN Rp 5.000"
        assertEquals(Klasifikasi.Jenis.STRUK, Klasifikasi.jenis(teks))
    }

    @Test
    fun faktur_dengan_npwp_terdeteksi() {
        assertEquals(
            Klasifikasi.Jenis.FAKTUR,
            Klasifikasi.jenis("FAKTUR PAJAK\nNPWP: 01.234.567.8-901.000"),
        )
    }

    @Test
    fun ktp_terdeteksi() {
        assertEquals(
            Klasifikasi.Jenis.KTP,
            Klasifikasi.jenis("KARTU TANDA PENDUDUK\nNIK : 3201234567890001"),
        )
    }

    @Test
    fun teks_biasa_umum() {
        assertEquals(Klasifikasi.Jenis.UMUM, Klasifikasi.jenis("Catatan rapat mingguan tim"))
    }

    @Test
    fun teks_kosong_umum() {
        assertEquals(Klasifikasi.Jenis.UMUM, Klasifikasi.jenis("   "))
    }
}
