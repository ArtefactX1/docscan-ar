package com.buuzz.docscanar.ui

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buuzz.docscanar.gambar.ModeWarna
import com.buuzz.docscanar.gambar.Pemindai
import com.buuzz.docscanar.gambar.Titik
import com.buuzz.docscanar.ui.theme.Garis
import com.buuzz.docscanar.ui.theme.Latar
import com.buuzz.docscanar.ui.theme.Peringatan
import com.buuzz.docscanar.ui.theme.Teks
import com.buuzz.docscanar.ui.theme.TeksRedup
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun ProsesScreen(
    file: File,
    onSelesai: (Bitmap, List<Titik>) -> Unit,
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
        val sudut = withContext(Dispatchers.Default) { Pemindai.cariTepi(asli) }
            ?: Pemindai.sudutCadangan(asli.width, asli.height)
        tahap = 2
        withContext(Dispatchers.Default) { Pemindai.rapikan(asli, sudut, ModeWarna.ABU) }
        tahap = 4
        onSelesai(asli, sudut)
    }

    val langkah = listOf("Memotong tepi", "Meluruskan perspektif", "Memperjelas teks", "Membaca teks")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Memproses", style = MaterialTheme.typography.headlineMedium, color = Teks)
        Spacer(Modifier.height(6.dp))
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
                .clip(RoundedCornerShape(12.dp))
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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

@Composable
fun HasilScreen(
    asli: Bitmap,
    sudutAwal: List<Titik>,
    onSimpan: (Bitmap) -> Unit,
    onScanUlang: () -> Unit,
) {
    var sudut by remember { mutableStateOf(sudutAwal) }
    var ubahSudut by remember { mutableStateOf(false) }
    var warna by remember { mutableStateOf(ModeWarna.ABU) }
    var hasil by remember { mutableStateOf<Bitmap?>(null) }
    var memproses by remember { mutableStateOf(false) }

    LaunchedEffect(sudut, warna) {
        memproses = true
        delay(120)
        hasil = withContext(Dispatchers.Default) { Pemindai.rapikan(asli, sudut, warna) }
        memproses = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Garis, RoundedCornerShape(14.dp))
                    .clickable(onClick = onScanUlang)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text("←", style = MaterialTheme.typography.bodyMedium, color = Teks)
            }
            Text(
                text = "Hasil",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (memproses) "Memproses…" else "1 halaman",
                style = MaterialTheme.typography.labelSmall,
                color = TeksRedup,
            )
        }

        Spacer(Modifier.height(12.dp))

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
                    .clip(RoundedCornerShape(10.dp)),
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
        }

        Spacer(Modifier.height(12.dp))

        if (ubahSudut) {
            Text(
                text = "Geser titik hijau ke sudut kertas. Tarik di mana saja untuk memindahkan titik terdekat.",
                style = MaterialTheme.typography.labelSmall,
                color = TeksRedup,
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PilihanWarna(
                teks = "Asli",
                aktif = warna == ModeWarna.ASLI,
                onClick = { warna = ModeWarna.ASLI },
                modifier = Modifier.weight(1f),
            )
            PilihanWarna(
                teks = "Abu-abu",
                aktif = warna == ModeWarna.ABU,
                onClick = { warna = ModeWarna.ABU },
                modifier = Modifier.weight(1f),
            )
            PilihanWarna(
                teks = if (ubahSudut) "Selesai" else "Ubah sudut",
                aktif = ubahSudut,
                onClick = { ubahSudut = !ubahSudut },
                modifier = Modifier.weight(1.3f),
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE7E9EC))
                    .clickable(enabled = !memproses) { onSimpan(hasil ?: asli) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Simpan",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Latar,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Garis, RoundedCornerShape(14.dp))
                    .clickable(onClick = onScanUlang),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Scan ulang",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    color = Teks,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PilihanWarna(
    teks: String,
    aktif: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (aktif) Color(0xFFE7E9EC) else Color(0xFF141416))
            .border(1.dp, if (aktif) Color.Transparent else Garis, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = teks,
            style = MaterialTheme.typography.labelMedium,
            color = if (aktif) Latar else Teks,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Editor sudut dengan kaca pembesar. Saat satu titik aktif, gambar di sekitarnya
 * diperbesar 3x di pojok berlawanan supaya pengguna bisa melihat tepi kertas
 * yang sedang ia pegang.
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
                        onSudutKini(sudutKini.toMutableList().also { it[i] = baru })
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

            // Sumber dalam piksel bitmap: selebar kaca dibagi pembesaran, lalu
            // dikonversi dari koordinat kanvas ke koordinat gambar.
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
