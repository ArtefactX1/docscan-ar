package com.buuzz.docscanar.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Satu tabel dokumen plus tabel FTS4 untuk pencarian isi teks.
 * SQLite langsung, tanpa Room: hanya satu tabel, tidak perlu generator.
 */
class DokumenDb(context: Context) :
    SQLiteOpenHelper(context, "docscan.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE dokumen (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                path TEXT NOT NULL,
                thumb TEXT,
                teks TEXT NOT NULL DEFAULT '',
                dibuat INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE VIRTUAL TABLE dokumen_fts USING fts4(teks)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun tambah(nama: String, path: String, thumb: String?, teks: String): Long {
        val db = writableDatabase
        val nilai = ContentValues().apply {
            put("nama", nama)
            put("path", path)
            put("thumb", thumb)
            put("teks", teks)
            put("dibuat", System.currentTimeMillis())
        }
        val id = db.insert("dokumen", null, nilai)
        if (id > 0) {
            db.execSQL("INSERT INTO dokumen_fts(rowid, teks) VALUES (?, ?)", arrayOf(id, teks))
        }
        return id
    }

    fun semua(): List<Dokumen> = baca(
        "SELECT id, nama, path, thumb, teks, dibuat FROM dokumen ORDER BY dibuat DESC",
        null,
    )

    fun cari(kata: String): List<Dokumen> {
        val bersih = kata.trim()
        if (bersih.isEmpty()) return semua()
        return baca(
            """
            SELECT d.id, d.nama, d.path, d.thumb, d.teks, d.dibuat
            FROM dokumen d
            JOIN dokumen_fts f ON f.rowid = d.id
            WHERE dokumen_fts MATCH ?
            ORDER BY d.dibuat DESC
            """.trimIndent(),
            arrayOf("$bersih*"),
        )
    }

    private fun baca(sql: String, args: Array<String>?): List<Dokumen> {
        val hasil = mutableListOf<Dokumen>()
        readableDatabase.rawQuery(sql, args).use { kursor ->
            while (kursor.moveToNext()) {
                hasil += Dokumen(
                    id = kursor.getLong(0),
                    nama = kursor.getString(1),
                    path = kursor.getString(2),
                    thumb = kursor.getString(3),
                    teks = kursor.getString(4),
                    dibuat = kursor.getLong(5),
                )
            }
        }
        return hasil
    }
}
