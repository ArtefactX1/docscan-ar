package com.zacky.docar.data

data class Dokumen(
    val id: Long,
    val nama: String,
    val dibuat: Long,
    val diubah: Long,
    val jumlahHalaman: Int,
    val thumb: String?,
)

data class Halaman(
    val id: Long,
    val dokumenId: Long,
    val urutan: Int,
    val gambar: String,
    val thumb: String,
    val teks: String,
    val kotak: String,
)
