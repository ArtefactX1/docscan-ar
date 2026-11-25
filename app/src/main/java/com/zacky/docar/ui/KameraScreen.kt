package com.zacky.docar.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zacky.docar.kamera.KameraPreview
import com.zacky.docar.kamera.MesinCapture
import com.zacky.docar.kamera.SegiLayar
import com.zacky.docar.kamera.StatusCapture
import com.zacky.docar.kamera.rememberKameraController
import com.zacky.docar.sensor.Kesiapan
import com.zacky.docar.sensor.ingatSensorKamera
import com.zacky.docar.ui.theme.Angka
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Peringatan
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.Siap
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
import java.io.File
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun KameraScreen(
    sudutMaks: Float,
    modeSenter: Int,
    onModeSenter: (Int) -> Unit,
    onFoto: (File) -> Unit,
    onArsip: () -> Unit,
    onKembali: () -> Unit,
) {
    val sensor = ingatSensorKamera()
    val controller = rememberKameraController()
    var segi by remember { mutableStateOf<SegiLayar?>(null) }
    val segiKini by rememberUpdatedState(segi)
    var terangFrame by remember { mutableStateOf<Float?>(null) }
    val terangKini by rememberUpdatedState(terangFrame)
    var bayangan by remember { mutableStateOf(false) }
    val luxKini by rememberUpdatedState(sensor.lux)
    val sudutKini by rememberUpdatedState(sensor.sudut)
    val mesin = remember { MesinCapture() }
    var status by remember { mutableStateOf(StatusCapture.SEARCH) }
    var progres by remember { mutableStateOf(0f) }

    // Auto-capture berkala: syaratnya kertas terdeteksi dan quad tidak berubah
    // antar frame (IoU). Sensor gerak tidak dipakai — denyut tangan selalu
    // memicunya dan membuat capture tidak pernah jalan.
    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            val siapSensor = Kesiapan.siap(
                lux = luxKini,
                terangFrame = terangKini,
                sudut = sudutKini,
            )
            status = mesin.perbarui(
                adaKertas = segiKini != null,
                luasRasio = segiKini?.luas ?: 0f,
                posisiStabil = segiKini?.stabil == true,
                sensorSiap = siapSensor,
                sekarang = System.currentTimeMillis(),
            )
            progres = mesin.progres
        }
    }

    val siap = status == StatusCapture.SIAP
    var terkunci by remember { mutableStateOf(false) }
    val senter = when (modeSenter) {
        1 -> true
        2 -> false
        else -> null
    }
    val kilat = remember { Animatable(0f) }

    LaunchedEffect(terkunci) {
        if (terkunci) {
            kilat.snapTo(0.85f)
            kilat.animateTo(0f, tween(320))
            delay(880)
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
                mesin.reset()
                onFoto(file)
            },
            onSegi = { hasil -> segi = hasil },
            onCahaya = { terang, adaBayangan ->
                terangFrame = terang
                bayangan = adaBayangan
            },
            modifier = Modifier.fillMaxSize(),
        )
        PanduanKerataan(siap = siap, segi = segi)

        // Kilat putih singkat saat capture supaya pengguna tahu foto diambil.
        if (kilat.value > 0f) {
            Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = kilat.value)))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = S4),
        ) {
            Spacer(Modifier.height(S2))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(S2, Alignment.End),
            ) {
                IkonAtas(Ikon.Kembali, "Menu", onKembali)
                IkonAtas(Ikon.Arsip, "Arsip", onArsip)
            }
            Spacer(Modifier.weight(1f))
            Telemetri(
                sudut = sensor.sudut,
                sudutMaks = sudutMaks,
                lux = sensor.lux,
                terangFrame = terangFrame,
                bayangan = bayangan,
                guncangan = sensor.guncangan || sensor.berputar,
                adaDokumen = segi != null,
                diam = segi?.stabil == true,
            )
            Spacer(Modifier.height(S2))
            Petunjuk(status = status, terkunci = terkunci, tersedia = sensor.tersedia)
            Spacer(Modifier.height(S4))
            KontrolBawah(
                terkunci = terkunci,
                progres = progres,
                senter = senter,
                onToggleSenter = { onModeSenter((modeSenter + 1) % 3) },
                onCapture = { controller.capture() },
            )
            Spacer(Modifier.height(S3))
        }
    }
}

