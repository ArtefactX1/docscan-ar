package com.buuzz.docscanar.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buuzz.docscanar.kamera.KameraPreview
import com.buuzz.docscanar.kamera.SegiLayar
import com.buuzz.docscanar.kamera.rememberKameraController
import com.buuzz.docscanar.sensor.Kesiapan
import com.buuzz.docscanar.sensor.ingatSensorKamera
import com.buuzz.docscanar.ui.theme.Peringatan
import com.buuzz.docscanar.ui.theme.Siap
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun KameraScreen(onFoto: (File) -> Unit, onArsip: () -> Unit) {
    val sensor = ingatSensorKamera()
    val controller = rememberKameraController()
    var segi by remember { mutableStateOf<SegiLayar?>(null) }
    var detikTerakhir by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            if (System.currentTimeMillis() - detikTerakhir > 700) segi = null
        }
    }

    val siap = Kesiapan.siap(
        sudut = sensor.sudut,
        guncangan = sensor.guncangan,
        berputar = sensor.berputar,
        lux = sensor.lux,
        adaDokumen = segi != null,
    )

    var terkunci by remember { mutableStateOf(false) }
    var senter by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(terkunci) {
        if (terkunci) {
            delay(1200)
            terkunci = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        KameraPreview(
            controller = controller,
            siap = siap,
            lux = sensor.lux,
            senter = senter,
            onFoto = { file ->
                terkunci = true
                onFoto(file)
            },
            onSegi = { hasil ->
                detikTerakhir = System.currentTimeMillis()
                segi = hasil
            },
            modifier = Modifier.fillMaxSize(),
        )
        PanduanKerataan(siap = siap, segi = segi)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xB8141618))
                        .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(50))
                        .clickable(onClick = onArsip)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = "Arsip",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFEDEFF2),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            BarisChip(
                siap = siap,
                sudut = sensor.sudut,
                lux = sensor.lux,
                guncangan = sensor.guncangan || sensor.berputar,
                adaDokumen = segi != null,
            )
            Spacer(Modifier.weight(1f))
            StatusKesiapan(siap = siap, adaDokumen = segi != null)
            Spacer(Modifier.height(20.dp))
            KontrolBawah(
                terkunci = terkunci,
                tersedia = sensor.tersedia,
                senter = senter,
                onToggleSenter = {
                    senter = when (senter) {
                        null -> true
                        true -> false
                        false -> null
                    }
                },
                onCapture = { controller.capture() },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PanduanKerataan(siap: Boolean, segi: SegiLayar?) {
    val warna = if (siap) Siap else Color.White.copy(alpha = 0.55f)

    Canvas(Modifier.fillMaxSize()) {
        // Preview memakai FIT_CENTER: koordinat ternormalisasi dipetakan ke kotak
        // gambar yang benar-benar tampil, bukan seluruh layar.
        val rasio = segi?.rasio ?: (4f / 3f)
        val lebarGambar: Float
        val tinggiGambar: Float
        if (rasio > size.width / size.height) {
            lebarGambar = size.width
            tinggiGambar = size.width / rasio
        } else {
            tinggiGambar = size.height
            lebarGambar = size.height * rasio
        }
        val kiri = (size.width - lebarGambar) / 2f
        val atas = (size.height - tinggiGambar) / 2f

        val sudut = if (segi != null && segi.titik.size == 4) {
            segi.titik.map { Offset(kiri + it.x * lebarGambar, atas + it.y * tinggiGambar) }
        } else {
            listOf(
                Offset(size.width * 0.14f, size.height * 0.25f),
                Offset(size.width * 0.86f, size.height * 0.23f),
                Offset(size.width * 0.90f, size.height * 0.76f),
                Offset(size.width * 0.11f, size.height * 0.78f),
            )
        }

        val jalur = Path().apply {
            moveTo(sudut[0].x, sudut[0].y)
            lineTo(sudut[1].x, sudut[1].y)
            lineTo(sudut[2].x, sudut[2].y)
            lineTo(sudut[3].x, sudut[3].y)
            close()
        }
        drawPath(jalur, warna, style = Stroke(width = 2.dp.toPx()))

        val panjang = 22.dp.toPx()
        fun siku(titik: Offset, dx: Float, dy: Float) {
            drawLine(warna, titik, Offset(titik.x + dx * panjang, titik.y), strokeWidth = 2.dp.toPx())
            drawLine(warna, titik, Offset(titik.x, titik.y + dy * panjang), strokeWidth = 2.dp.toPx())
        }
        siku(sudut[0], 1f, 1f)
        siku(sudut[1], -1f, 1f)
        siku(sudut[2], -1f, -1f)
        siku(sudut[3], 1f, -1f)

        val tengah = (sudut[0].y + sudut[3].y) / 2f
        drawLine(
            color = warna.copy(alpha = 0.6f),
            start = Offset(sudut[0].x, tengah),
            end = Offset(sudut[1].x, tengah),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 14f)),
        )
        drawCircle(
            color = warna.copy(alpha = 0.5f),
            radius = 14.dp.toPx(),
            center = Offset(size.width / 2f, tengah),
            style = Stroke(width = 1.5.dp.toPx()),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BarisChip(
    siap: Boolean,
    sudut: Float,
    lux: Float?,
    guncangan: Boolean,
    adaDokumen: Boolean,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Chip(
            label = "Sudut",
            nilai = String.format(Locale.US, "%.1f", sudut),
            satuan = "°",
            siap = siap,
        )
        Chip(
            label = "Cahaya",
            nilai = when {
                lux == null -> "—"
                lux >= Kesiapan.LUX_MIN -> "cukup"
                else -> "redup"
            },
            satuan = null,
            siap = siap,
        )
        Chip(
            label = "Getar",
            nilai = if (guncangan) "goyah" else "stabil",
            satuan = null,
            siap = siap,
        )
        Chip(
            label = "Kertas",
            nilai = if (adaDokumen) "terdeteksi" else "belum",
            satuan = null,
            siap = siap,
        )
    }
}

@Composable
private fun Chip(label: String, nilai: String, satuan: String?, siap: Boolean) {
    val latar = if (siap) Color(0xE0102E1E) else Color(0xE03A2808)
    val garis = if (siap) Siap.copy(alpha = 0.45f) else Peringatan.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(latar)
            .border(1.dp, garis, RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF9BA3AC),
        )
        if (satuan != null) {
            NumText(nilai = nilai, style = MaterialTheme.typography.labelMedium, warna = Color(0xFFEAECEF))
            Text(
                text = satuan,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9BA3AC),
            )
        } else {
            Text(
                text = nilai,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFEAECEF),
            )
        }
    }
}

