package com.buuzz.docscanar.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buuzz.docscanar.data.Dokumen
import com.buuzz.docscanar.data.Halaman
import com.buuzz.docscanar.ui.theme.Garis
import com.buuzz.docscanar.ui.theme.Latar
import com.buuzz.docscanar.ui.theme.Permukaan
import com.buuzz.docscanar.ui.theme.Teks
import com.buuzz.docscanar.ui.theme.TeksRedup

@Composable
fun DokumenScreen(
    dokumen: Dokumen,
    halaman: List<Halaman>,
    onKembali: () -> Unit,
    onTambahHalaman: () -> Unit,
    onHapusHalaman: (Halaman) -> Unit,
    onNaik: (Halaman) -> Unit,
    onTurun: (Halaman) -> Unit,
    onBukaPdf: () -> Unit,
) {
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
            TombolKecil("←", onKembali)
            Column(Modifier.weight(1f)) {
                Text(
                    text = dokumen.nama,
                    style = MaterialTheme.typography.titleMedium,
                    color = Teks,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${halaman.size} halaman",
                    style = MaterialTheme.typography.labelSmall,
                    color = TeksRedup,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        if (halaman.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Belum ada halaman",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeksRedup,
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f),
            ) {
                itemsIndexed(halaman, key = { _, h -> h.id }) { i, h ->
                    BarisHalaman(
                        nomor = i + 1,
                        halaman = h,
                        bisaNaik = i > 0,
                        bisaTurun = i < halaman.lastIndex,
                        onNaik = { onNaik(h) },
                        onTurun = { onTurun(h) },
                        onHapus = { onHapusHalaman(h) },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE7E9EC))
                    .clickable(onClick = onTambahHalaman),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tambah halaman",
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
                    .clickable(enabled = halaman.isNotEmpty(), onClick = onBukaPdf),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Buka PDF",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    color = if (halaman.isEmpty()) TeksRedup else Teks,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BarisHalaman(
    nomor: Int,
    halaman: Halaman,
    bisaNaik: Boolean,
    bisaTurun: Boolean,
    onNaik: () -> Unit,
    onTurun: () -> Unit,
    onHapus: () -> Unit,
) {
    val gambar = remember(halaman.thumb) {
        BitmapFactory.decodeFile(halaman.thumb)?.asImageBitmap()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Permukaan)
            .border(1.dp, Garis, RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "$nomor",
            style = MaterialTheme.typography.labelMedium,
            color = TeksRedup,
            modifier = Modifier.size(width = 18.dp, height = 18.dp),
        )

        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 58.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF4F1EA)),
        ) {
            if (gambar != null) {
                Image(
                    bitmap = gambar,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = halaman.teks.lineSequence().firstOrNull { it.isNotBlank() } ?: "Tanpa teks",
                style = MaterialTheme.typography.bodySmall,
                color = Teks,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        TombolKecil("↑", onNaik, aktif = bisaNaik)
        TombolKecil("↓", onTurun, aktif = bisaTurun)
        TombolKecil("✕", onHapus)
    }
}

@Composable
private fun TombolKecil(teks: String, onClick: () -> Unit, aktif: Boolean = true) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Garis, RoundedCornerShape(12.dp))
            .clickable(enabled = aktif, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = teks,
            style = MaterialTheme.typography.bodyMedium,
            color = if (aktif) Teks else TeksRedup.copy(alpha = 0.4f),
        )
    }
}
