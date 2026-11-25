package com.zacky.docar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.R2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup

/** Tombol ikon 48dp tanpa kotak; hanya lingkaran penanda saat aktif. */
@Composable
fun IkonKotak(
    ikon: Ikon,
    label: String,
    aktif: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (aktif) Aksen else Color.Transparent)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IkonGambar(
            ikon = ikon,
            warna = if (aktif) Latar else Teks.copy(alpha = 0.9f),
            ukuran = 20.dp,
        )
    }
}

/** Aksi sekunder: teks saja, tanpa kotak. */
@Composable
fun TeksAksi(teks: String, aktifkan: Boolean = true, onClick: () -> Unit) {
    Text(
        text = teks,
        style = MaterialTheme.typography.labelMedium,
        color = if (aktifkan) Aksen else TeksRedup.copy(alpha = 0.4f),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = aktifkan, onClick = onClick)
            .padding(vertical = S3),
    )
}

/** Satu aksi utama per layar. */
@Composable
fun TombolPrimer(
    teks: String,
    aktif: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(R2))
            .background(if (aktif) Aksen else Permukaan)
            .clickable(enabled = aktif, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = teks,
            style = MaterialTheme.typography.titleMedium,
            color = if (aktif) Latar else TeksRedup,
        )
    }
}

/** Pilihan tunggal dalam satu baris: yang aktif terisi, sisanya permukaan. */
@Composable
fun PilihanSegmen(
    teks: String,
    aktif: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tinggi: Dp = 44.dp,
) {
    Box(
        modifier = modifier
            .heightIn(min = tinggi)
            .clip(RoundedCornerShape(R2))
            .background(if (aktif) Aksen else Permukaan)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = teks,
            style = MaterialTheme.typography.labelMedium,
            color = if (aktif) Latar else Teks,
        )
    }
}

/** Baris pengaturan: label di kiri, nilai di kanan. */
@Composable
fun BarisPengaturan(
    label: String,
    nilai: String,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Teks)
        Text(nilai, style = MaterialTheme.typography.bodyMedium, color = TeksRedup)
    }
}
