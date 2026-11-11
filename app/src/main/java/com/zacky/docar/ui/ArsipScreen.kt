package com.zacky.docar.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zacky.docar.data.Dokumen
import com.zacky.docar.ui.theme.Garis
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
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
                    .clickable(onClick = onKembali)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text("←", style = MaterialTheme.typography.bodyMedium, color = Teks)
            }
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

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Permukaan)
                .border(1.dp, Garis, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp),
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

        Spacer(Modifier.height(14.dp))

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
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(dokumen, key = { it.id }) { d ->
                BarisDokumen(d, onBuka)
            }
            item { Spacer(Modifier.height(20.dp)) }
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
            .clip(RoundedCornerShape(14.dp))
            .background(Permukaan)
            .border(1.dp, Garis, RoundedCornerShape(14.dp))
            .clickable { onBuka(dokumen) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 44.dp, height = 58.dp)
                .clip(RoundedCornerShape(4.dp))
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
            Spacer(Modifier.height(4.dp))
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
