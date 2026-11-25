package com.zacky.docar.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Angka
import com.zacky.docar.ui.theme.Garis
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.PermukaanNaik
import com.zacky.docar.ui.theme.R2
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.S5
import com.zacky.docar.ui.theme.S6
import com.zacky.docar.ui.theme.Siap
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup

@Composable
fun MenuScreen(
    jumlahDokumen: Int,
    versi: String,
    onPindai: () -> Unit,
    onArsip: () -> Unit,
    onPetunjuk: () -> Unit,
    onTentang: () -> Unit,
    onSetelan: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = S4),
    ) {
        Spacer(Modifier.height(S6))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S3),
        ) {
            LogoMark()
            Column {
                Text(
                    text = "DocScan AR",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Teks,
                )
                Text(
                    text = "Pemindai dokumen · semua lokal",
                    style = MaterialTheme.typography.labelSmall,
                    color = TeksRedup,
                )
            }
        }

        Spacer(Modifier.height(S6))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .clip(RoundedCornerShape(R2))
                .background(PermukaanNaik)
                .clickable(onClickLabel = "Pindai dokumen", onClick = onPindai)
                .padding(horizontal = S4),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(S3),
            ) {
                IkonGambar(Ikon.Kamera, Aksen, ukuran = 22.dp)
                Text(
                    text = "Pindai dokumen",
                    style = MaterialTheme.typography.titleMedium,
                    color = Teks,
                    modifier = Modifier.weight(1f),
                )
                IkonGambar(Ikon.Panah, TeksRedup, ukuran = 18.dp)
            }
        }

        Spacer(Modifier.height(S5))

        DaftarBaris(
            baris = listOf(
                BarisData(Ikon.Arsip, "Arsip", if (jumlahDokumen > 0) "$jumlahDokumen" else null, onArsip),
                BarisData(Ikon.Petunjuk, "Petunjuk", null, onPetunjuk),
                BarisData(Ikon.Info, "Tentang", versi, onTentang),
                BarisData(Ikon.Setelan, "Pengaturan", null, onSetelan),
            ),
        )

        Spacer(Modifier.weight(1f))

        Text(
            text = "Tidak ada gambar yang dikirim ke server",
            style = MaterialTheme.typography.labelSmall,
            color = TeksRedup.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = S5),
        )
    }
}

private data class BarisData(
    val ikon: Ikon,
    val label: String,
    val nilai: String?,
    val onClick: () -> Unit,
)

@Composable
private fun DaftarBaris(baris: List<BarisData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(R2))
            .background(Permukaan),
    ) {
        baris.forEachIndexed { i, b ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClickLabel = b.label, onClick = b.onClick)
                    .padding(horizontal = S4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(S3),
            ) {
                IkonGambar(b.ikon, TeksRedup, ukuran = 20.dp)
                Text(
                    text = b.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Teks,
                    modifier = Modifier.weight(1f),
                )
                if (b.nilai != null) {
                    Text(text = b.nilai, style = Angka, color = TeksRedup)
                }
                IkonGambar(Ikon.Panah, TeksRedup.copy(alpha = 0.6f), ukuran = 16.dp)
            }
            if (i != baris.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = S4 + 20.dp + S3),
                    thickness = 1.dp,
                    color = Garis,
                )
            }
        }
    }
}

/** Tanda aplikasi: halaman miring dengan sapuan pindai, sama seperti ikon launcher. */
@Composable
private fun LogoMark() {
    Canvas(Modifier.size(44.dp)) {
        val s = size.minDimension
        val jalur = Path().apply {
            moveTo(s * 0.3f, s * 0.16f)
            lineTo(s * 0.76f, s * 0.1f)
            lineTo(s * 0.8f, s * 0.76f)
            lineTo(s * 0.26f, s * 0.82f)
            close()
        }
        drawPath(jalur, Color(0xFFEDEFF2))

        drawLine(
            color = Color(0xFF0A0A0B).copy(alpha = 0.25f),
            start = Offset(s * 0.37f, s * 0.33f),
            end = Offset(s * 0.68f, s * 0.3f),
            strokeWidth = s * 0.06f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Color(0xFF0A0A0B).copy(alpha = 0.25f),
            start = Offset(s * 0.37f, s * 0.46f),
            end = Offset(s * 0.62f, s * 0.44f),
            strokeWidth = s * 0.06f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Siap,
            start = Offset(s * 0.2f, s * 0.63f),
            end = Offset(s * 0.86f, s * 0.57f),
            strokeWidth = s * 0.075f,
            cap = StrokeCap.Round,
        )
        // Bingkai tipis supaya logo tidak menyatu dengan latar gelap.
        drawRoundRect(
            color = Color.White.copy(alpha = 0.06f),
            topLeft = Offset(0f, 0f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.22f),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
