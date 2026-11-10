package com.buuzz.docscanar.data

data class Dokumen(
    val id: Long,
    val nama: String,
    val path: String,
    val thumb: String?,
    val teks: String,
    val dibuat: Long,
)