@Composable
private fun PanduanKerataan(siap: Boolean, segi: SegiLayar?) {
    val warna = if (siap) Siap else Color.White.copy(alpha = 0.55f)
    Canvas(Modifier.fillMaxSize()) {
        // Belum ada kertas terdeteksi: tidak ada kotak sama sekali.
        val titik = segi?.titik?.takeIf { it.size == 4 } ?: return@Canvas
        // FIT_CENTER: koordinat ternormalisasi dipetakan ke kotak gambar yang tampil.
        val rasio = segi.rasio
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
        val sudut = titik.map { Offset(kiri + it.x * lebarGambar, atas + it.y * tinggiGambar) }

        val jalur = Path().apply {
            moveTo(sudut[0].x, sudut[0].y)
            lineTo(sudut[1].x, sudut[1].y)
            lineTo(sudut[2].x, sudut[2].y)
            lineTo(sudut[3].x, sudut[3].y)
            close()
        }
        drawPath(jalur, warna, style = Stroke(width = 2.dp.toPx()))

        val panjang = 22.dp.toPx()
        fun siku(p: Offset, dx: Float, dy: Float) {
            drawLine(warna, p, Offset(p.x + dx * panjang, p.y), strokeWidth = 2.dp.toPx())
            drawLine(warna, p, Offset(p.x, p.y + dy * panjang), strokeWidth = 2.dp.toPx())
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

@Composable
private fun IkonAtas(ikon: Ikon, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IkonGambar(ikon, Color.White.copy(alpha = 0.9f))
    }
}

/** Satu baris mono: lebih cepat dibaca daripada empat chip. */
@Composable
private fun Telemetri(
    sudut: Float,
    sudutMaks: Float,
    lux: Float?,
    terangFrame: Float?,
    bayangan: Boolean,
    guncangan: Boolean,
    adaDokumen: Boolean,
    diam: Boolean,
) {
    val datar = Kesiapan.cukupDatar(sudut, sudutMaks)
    val cahaya = when {
        bayangan -> "bayangan"
        (lux != null && lux < Kesiapan.LUX_MIN) ||
            (terangFrame != null && terangFrame < Kesiapan.TERANG_MIN) -> "redup"
        lux == null && terangFrame == null -> "—"
        else -> "cukup"
    }
    val bagian = listOf(
        String.format(Locale.US, "%.1f°", sudut) to datar,
        cahaya to (cahaya == "cukup"),
        (if (guncangan) "goyah" else "stabil") to !guncangan,
        (if (adaDokumen) "kertas" else "—") to adaDokumen,
        (if (diam) "diam" else "gerak") to diam,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(S3, Alignment.CenterHorizontally),
    ) {
        bagian.forEachIndexed { i, (teks, baik) ->
            if (i > 0) {
                Text("·", style = Angka, color = TeksRedup.copy(alpha = 0.5f))
            }
            Text(
                text = teks,
                style = Angka,
                color = if (baik) Teks.copy(alpha = 0.9f) else Peringatan,
            )
        }
    }
}

@Composable
private fun Petunjuk(status: StatusCapture, terkunci: Boolean, tersedia: Boolean) {
    val teks = when {
        terkunci -> "Terkunci"
        !tersedia -> "Akselerometer tidak tersedia — ambil manual"
        status == StatusCapture.SIAP -> "Tahan diam — memotret"
        status == StatusCapture.SEARCH -> "Arahkan ke kertas"
        status == StatusCapture.CLOSER -> "Dekatkan ke kertas"
        else -> "Tahan diam sebentar"
    }
    Text(
        text = teks,
        style = MaterialTheme.typography.labelMedium,
        color = if (status == StatusCapture.SIAP) Siap else Color.White.copy(alpha = 0.72f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun KontrolBawah(
    terkunci: Boolean,
    progres: Float,
    senter: Boolean?,
    onToggleSenter: () -> Unit,
    onCapture: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        SenterButton(senter = senter, onClick = onToggleSenter)

        Shutter(terkunci = terkunci, progres = progres, onClick = onCapture)

        // Penyeimbang supaya shutter tetap di tengah.
        Spacer(Modifier.size(48.dp))
    }
}

@Composable
private fun SenterButton(senter: Boolean?, onClick: () -> Unit) {
    val nyala = senter == true
    val warna = when {
        nyala -> Latar
        senter == null -> Color.White.copy(alpha = 0.9f)
        else -> Color.White.copy(alpha = 0.4f)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (nyala) Color(0xFFE7E9EC) else Color.White.copy(alpha = 0.1f)
            )
            .clickable(onClickLabel = "Mode senter", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IkonGambar(Ikon.Senter, warna, ukuran = 20.dp)
    }
}

/**
 * Cincin di sekeliling shutter mengisi seiring frame stabil terkumpul, jadi
 * pengguna melihat progres tanpa membaca teks status.
 */
@Composable
private fun Shutter(terkunci: Boolean, progres: Float, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(CircleShape)
            .clickable(enabled = !terkunci, onClickLabel = "Ambil foto", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val t = 2.dp.toPx()
            val r = size.minDimension / 2f - t
            drawCircle(
                color = Color.White.copy(alpha = 0.16f),
                radius = r,
                style = Stroke(width = t),
            )
            if (progres > 0f) {
                drawArc(
                    color = Siap,
                    startAngle = -90f,
                    sweepAngle = 360f * progres.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(t, t),
                    size = androidx.compose.ui.geometry.Size(size.width - t * 2, size.height - t * 2),
                    style = Stroke(width = t, cap = StrokeCap.Round),
                )
            }
        }
        Box(
            Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(
                    if (terkunci) Color.White.copy(alpha = 0.35f) else Color.White
                )
        )
    }
}
