package com.zacky.docar.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zacky.docar.data.Dokumen
import com.zacky.docar.data.Halaman
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.Peringatan
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
import com.zacky.docar.ui.theme.R1
import com.zacky.docar.ui.theme.R2
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4

@Composable
fun DokumenScreen(
    dokumen: Dokumen,
    halaman: List<Halaman>,
    sorot: String,
    onKembali: () -> Unit,
    onTambahHalaman: () -> Unit,
    onHapusHalaman: (Halaman) -> Unit,
    onNaik: (Halaman) -> Unit,
    onTurun: (Halaman) -> Unit,
    onBukaPdf: () -> Unit,
    onEksporPdf: () -> Unit,
    onEksporTxt: () -> Unit,
) {
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

        Spacer(Modifier.height(S3))

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
                        sorot = sorot,
                        bisaNaik = i > 0,
                        bisaTurun = i < halaman.lastIndex,
                        onNaik = { onNaik(h) },
                        onTurun = { onTurun(h) },
                        onHapus = { onHapusHalaman(h) },
                    )
                }
            }
        }

        Spacer(Modifier.height(S3))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S3),
        ) {
            TeksAksi("Buka PDF", aktifkan = halaman.isNotEmpty(), onClick = onBukaPdf)
            TeksAksi("Ekspor PDF", aktifkan = halaman.isNotEmpty(), onClick = onEksporPdf)
            TeksAksi("Ekspor TXT", aktifkan = halaman.isNotEmpty(), onClick = onEksporTxt)
        }

        Spacer(Modifier.height(S2))

        TombolPrimer(
            teks = "Tambah halaman",
            modifier = Modifier.fillMaxWidth(),
            onClick = onTambahHalaman,
        )

        Spacer(Modifier.height(S3))
    }
}

@Composable
private fun BarisHalaman(
    nomor: Int,
    halaman: Halaman,
    sorot: String,
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
            .clip(RoundedCornerShape(R2))
            .background(Permukaan)
            .padding(S3),
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
                .clip(RoundedCornerShape(R1))
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
                text = sorotBaris(halaman.teks, sorot),
                style = MaterialTheme.typography.bodySmall,
                color = Teks,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IkonBaris(Ikon.Naik, "Naikkan", bisaNaik, onNaik)
        IkonBaris(Ikon.Turun, "Turunkan", bisaTurun, onTurun)
        IkonBaris(Ikon.Hapus, "Hapus", true, onHapus)
    }
}

@Composable
private fun IkonBaris(
    ikon: Ikon,
    label: String,
    aktif: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = aktif, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IkonGambar(
            ikon = ikon,
            warna = if (aktif) Teks.copy(alpha = 0.85f) else TeksRedup.copy(alpha = 0.3f),
            ukuran = 18.dp,
        )
    }
}

/**
 * Baris pertama yang memuat kueri (atau baris pertama yang tidak kosong), dengan
 * kata kueri ditandai.
 */
private fun sorotBaris(teks: String, kata: String): AnnotatedString {
    val kueri = kata.trim()
    val baris = teks.lineSequence()
        .firstOrNull { kueri.isNotEmpty() && it.contains(kueri, ignoreCase = true) }
        ?: teks.lineSequence().firstOrNull { it.isNotBlank() }
        ?: return AnnotatedString("Tanpa teks")

    if (kueri.isEmpty()) return AnnotatedString(baris)

    return buildAnnotatedString {
        append(baris)
        var i = baris.indexOf(kueri, ignoreCase = true)
        while (i >= 0) {
            addStyle(
                SpanStyle(background = Peringatan.copy(alpha = 0.35f), color = Teks),
                i,
                i + kueri.length,
            )
            i = baris.indexOf(kueri, i + kueri.length, ignoreCase = true)
        }
    }
}
