package com.zacky.docar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zacky.docar.ocr.BahasaOcr
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Garis
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup
import com.zacky.docar.ui.theme.Angka
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.S5
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
        Spacer(Modifier.height(S3))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S3),
        ) {
            IkonKotak(Ikon.Kembali, "Kembali", onClick = onKembali)
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(S5))

        Text("Ambang sudut", style = MaterialTheme.typography.titleMedium, color = Teks)
        Spacer(Modifier.height(S1))
        Text(
            text = "Peringatan kalau kemiringan HP melewati nilai ini. Tidak menghalangi pemotretan — perspektif dikoreksi otomatis.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(S2))

        Text(
            text = "${sudutMaks.roundToInt()}°",
            style = Angka.copy(fontSize = 22.sp),
            color = Aksen,
        )

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

        Spacer(Modifier.height(S5))

        Text("Mode senter", style = MaterialTheme.typography.titleMedium, color = Teks)
        Spacer(Modifier.height(S1))
        Text(
            text = "Otomatis menyalakan senter saat ruangan redup.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(S3))

        Row(horizontalArrangement = Arrangement.spacedBy(S2)) {
            PilihanSegmen(
                teks = "Otomatis",
                aktif = modeSenter == 0,
                onClick = { onSenter(0) },
                modifier = Modifier.weight(1f),
            )
            PilihanSegmen(
                teks = "Nyala",
                aktif = modeSenter == 1,
                onClick = { onSenter(1) },
                modifier = Modifier.weight(1f),
            )
            PilihanSegmen(
                teks = "Mati",
                aktif = modeSenter == 2,
                onClick = { onSenter(2) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(S5))

        Text("Bahasa OCR", style = MaterialTheme.typography.titleMedium, color = Teks)
        Spacer(Modifier.height(S1))
        Text(
            text = "Model tambahan diunduh lewat Play Services sekali, setelah itu offline. Bahasa Indonesia memakai model Latin.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
        )
        Spacer(Modifier.height(S3))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(S2),
            verticalArrangement = Arrangement.spacedBy(S2),
        ) {
            BahasaOcr.entries.forEachIndexed { i, bahasa ->
                PilihanSegmen(
                    teks = bahasa.label,
                    aktif = bahasaOcr == i,
                    onClick = { onBahasa(i) },
                )
            }
        }

        Spacer(Modifier.height(S3))

        TeksAksi("Siapkan model", onClick = onSiapkanModel)

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(S4))
    }
}
