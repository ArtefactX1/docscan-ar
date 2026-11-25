package com.zacky.docar.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zacky.docar.ui.theme.Aksen
import com.zacky.docar.ui.theme.Angka
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.S1
import com.zacky.docar.ui.theme.S2
import com.zacky.docar.ui.theme.S3
import com.zacky.docar.ui.theme.S4
import com.zacky.docar.ui.theme.S5
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup

private data class Bagian(val judul: String, val isi: String)

private val bagian = listOf(
    Bagian(
        "Arahkan ke kertas",
        "Kotak muncul sendiri saat kertas menutup minimal 8% layar. Kalau belum muncul, dekatkan kamera atau tambah cahaya — jangan mengubah sudut dulu.",
    ),
    Bagian(
        "Diamkan sampai memotret",
        "Cincin di sekeliling tombol terisi selama posisi kertas stabil. Penuh setelah 0,7 detik, lalu foto diambil. Kalau bergeser, cincin mundur.",
    ),
    Bagian(
        "Atau ambil manual",
        "Ketuk tombol besar kapan pun. Auto-capture tidak perlu ditunggu, terutama untuk dokumen di bawah 15° kemiringan atau saat kertas sulit terdeteksi.",
    ),
    Bagian(
        "Perbaiki sudut",
        "Di layar hasil, ketuk ikon sudut lalu geser titik hijau ke pojok kertas. Kaca pembesar 3x membantu melihat tepi. Gunakan Auto-detect untuk mengulang deteksi, atau Seluruh foto untuk memakai bingkai penuh.",
    ),
    Bagian(
        "Pilih mode hasil",
        "Asli mempertahankan warna, Abu membuang bayangan dan menajamkan teks, Hitam membuat biner kontras tinggi, Linear meregangkan kontras foto redup.",
    ),
    Bagian(
        "Simpan dan atur halaman",
        "Simpan PDF menambahkan halaman ke dokumen. Satu dokumen bisa berisi banyak halaman: tambah, naikkan, turunkan, atau hapus lewat layar dokumen.",
    ),
    Bagian(
        "Cari dan ekspor",
        "Pencarian arsip membaca teks hasil OCR, bukan nama file. Kata yang dicari disorot di daftar halaman, dan ekspor PDF saat pencarian aktif ikut menyorot baris yang cocok. Ekspor TXT menghasilkan teks mentah.",
    ),
    Bagian(
        "Pengaturan",
        "Ambang sudut hanya mengubah warna peringatan; capture tetap jalan sampai 15°. Mode senter otomatis menyalakan lampu saat ruangan redup. Bahasa OCR Latin mencakup bahasa Indonesia.",
    ),
)

@Composable
fun PetunjukScreen(onKembali: () -> Unit) {
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
                text = "Petunjuk",
                style = MaterialTheme.typography.headlineMedium,
                color = Teks,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(S3))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(S5)) {
            items(bagian.size) { i ->
                val b = bagian[i]
                Row(horizontalArrangement = Arrangement.spacedBy(S3)) {
                    Text(
                        text = (i + 1).toString().padStart(2, '0'),
                        style = Angka,
                        color = Aksen,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            text = b.judul,
                            style = MaterialTheme.typography.titleMedium,
                            color = Teks,
                        )
                        Spacer(Modifier.height(S1))
                        Text(
                            text = b.isi,
                            style = MaterialTheme.typography.bodySmall,
                            color = TeksRedup,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(S5)) }
        }
    }
}
