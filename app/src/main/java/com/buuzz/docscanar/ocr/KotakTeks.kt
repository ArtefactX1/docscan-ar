package com.buuzz.docscanar.ocr

import org.json.JSONArray
import org.json.JSONObject

/** Serialisasi kotak teks ke JSON supaya PDF bisa dibangun ulang tanpa OCR ulang. */
object KotakTeks {
    fun keJson(baris: List<BarisOcr>): String {
        val arr = JSONArray()
        baris.forEach { b ->
            arr.put(
                JSONObject().apply {
                    put("t", b.teks)
                    put("l", b.kiri.toDouble())
                    put("a", b.atas.toDouble())
                    put("r", b.kanan.toDouble())
                    put("b", b.bawah.toDouble())
                }
            )
        }
        return arr.toString()
    }

    fun dariJson(json: String): List<BarisOcr> {
        if (json.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                BarisOcr(
                    teks = o.getString("t"),
                    kiri = o.getDouble("l").toFloat(),
                    atas = o.getDouble("a").toFloat(),
                    kanan = o.getDouble("r").toFloat(),
                    bawah = o.getDouble("b").toFloat(),
                )
            }
        }.getOrDefault(emptyList())
    }
}
