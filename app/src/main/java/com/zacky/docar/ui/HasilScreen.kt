package com.zacky.docar.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zacky.docar.gambar.ModeWarna
import com.zacky.docar.gambar.Pemindai
import com.zacky.docar.gambar.Titik
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Peringatan
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.R1
import com.zacky.docar.ui.theme.R2
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.S5
import com.zacky.docar.ui.theme.Siap
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
import com.zacky.docar.ui.theme.Garis
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProsesScreen(
    file: File,
    onSelesai: (Bitmap, List<Titik>, List<Titik>?) -> Unit,
    onGagal: () -> Unit,
) {
    var tahap by remember { mutableIntStateOf(0) }

    LaunchedEffect(file) {
        val asli = withContext(Dispatchers.Default) { Pemindai.bacaGambar(file) }
        if (asli == null) {
            onGagal()
            return@LaunchedEffect
        }
        tahap = 1
        val ditemukan = withContext(Dispatchers.Default) { Pemindai.cariTepi(asli) }
        val sudut = ditemukan ?: Pemindai.sudutCadangan(asli.width, asli.height)
        tahap = 2
        withContext(Dispatchers.Default) { Pemindai.rapikan(asli, sudut, ModeWarna.ABU) }
        tahap = 4
        onSelesai(asli, sudut, ditemukan)
    }

    val langkah = listOf("Memotong tepi", "Meluruskan perspektif", "Memperjelas teks", "Membaca teks")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(S5))
        Text("Memproses", style = MaterialTheme.typography.headlineMedium, color = Teks)
        Spacer(Modifier.height(S2))
        Text(
            text = "Tetap di halaman ini sampai selesai.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(210f / 297f)
                .clip(RoundedCornerShape(R2))
                .background(Color(0xFF1C1C1F)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (tahap == 0) "Mencari tepi kertas…" else "Tahap $tahap dari 4",
                style = MaterialTheme.typography.labelMedium,
                color = TeksRedup,
            )
        }

        Spacer(Modifier.height(28.dp))
        langkah.forEachIndexed { i, nama ->
            val selesai = i < tahap
            val aktif = i == tahap
            Row(
                modifier = Modifier.heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(S3),
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.5.dp, if (selesai || aktif) Peringatan else Garis, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = if (selesai) "✓" else "${i + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selesai || aktif) Peringatan else TeksRedup,
                    )
                }
                Text(
                    text = nama,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selesai || aktif) Teks else TeksRedup,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HasilScreen(
    asli: Bitmap,
    sudutAwal: List<Titik>,
    ditemukan: List<Titik>?,
    menyimpan: Boolean = false,
    onSimpan: (Bitmap) -> Unit,
    onScanUlang: () -> Unit,
) {
    var sudut by remember { mutableStateOf(sudutAwal) }
    var ubahSudut by remember { mutableStateOf(false) }
    var warna by remember { mutableStateOf(ModeWarna.ABU) }
    var hasil by remember { mutableStateOf<Bitmap?>(null) }
    var memproses by remember { mutableStateOf(false) }
    var sheetMode by remember { mutableStateOf(false) }

    // Preview lama tetap tampil selama yang baru dibuat, seperti ScanScreen PdfCraft.
    // LaunchedEffect membatalkan sendiri job lama saat sudut/warna berubah.
    LaunchedEffect(sudut, warna) {
        memproses = true
        val keluaran = withContext(Dispatchers.Default) { Pemindai.rapikan(asli, sudut, warna) }
        hasil = keluaran
        memproses = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = S4),
    ) {
        Spacer(Modifier.height(S2))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S1),
        ) {
            IkonKotak(Ikon.Kembali, "Kembali", onClick = onScanUlang)
            Text(
                text = "Hasil",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
            TombolMode(warna = warna, onClick = { sheetMode = true })
            IkonKotak(
                ikon = Ikon.Sudut,
                label = if (ubahSudut) "Selesai" else "Ubah sudut",
                aktif = ubahSudut,
                onClick = { ubahSudut = !ubahSudut },
            )
        }

        Spacer(Modifier.height(S3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(asli.width.toFloat() / asli.height.toFloat())
                    .clip(RoundedCornerShape(R1)),
            ) {
                Image(
                    bitmap = (if (ubahSudut) asli else hasil ?: asli).asImageBitmap(),
                    contentDescription = "Halaman hasil pemindaian",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )

                if (ubahSudut) {
                    SudutEditor(
                        asli = asli,
                        sudut = sudut,
                        onSudut = { sudut = it },
                    )
                }
            }

            if (memproses) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(S3)
                        .size(20.dp),
                    color = Siap,
                    strokeWidth = 2.dp,
                )
            }
        }

        if (ubahSudut) {
            Spacer(Modifier.height(S2))
            Text(
                text = "Geser titik ke sudut kertas. Quad cekung ditolak otomatis.",
                style = MaterialTheme.typography.labelSmall,
                color = TeksRedup,
            )
            Spacer(Modifier.height(S1))
            Row(horizontalArrangement = Arrangement.spacedBy(S4)) {
                TeksAksi("Auto-detect", aktifkan = ditemukan != null) {
                    ditemukan?.let { sudut = it }
                }
                TeksAksi("Seluruh foto") {
                    sudut = listOf(
                        Titik(0f, 0f),
                        Titik(asli.width.toFloat(), 0f),
                        Titik(asli.width.toFloat(), asli.height.toFloat()),
                        Titik(0f, asli.height.toFloat()),
                    )
                }
            }
        }

        Spacer(Modifier.height(S4))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(RoundedCornerShape(R2))
                .background(Aksen)
                .clickable(enabled = !memproses && !menyimpan) { onSimpan(hasil ?: asli) },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (menyimpan) "Menyimpan…" else "Simpan PDF",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Latar,
            )
        }

        Spacer(Modifier.height(S3))
    }

    if (sheetMode) {
        ModalBottomSheet(
            onDismissRequest = { sheetMode = false },
            containerColor = Permukaan,
            dragHandle = null,
        ) {
            Column(
                Modifier
                    .padding(horizontal = S4)
                    .padding(bottom = S5),
            ) {
                Text(
                    text = "Mode hasil",
                    style = MaterialTheme.typography.titleMedium,
                    color = TeksRedup,
                    modifier = Modifier.padding(vertical = S3),
                )
                ModeWarna.entries.forEach { mode ->
                    BarisMode(
                        label = labelMode(mode),
                        aktif = warna == mode,
                        onClick = {
                            warna = mode
                            sheetMode = false
                        },
                    )
                }
            }
        }
    }
}