/**
 * Angka dengan lebar slot tetap. Poppins tidak punya tabular figures, jadi
 * "1.2" dan "11.2" akan menggeser tetangganya tanpa ini.
 */
@Composable
fun NumText(nilai: String, style: TextStyle, warna: Color) {
    val density = LocalDensity.current
    val ukuran = if (style.fontSize.isSp) style.fontSize.value else 12f
    val lebarDigit = with(density) { (ukuran * 0.58f).sp.toDp() }
    val lebarLain = with(density) { (ukuran * 0.28f).sp.toDp() }

    Row {
        nilai.forEach { karakter ->
            Text(
                text = karakter.toString(),
                style = style,
                color = warna,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(if (karakter.isDigit()) lebarDigit else lebarLain),
            )
        }
    }
}

@Composable
private fun StatusKesiapan(siap: Boolean, adaDokumen: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (siap) "Siap" else "Belum siap",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = if (siap) 32.sp else 22.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = if (siap) Siap else Peringatan,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = when {
                siap -> "Tahan diam, memotret otomatis"
                !adaDokumen -> "Arahkan kamera ke kertas"
                else -> "Dekatkan ke kertas, hindari bayangan"
            },
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.74f),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PillButton(teks: String, aktif: Boolean, aktifkan: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(50))
            .background(if (aktif) Color(0xFFE7E9EC) else Color(0xB8141618))
            .border(
                width = 1.dp,
                color = if (aktif) Color.Transparent else Color.White.copy(alpha = 0.24f),
                shape = RoundedCornerShape(50),
            )
            .clickable(enabled = aktifkan, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(
            text = teks,
            style = MaterialTheme.typography.labelMedium,
            color = if (aktif) Color(0xFF0A0A0B) else Color(0xFFEDEFF2),
        )
    }
}

@Composable
private fun SenterButton(senter: Boolean?, onClick: () -> Unit) {
    val nyala = senter == true
    val otomatis = senter == null
    val warna = when {
        nyala -> Color(0xFF0A0A0B)
        otomatis -> Color(0xFFEDEFF2)
        else -> Color.White.copy(alpha = 0.45f)
    }

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(if (nyala) Color(0xFFE7E9EC) else Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(24.dp)) {
            val bolt = Path().apply {
                moveTo(size.width * 0.56f, size.height * 0.06f)
                lineTo(size.width * 0.24f, size.height * 0.54f)
                lineTo(size.width * 0.46f, size.height * 0.54f)
                lineTo(size.width * 0.42f, size.height * 0.94f)
                lineTo(size.width * 0.76f, size.height * 0.44f)
                lineTo(size.width * 0.52f, size.height * 0.44f)
                close()
            }
            if (nyala) {
                drawPath(bolt, warna)
            } else {
                drawPath(bolt, warna, style = Stroke(width = 1.8.dp.toPx()))
            }
        }
    }
}

@Composable
private fun KontrolBawah(
    terkunci: Boolean,
    tersedia: Boolean,
    senter: Boolean?,
    onToggleSenter: () -> Unit,
    onCapture: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PillButton(
            teks = "Ambil manual",
            aktif = false,
            aktifkan = !terkunci,
            onClick = onCapture,
        )

        Spacer(Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            SenterButton(senter = senter, onClick = onToggleSenter)

            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .border(
                        width = 4.dp,
                        color = Color.White.copy(alpha = if (terkunci) 0.4f else 0.9f),
                        shape = CircleShape,
                    )
                    .clickable(enabled = !terkunci, onClick = onCapture),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(if (terkunci) Color.White.copy(alpha = 0.35f) else Color.White)
                )
            }

            Spacer(Modifier.size(56.dp))
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = when {
                terkunci -> "Terkunci"
                !tersedia -> "Akselerometer tidak tersedia — pakai ambil manual"
                else -> "Auto-capture aktif saat ketiga indikator hijau"
            },
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.52f),
            textAlign = TextAlign.Center,
        )
    }
}
