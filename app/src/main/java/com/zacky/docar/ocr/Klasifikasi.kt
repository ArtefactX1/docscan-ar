package com.zacky.docar.ocr

/**
 * Pengganti ringan model klasifikasi CamScanner: pola kata kunci, bukan neural
 * net. Tujuannya hanya memberi nama lebih berguna daripada "Scan 11 Nov 2026".
 */
object Klasifikasi {
    enum class Jenis(val label: String) {
        STRUK("Struk"),
        FAKTUR("Faktur"),
        KTP("KTP"),
        RESI("Resi"),
        UMUM("Dokumen"),
    }

    private val pola = listOf(
        Jenis.STRUK to listOf(
            Regex("""\b(total|subtotal|kembalian|tunai|qris)\b""", RegexOption.IGNORE_CASE),
            Regex("""\bRp\s?\d""", RegexOption.IGNORE_CASE),
        ),
        Jenis.FAKTUR to listOf(
            Regex("""\b(invoice|faktur|no\.?\s*faktur|npwp)\b""", RegexOption.IGNORE_CASE),
        ),
        Jenis.KTP to listOf(
            Regex("""\b(nik|kartu tanda penduduk|golongan darah)\b""", RegexOption.IGNORE_CASE),
        ),
        Jenis.RESI to listOf(
            Regex("""\b(resi|no\.?\s*resi|kurir|ekspedisi|tracking)\b""", RegexOption.IGNORE_CASE),
        ),
    )

    /** Jenis dengan kata kunci terbanyak; UMUM kalau tidak ada yang cocok. */
    fun jenis(teks: String): Jenis {
        if (teks.isBlank()) return Jenis.UMUM
        var terbaik = Jenis.UMUM
        var skorTerbaik = 0
        for ((jenis, daftar) in pola) {
            val skor = daftar.sumOf { polaHit -> polaHit.findAll(teks).count() }
            if (skor > skorTerbaik) {
                skorTerbaik = skor
                terbaik = jenis
            }
        }
        return terbaik
    }
}
