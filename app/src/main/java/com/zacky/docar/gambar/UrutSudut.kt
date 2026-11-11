package com.zacky.docar.gambar

data class Titik(val x: Float, val y: Float)

object UrutSudut {
    /**
     * Urutkan jadi kiri-atas, kanan-atas, kanan-bawah, kiri-bawah.
     */
    fun urut(titik: List<Titik>): List<Titik> {
        require(titik.size == 4) { "butuh tepat 4 titik" }
        val menurutY = titik.sortedBy { it.y }
        val atas = menurutY.take(2).sortedBy { it.x }
        val bawah = menurutY.drop(2).sortedBy { it.x }
        return listOf(atas[0], atas[1], bawah[1], bawah[0])
    }
}
