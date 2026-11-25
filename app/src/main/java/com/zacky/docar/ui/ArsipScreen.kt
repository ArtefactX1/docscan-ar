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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zacky.docar.data.Dokumen
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
import com.zacky.docar.ui.theme.R1
import com.zacky.docar.ui.theme.R2
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.S5
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArsipScreen(
    dokumen: List<Dokumen>,
    kueri: String,
    onKueri: (String) -> Unit,
    onBuka: (Dokumen) -> Unit,
    onKembali: () -> Unit,
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
            Text(
                text = "Arsip",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${dokumen.size} dokumen",
                style = MaterialTheme.typography.labelSmall,
                color = TeksRedup,
            )
        }

        Spacer(Modifier.height(S3))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(R2))
                .background(Permukaan)
                .padding(horizontal = S3, vertical = S3),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (kueri.isEmpty()) {
                Text(
                    text = "Cari isi dokumen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeksRedup,
                )
            }
            BasicTextField(
                value = kueri,
                onValueChange = onKueri,
                singleLine = true,
                textStyle = TextStyle(
                    color = Teks,
                    fontSize = 14.sp,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(S3))

        if (dokumen.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (kueri.isEmpty()) "Belum ada dokumen" else "Tidak ada yang cocok",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TeksRedup,
                )
            }
            return@Column
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(S2),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(dokumen, key = { it.id }) { d ->
                BarisDokumen(d, onBuka)
            }
            item { Spacer(Modifier.height(S5)) }
        }
    }
}

@Composable
private fun BarisDokumen(dokumen: Dokumen, onBuka: (Dokumen) -> Unit) {
    val gambar = remember(dokumen.thumb) {
        dokumen.thumb?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(R2))
            .background(Permukaan)
            .clickable { onBuka(dokumen) }
            .padding(S3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 58.dp)
                .clip(RoundedCornerShape(R1))
                .background(Color(0xFFF4F1EA)),
            contentAlignment = Alignment.Center,
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
                text = dokumen.nama,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = Teks,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(S1))
            Text(
                text = "${dokumen.jumlahHalaman} halaman · ${tanggal(dokumen.dibuat)}",
                style = MaterialTheme.typography.labelSmall,
                color = TeksRedup,
            )
        }
    }
}

private fun tanggal(milidetik: Long): String =
    SimpleDateFormat("d MMM yyyy · HH:mm", Locale("id", "ID")).format(Date(milidetik))
