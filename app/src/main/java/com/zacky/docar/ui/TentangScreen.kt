package com.zacky.docar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Angka
import com.zacky.docar.ui.theme.Garis
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.R2
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.S5
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup

private const val REPO = "https://github.com/ArtefactX1/docscan-ar"

private val teknologi = listOf(
    "Kamera" to "CameraX",
    "Pengolahan citra" to "OpenCV 4.12 (core + imgproc)",
    "OCR" to "ML Kit text recognition",
    "PDF" to "PdfBox-Android",
    "Penyimpanan" to "SQLite + FTS4",
    "Antarmuka" to "Jetpack Compose",
)

@Composable
fun TentangScreen(versi: String, onKembali: () -> Unit) {
    val tautan = LocalUriHandler.current

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
            IkonKotak(Ikon.Kembali, "Kembali", onClick = onKembali)
            Text(
                text = "Tentang",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = S5),
        ) {
            Spacer(Modifier.height(S4))

            Text("DocScan AR", style = MaterialTheme.typography.headlineMedium, color = Teks)
            Spacer(Modifier.height(S1))
            Text(
                text = "Versi $versi",
                style = Angka,
                color = TeksRedup,
            )
            Spacer(Modifier.height(S3))
            Text(
                text = "Pemindai dokumen dengan panduan kerataan dan koreksi perspektif. Semua pemrosesan berjalan di perangkat — tidak ada gambar, teks, atau PDF yang dikirim ke server.",
                style = MaterialTheme.typography.bodySmall,
                color = TeksRedup,
            )

            Spacer(Modifier.height(S5))
            Text("Dibangun dengan", style = MaterialTheme.typography.titleMedium, color = Teks)
            Spacer(Modifier.height(S2))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(R2))
                    .background(Permukaan),
            ) {
                teknologi.forEachIndexed { i, (peran, nama) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = S4, vertical = S3),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = peran,
                            style = MaterialTheme.typography.bodySmall,
                            color = TeksRedup,
                        )
                        Text(
                            text = nama,
                            style = MaterialTheme.typography.bodySmall,
                            color = Teks,
                        )
                    }
                    if (i != teknologi.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = Garis)
                    }
                }
            }

            Spacer(Modifier.height(S5))
            Text("Privasi", style = MaterialTheme.typography.titleMedium, color = Teks)
            Spacer(Modifier.height(S1))
            Text(
                text = "Izin yang diminta hanya kamera. Model OCR diunduh sekali lewat Play Services, setelah itu pengenalan teks berjalan offline. Tidak ada analitik dan tidak ada iklan.",
                style = MaterialTheme.typography.bodySmall,
                color = TeksRedup,
            )

            Spacer(Modifier.height(S5))
            Text("Lisensi", style = MaterialTheme.typography.titleMedium, color = Teks)
            Spacer(Modifier.height(S1))
            Text(
                text = "MIT. OpenCV, ML Kit, dan PdfBox memakai lisensi masing-masing.",
                style = MaterialTheme.typography.bodySmall,
                color = TeksRedup,
            )

            Spacer(Modifier.height(S5))
            Text(
                text = "Kode sumber",
                style = MaterialTheme.typography.titleMedium,
                color = Aksen,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { runCatching { tautan.openUri(REPO) } }
                    .padding(vertical = S2),
            )
            Text(
                text = REPO,
                style = MaterialTheme.typography.labelSmall,
                color = TeksRedup,
            )
        }
    }
}
