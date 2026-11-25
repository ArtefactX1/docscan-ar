package com.zacky.docar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Ikon digambar dengan Canvas, bukan glyph font atau emoji, supaya tebal garis
 * dan ujungnya seragam di semua ukuran.
 */
enum class Ikon {
    Kembali,
    Naik,
    Turun,
    Hapus,
    Centang,
    Kamera,
    Petunjuk,
    Info,
    Panah,
    Sudut,
    Setelan,
    Arsip,
    Senter,
}

@Composable
fun IkonGambar(
    ikon: Ikon,
    warna: Color,
    modifier: Modifier = Modifier,
    ukuran: Dp = 22.dp,
    tebal: Dp = 1.8.dp,
) {
    Canvas(modifier.size(ukuran)) {
        val s = size.minDimension
        val t = tebal.toPx()
        when (ikon) {
            Ikon.Kembali -> {
                garis(Offset(s * 0.62f, s * 0.18f), Offset(s * 0.34f, s * 0.5f), warna, t)
                garis(Offset(s * 0.34f, s * 0.5f), Offset(s * 0.62f, s * 0.82f), warna, t)
            }

            Ikon.Naik -> {
                garis(Offset(s * 0.2f, s * 0.6f), Offset(s * 0.5f, s * 0.32f), warna, t)
                garis(Offset(s * 0.5f, s * 0.32f), Offset(s * 0.8f, s * 0.6f), warna, t)
            }

            Ikon.Turun -> {
                garis(Offset(s * 0.2f, s * 0.4f), Offset(s * 0.5f, s * 0.68f), warna, t)
                garis(Offset(s * 0.5f, s * 0.68f), Offset(s * 0.8f, s * 0.4f), warna, t)
            }

            Ikon.Hapus -> {
                garis(Offset(s * 0.28f, s * 0.28f), Offset(s * 0.72f, s * 0.72f), warna, t)
                garis(Offset(s * 0.72f, s * 0.28f), Offset(s * 0.28f, s * 0.72f), warna, t)
            }

            Ikon.Centang -> {
                garis(Offset(s * 0.24f, s * 0.52f), Offset(s * 0.43f, s * 0.71f), warna, t)
                garis(Offset(s * 0.43f, s * 0.71f), Offset(s * 0.77f, s * 0.31f), warna, t)
            }

            // Empat siku: sudut kertas.
            Ikon.Sudut -> {
                val p = s * 0.24f
                val q = s * 0.76f
                val l = s * 0.16f
                garis(Offset(p, p + l), Offset(p, p), warna, t)
                garis(Offset(p, p), Offset(p + l, p), warna, t)
                garis(Offset(q - l, p), Offset(q, p), warna, t)
                garis(Offset(q, p), Offset(q, p + l), warna, t)
                garis(Offset(q, q - l), Offset(q, q), warna, t)
                garis(Offset(q, q), Offset(q - l, q), warna, t)
                garis(Offset(p + l, q), Offset(p, q), warna, t)
                garis(Offset(p, q), Offset(p, q - l), warna, t)
            }

            // Tiga slider: bahasa visual lazim untuk pengaturan, tanpa roda gigi.
            Ikon.Setelan -> {
                listOf(0.3f to 0.38f, 0.5f to 0.66f, 0.7f to 0.46f).forEach { (y, x) ->
                    garis(Offset(s * 0.18f, s * y), Offset(s * 0.82f, s * y), warna, t)
                    drawCircle(warna, radius = t * 1.35f, center = Offset(s * x, s * y))
                }
            }

            Ikon.Arsip -> {
                drawRoundRect(
                    color = warna,
                    topLeft = Offset(s * 0.2f, s * 0.24f),
                    size = Size(s * 0.6f, s * 0.56f),
                    cornerRadius = CornerRadius(s * 0.08f),
                    style = Stroke(width = t),
                )
                garis(Offset(s * 0.2f, s * 0.42f), Offset(s * 0.8f, s * 0.42f), warna, t)
                garis(Offset(s * 0.4f, s * 0.58f), Offset(s * 0.6f, s * 0.58f), warna, t)
            }

            // Badan kamera + lensa.
            Ikon.Kamera -> {
                drawRoundRect(
                    color = warna,
                    topLeft = Offset(s * 0.16f, s * 0.3f),
                    size = Size(s * 0.68f, s * 0.46f),
                    cornerRadius = CornerRadius(s * 0.09f),
                    style = Stroke(width = t),
                )
                garis(Offset(s * 0.38f, s * 0.3f), Offset(s * 0.42f, s * 0.2f), warna, t)
                garis(Offset(s * 0.42f, s * 0.2f), Offset(s * 0.58f, s * 0.2f), warna, t)
                garis(Offset(s * 0.58f, s * 0.2f), Offset(s * 0.62f, s * 0.3f), warna, t)
                drawCircle(
                    color = warna,
                    radius = s * 0.13f,
                    center = Offset(s * 0.5f, s * 0.53f),
                    style = Stroke(width = t),
                )
            }

            // Buku terbuka: panduan.
            Ikon.Petunjuk -> {
                garis(Offset(s * 0.5f, s * 0.26f), Offset(s * 0.5f, s * 0.8f), warna, t)
                val kiri = Path().apply {
                    moveTo(s * 0.5f, s * 0.26f)
                    lineTo(s * 0.18f, s * 0.3f)
                    lineTo(s * 0.18f, s * 0.76f)
                    lineTo(s * 0.5f, s * 0.8f)
                    close()
                }
                val kanan = Path().apply {
                    moveTo(s * 0.5f, s * 0.26f)
                    lineTo(s * 0.82f, s * 0.3f)
                    lineTo(s * 0.82f, s * 0.76f)
                    lineTo(s * 0.5f, s * 0.8f)
                    close()
                }
                drawPath(kiri, warna, style = Stroke(width = t))
                drawPath(kanan, warna, style = Stroke(width = t))
            }

            Ikon.Info -> {
                drawCircle(
                    color = warna,
                    radius = s * 0.32f,
                    center = Offset(s * 0.5f, s * 0.5f),
                    style = Stroke(width = t),
                )
                garis(Offset(s * 0.5f, s * 0.44f), Offset(s * 0.5f, s * 0.66f), warna, t)
                drawCircle(warna, radius = t * 0.9f, center = Offset(s * 0.5f, s * 0.34f))
            }

            // Chevron kanan untuk baris menu.
            Ikon.Panah -> {
                garis(Offset(s * 0.44f, s * 0.26f), Offset(s * 0.62f, s * 0.5f), warna, t)
                garis(Offset(s * 0.62f, s * 0.5f), Offset(s * 0.44f, s * 0.74f), warna, t)
            }

            Ikon.Senter -> {
                val bolt = Path().apply {
                    moveTo(s * 0.56f, s * 0.06f)
                    lineTo(s * 0.24f, s * 0.54f)
                    lineTo(s * 0.46f, s * 0.54f)
                    lineTo(s * 0.42f, s * 0.94f)
                    lineTo(s * 0.76f, s * 0.44f)
                    lineTo(s * 0.52f, s * 0.44f)
                    close()
                }
                drawPath(bolt, warna, style = Stroke(width = t, cap = StrokeCap.Round))
            }
        }
    }
}

private fun DrawScope.garis(a: Offset, b: Offset, warna: Color, tebal: Float) {
    drawLine(warna, a, b, strokeWidth = tebal, cap = StrokeCap.Round)
}
