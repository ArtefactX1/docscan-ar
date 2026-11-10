package com.buuzz.docscanar.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Satu dokumen berisi banyak halaman. PDF tidak disimpan sebagai sumber
 * kebenaran — ia dibangun ulang dari halaman setiap kali urutan berubah.
 */
class DokumenDb(context: Context) :
    SQLiteOpenHelper(context, "docscan.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE dokumen (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                dibuat INTEGER NOT NULL,
                diubah INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE halaman (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dokumen_id INTEGER NOT NULL,
                urutan INTEGER NOT NULL,
                gambar TEXT NOT NULL,
                thumb TEXT NOT NULL,
                teks TEXT NOT NULL DEFAULT '',
                kotak TEXT NOT NULL DEFAULT '[]'
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_halaman_dokumen ON halaman(dokumen_id, urutan)")
        db.execSQL("CREATE VIRTUAL TABLE halaman_fts USING fts4(teks)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // v1 menyimpan satu PDF per dokumen tanpa gambar halaman; tidak bisa
        // dimigrasikan ke model halaman, jadi dibangun ulang.
        db.execSQL("DROP TABLE IF EXISTS dokumen_fts")
        db.execSQL("DROP TABLE IF EXISTS dokumen")
        db.execSQL("DROP TABLE IF EXISTS halaman")
        db.execSQL("DROP TABLE IF EXISTS halaman_fts")
        onCreate(db)
    }

    fun tambahDokumen(nama: String): Long {
        val sekarang = System.currentTimeMillis()
        val nilai = ContentValues().apply {
            put("nama", nama)
            put("dibuat", sekarang)
            put("diubah", sekarang)
        }
        return writableDatabase.insert("dokumen", null, nilai)
    }

    fun tambahHalaman(
        dokumenId: Long,
        gambar: String,
        thumb: String,
        teks: String,
        kotak: String,
    ): Long {
        val db = writableDatabase
        val urutan = db.rawQuery(
            "SELECT COALESCE(MAX(urutan), -1) + 1 FROM halaman WHERE dokumen_id = ?",
            arrayOf(dokumenId.toString()),
        ).use { if (it.moveToFirst()) it.getInt(0) else 0 }

        val nilai = ContentValues().apply {
            put("dokumen_id", dokumenId)
            put("urutan", urutan)
            put("gambar", gambar)
            put("thumb", thumb)
            put("teks", teks)
            put("kotak", kotak)
        }
        val id = db.insert("halaman", null, nilai)
        if (id > 0) {
            db.execSQL("INSERT INTO halaman_fts(rowid, teks) VALUES (?, ?)", arrayOf(id, teks))
            sentuh(dokumenId)
        }
        return id
    }

    fun halaman(dokumenId: Long): List<Halaman> {
        val hasil = mutableListOf<Halaman>()
        readableDatabase.rawQuery(
            "SELECT id, dokumen_id, urutan, gambar, thumb, teks, kotak FROM halaman WHERE dokumen_id = ? ORDER BY urutan",
            arrayOf(dokumenId.toString()),
        ).use { k ->
            while (k.moveToNext()) {
                hasil += Halaman(
                    id = k.getLong(0),
                    dokumenId = k.getLong(1),
                    urutan = k.getInt(2),
                    gambar = k.getString(3),
                    thumb = k.getString(4),
                    teks = k.getString(5),
                    kotak = k.getString(6),
                )
            }
        }
        return hasil
    }

    fun hapusHalaman(halaman: Halaman) {
        val db = writableDatabase
        db.delete("halaman", "id = ?", arrayOf(halaman.id.toString()))
        db.delete("halaman_fts", "rowid = ?", arrayOf(halaman.id.toString()))
        sentuh(halaman.dokumenId)
    }

    fun urutkanUlang(dokumenId: Long, urutanBaru: List<Long>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            urutanBaru.forEachIndexed { i, id ->
                val nilai = ContentValues().apply { put("urutan", i) }
                db.update("halaman", nilai, "id = ?", arrayOf(id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        sentuh(dokumenId)
    }

    fun dokumen(): List<Dokumen> {
        val hasil = mutableListOf<Dokumen>()
        readableDatabase.rawQuery(
            """
            SELECT d.id, d.nama, d.dibuat, d.diubah,
                (SELECT COUNT(*) FROM halaman h WHERE h.dokumen_id = d.id) AS jumlah,
                (SELECT h.thumb FROM halaman h WHERE h.dokumen_id = d.id ORDER BY h.urutan LIMIT 1) AS thumb
            FROM dokumen d
            ORDER BY d.diubah DESC
            """.trimIndent(),
            null,
        ).use { k ->
            while (k.moveToNext()) {
                hasil += Dokumen(
                    id = k.getLong(0),
                    nama = k.getString(1),
                    dibuat = k.getLong(2),
                    diubah = k.getLong(3),
                    jumlahHalaman = k.getInt(4),
                    thumb = k.getString(5),
                )
            }
        }
        return hasil
    }

    fun cari(kata: String): List<Dokumen> {
        val bersih = kata.trim()
        if (bersih.isEmpty()) return dokumen()
        val hasil = mutableListOf<Dokumen>()
        readableDatabase.rawQuery(
            """
            SELECT DISTINCT d.id, d.nama, d.dibuat, d.diubah,
                (SELECT COUNT(*) FROM halaman h2 WHERE h2.dokumen_id = d.id) AS jumlah,
                (SELECT h3.thumb FROM halaman h3 WHERE h3.dokumen_id = d.id ORDER BY h3.urutan LIMIT 1) AS thumb
            FROM dokumen d
            JOIN halaman h ON h.dokumen_id = d.id
            JOIN halaman_fts f ON f.rowid = h.id
            WHERE halaman_fts MATCH ?
            ORDER BY d.diubah DESC
            """.trimIndent(),
            arrayOf("$bersih*"),
        ).use { k ->
            while (k.moveToNext()) {
                hasil += Dokumen(
                    id = k.getLong(0),
                    nama = k.getString(1),
                    dibuat = k.getLong(2),
                    diubah = k.getLong(3),
                    jumlahHalaman = k.getInt(4),
                    thumb = k.getString(5),
                )
            }
        }
        return hasil
    }

    fun dokumenById(id: Long): Dokumen? = dokumen().firstOrNull { it.id == id }

    fun ubahNama(id: Long, nama: String) {
        val nilai = ContentValues().apply { put("nama", nama) }
        writableDatabase.update("dokumen", nilai, "id = ?", arrayOf(id.toString()))
    }

    fun hapusDokumen(id: Long) {
        val db = writableDatabase
        val idHalaman = halaman(id).map { it.id }
        idHalaman.forEach { db.delete("halaman_fts", "rowid = ?", arrayOf(it.toString())) }
        db.delete("halaman", "dokumen_id = ?", arrayOf(id.toString()))
        db.delete("dokumen", "id = ?", arrayOf(id.toString()))
    }

    private fun sentuh(dokumenId: Long) {
        val nilai = ContentValues().apply { put("diubah", System.currentTimeMillis()) }
        writableDatabase.update("dokumen", nilai, "id = ?", arrayOf(dokumenId.toString()))
    }
}