private fun labelMode(mode: ModeWarna): String = when (mode) {
    ModeWarna.ASLI -> "Asli"
    ModeWarna.ABU -> "Abu"
    ModeWarna.HITAM_PUTIH -> "Hitam"
    ModeWarna.LINEAR -> "Linear"
}

@Composable
private fun TombolMode(warna: ModeWarna, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(R2))
            .background(Permukaan)
            .clickable(onClickLabel = "Pilih mode", onClick = onClick)
            .padding(horizontal = S3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(S1),
    ) {
        Text(
            text = labelMode(warna),
            style = MaterialTheme.typography.labelMedium,
            color = Teks,
        )
        IkonGambar(Ikon.Turun, TeksRedup, ukuran = 16.dp, tebal = 1.6.dp)
    }
}

@Composable
private fun BarisMode(label: String, aktif: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (aktif) Teks else TeksRedup,
        )
        if (aktif) IkonGambar(Ikon.Centang, Siap, ukuran = 18.dp)
    }
}

/**
 * Kaca pembesar 3x di pojok terjauh dari titik aktif, supaya tepi kertas yang
 * sedang digeser tetap terlihat.
 */
@Composable
private fun SudutEditor(
    asli: Bitmap,
    sudut: List<Titik>,
    onSudut: (List<Titik>) -> Unit,
) {
    val density = LocalDensity.current
    val radiusTitik = with(density) { 12.dp.toPx() }
    val ukuranKaca = with(density) { 120.dp.toPx() }
    val pembesaran = 3f
    val gambar = remember(asli) { asli.asImageBitmap() }

    val sudutKini by rememberUpdatedState(sudut)
    val onSudutKini by rememberUpdatedState(onSudut)
    var seret by remember { mutableStateOf(-1) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(asli) {
                detectDragGestures(
                    onDragStart = { posisi ->
                        val jarak = sudutKini.map { t ->
                            val p = Offset(t.x * size.width / asli.width, t.y * size.width / asli.width)
                            val dx = p.x - posisi.x
                            val dy = p.y - posisi.y
                            dx * dx + dy * dy
                        }
                        seret = jarak.indices.minByOrNull { jarak[it] } ?: -1
                    },
                    onDragEnd = { seret = -1 },
                    onDragCancel = { seret = -1 },
                    onDrag = { perubahan, jarak ->
                        perubahan.consume()
                        val i = seret
                        if (i < 0) return@detectDragGestures
                        val skala = size.width.toFloat() / asli.width
                        val lama = sudutKini[i]
                        val baru = Titik(
                            (lama.x + jarak.x / skala).coerceIn(0f, asli.width.toFloat()),
                            (lama.y + jarak.y / skala).coerceIn(0f, asli.height.toFloat()),
                        )
                        val usulan = sudutKini.toMutableList().also { it[i] = baru }
                        if (Pemindai.konveks(usulan)) onSudutKini(usulan)
                    },
                )
            },
    ) {
        val skala = size.width / asli.width
        val titik = sudut.map { Offset(it.x * skala, it.y * skala) }

        for (i in titik.indices) {
            drawLine(
                color = Color(0xFF3DDC97),
                start = titik[i],
                end = titik[(i + 1) % titik.size],
                strokeWidth = 2.dp.toPx(),
            )
        }

        titik.forEach { p ->
            drawCircle(Color(0xFF0A0A0B), radiusTitik + 3.dp.toPx(), p)
            drawCircle(Color(0xFF3DDC97), radiusTitik, p)
            drawCircle(Color(0xFF0A0A0B), radiusTitik * 0.35f, p)
        }

        val aktif = seret
        if (aktif < 0 || aktif >= titik.size) return@Canvas

        val fokus = titik[aktif]

        // Kaca ditaruh di pojok yang paling jauh dari titik aktif.
        val kanan = fokus.x < size.width / 2f
        val bawah = fokus.y < size.height / 2f
        val kacaKiri = if (kanan) size.width - ukuranKaca - 12.dp.toPx() else 12.dp.toPx()
        val kacaAtas = if (bawah) size.height - ukuranKaca - 12.dp.toPx() else 12.dp.toPx()
        val pusatKaca = Offset(kacaKiri + ukuranKaca / 2f, kacaAtas + ukuranKaca / 2f)

        clipRect(
            left = kacaKiri,
            top = kacaAtas,
            right = kacaKiri + ukuranKaca,
            bottom = kacaAtas + ukuranKaca,
        ) {
            drawRect(Color(0xFF141416), Offset(kacaKiri, kacaAtas), Size(ukuranKaca, ukuranKaca))

            // Bidang sumber: lebar kaca dibagi pembesaran, diubah ke koordinat bitmap.
            val sisiSumber = (ukuranKaca / (pembesaran * skala)).toInt().coerceAtLeast(1)
            val pusatSumberX = (fokus.x / skala).toInt()
            val pusatSumberY = (fokus.y / skala).toInt()
            val srcX = (pusatSumberX - sisiSumber / 2).coerceIn(0, maxOf(0, asli.width - sisiSumber))
            val srcY = (pusatSumberY - sisiSumber / 2).coerceIn(0, maxOf(0, asli.height - sisiSumber))

            drawImage(
                image = gambar,
                srcOffset = IntOffset(srcX, srcY),
                srcSize = IntSize(sisiSumber, sisiSumber),
                dstOffset = IntOffset(kacaKiri.toInt(), kacaAtas.toInt()),
                dstSize = IntSize(ukuranKaca.toInt(), ukuranKaca.toInt()),
            )
        }

        drawRect(
            color = Color(0xFF3DDC97),
            topLeft = Offset(kacaKiri, kacaAtas),
            size = Size(ukuranKaca, ukuranKaca),
            style = Stroke(width = 2.dp.toPx()),
        )

        // Garis bidik di tengah kaca.
        val setengahKaca = ukuranKaca * 0.18f
        drawLine(
            color = Color(0xFF3DDC97).copy(alpha = 0.8f),
            start = Offset(pusatKaca.x - setengahKaca, pusatKaca.y),
            end = Offset(pusatKaca.x + setengahKaca, pusatKaca.y),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = Color(0xFF3DDC97).copy(alpha = 0.8f),
            start = Offset(pusatKaca.x, pusatKaca.y - setengahKaca),
            end = Offset(pusatKaca.x, pusatKaca.y + setengahKaca),
            strokeWidth = 1.dp.toPx(),
        )

        // Pena tipis yang menghubungkan titik aktif ke kaca.
        drawLine(
            color = Color(0xFF3DDC97).copy(alpha = 0.35f),
            start = fokus,
            end = pusatKaca,
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
        )
    }
}
