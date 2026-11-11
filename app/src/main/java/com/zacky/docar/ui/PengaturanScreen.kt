package com.zacky.docar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zacky.docar.ocr.BahasaOcr
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Garis
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Permukaan
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PengaturanScreen(
    sudutMaks: Float,
    modeSenter: Int,
    bahasaOcr: Int,
    onSudut: (Float) -> Unit,
    onSenter: (Int) -> Unit,
    onBahasa: (Int) -> Unit,
    onSiapkanModel: () -> Unit,
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
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Ambang sudut", style = MaterialTheme.typography.titleMedium, color = Teks)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Auto-capture hanya jalan kalau kemiringan HP di bawah nilai ini.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            NumText(
                nilai = sudutMaks.roundToInt().toString(),
                style = MaterialTheme.typography.headlineMedium,
                warna = Aksen,
            )
            Text(
                text = "°",
                style = MaterialTheme.typography.titleMedium,
                color = TeksRedup,
            )
        }

        Slider(
            value = sudutMaks,
            onValueChange = { onSudut(it.roundToInt().toFloat()) },
            valueRange = 1f..8f,
            steps = 6,
            colors = SliderDefaults.colors(
                thumbColor = Aksen,
                activeTrackColor = Aksen,
                inactiveTrackColor = Garis,
            ),
        )

        Spacer(Modifier.height(24.dp))

        Text("Mode senter", style = MaterialTheme.typography.titleMedium, color = Teks)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Otomatis menyalakan senter saat ruangan redup.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pilihan(
                teks = "Otomatis",
                aktif = modeSenter == 0,
                onClick = { onSenter(0) },
                modifier = Modifier.weight(1f),
            )
            Pilihan(
                teks = "Nyala",
                aktif = modeSenter == 1,
                onClick = { onSenter(1) },
                modifier = Modifier.weight(1f),
            )
            Pilihan(
                teks = "Mati",
                aktif = modeSenter == 2,
                onClick = { onSenter(2) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))

        Text("Bahasa OCR", style = MaterialTheme.typography.titleMedium, color = Teks)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Model tambahan diunduh lewat Play Services sekali, setelah itu offline. Bahasa Indonesia memakai model Latin.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(10.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            BahasaOcr.entries.forEachIndexed { i, bahasa ->
                Pilihan(
                    teks = bahasa.label,
                    aktif = bahasaOcr == i,
                    onClick = { onBahasa(i) },
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Pilihan(
            teks = "Siapkan model",
            aktif = false,
            onClick = onSiapkanModel,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Pilihan(
    teks: String,
    aktif: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (aktif) Aksen else Permukaan)
            .border(1.dp, if (aktif) Color.Transparent else Garis, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = teks,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 14.sp,
                fontWeight = if (aktif) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = if (aktif) Latar else Teks,
        )
    }
}
