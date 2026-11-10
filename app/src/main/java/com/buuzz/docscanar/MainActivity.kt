package com.buuzz.docscanar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.buuzz.docscanar.data.Dokumen
import com.buuzz.docscanar.data.DokumenDb
import com.buuzz.docscanar.gambar.Titik
import com.buuzz.docscanar.ocr.OcrEngine
import com.buuzz.docscanar.pdf.PdfBuat
import com.buuzz.docscanar.ui.ArsipScreen
import com.buuzz.docscanar.ui.HasilScreen
import com.buuzz.docscanar.ui.IzinKamera
import com.buuzz.docscanar.ui.KameraScreen
import com.buuzz.docscanar.ui.ProsesScreen
import com.buuzz.docscanar.ui.theme.DocScanTheme
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Layar {
    data object Kamera : Layar
    data class Proses(val file: File) : Layar
    data class Hasil(val asli: Bitmap, val sudut: List<Titik>) : Layar
    data object Arsip : Layar
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocScanTheme {
                val db = remember { DokumenDb(this) }
                var izinDiberikan by remember { mutableStateOf(false) }
                var layar by remember { mutableStateOf<Layar>(Layar.Kamera) }
                var menyimpan by remember { mutableStateOf(false) }
                var dokumen by remember { mutableStateOf<List<Dokumen>>(emptyList()) }
                var kueri by remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()

                LaunchedEffect(layar, kueri) {
                    if (layar is Layar.Arsip) {
                        dokumen = withContext(Dispatchers.IO) { db.cari(kueri) }
                    }
                }

                if (!izinDiberikan) {
                    IzinKamera(onGranted = { izinDiberikan = true })
                } else {
                    when (val aktif = layar) {
                        is Layar.Kamera -> KameraScreen(
                            onFoto = { layar = Layar.Proses(it) },
                            onArsip = { layar = Layar.Arsip },
                        )

                        is Layar.Proses -> ProsesScreen(
                            file = aktif.file,
                            onSelesai = { asli, sudut -> layar = Layar.Hasil(asli, sudut) },
                            onGagal = {
                                Toast.makeText(this, "Foto gagal dibaca", Toast.LENGTH_SHORT).show()
                                layar = Layar.Kamera
                            },
                        )

                        is Layar.Hasil -> HasilScreen(
                            asli = aktif.asli,
                            sudutAwal = aktif.sudut,
                            menyimpan = menyimpan,
                            onSimpan = { bitmap ->
                                menyimpan = true
                                scope.launch {
                                    val hasil = runCatching {
                                        withContext(Dispatchers.IO) {
                                            val simpan = simpanPdf(this@MainActivity, bitmap)
                                            db.tambah(
                                                nama = simpan.file.name,
                                                path = simpan.file.absolutePath,
                                                thumb = simpan.thumb.absolutePath,
                                                teks = simpan.teks,
                                            )
                                            bersihkanCache(this@MainActivity)
                                            simpan
                                        }
                                    }
                                    menyimpan = false
                                    hasil.onSuccess { simpan ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "PDF tersimpan: ${simpan.file.name}",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                        layar = Layar.Arsip
                                    }.onFailure {
                                        Log.e("DocScanAR", "gagal simpan PDF", it)
                                        runCatching {
                                            File(filesDir, "last-error.txt")
                                                .writeText(it.stackTraceToString())
                                        }
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Gagal menyimpan: ${it::class.java.simpleName}: ${it.message}",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                    }
                                }
                            },
                            onScanUlang = { layar = Layar.Kamera },
                        )

                        is Layar.Arsip -> ArsipScreen(
                            dokumen = dokumen,
                            kueri = kueri,
                            onKueri = { kueri = it },
                            onBuka = { bukaPdf(this, it) },
                            onKembali = { layar = Layar.Kamera },
                        )
                    }
                }
            }
        }
    }
}

private class HasilSimpan(val file: File, val thumb: File, val teks: String)

private suspend fun simpanPdf(context: Context, bitmap: Bitmap): HasilSimpan {
    val baris = OcrEngine.baca(bitmap)
    val folder = File(context.filesDir, "pdf").apply { mkdirs() }
    val nama = "scan_${System.currentTimeMillis()}"

    val file = File(folder, "$nama.pdf")
    PdfBuat.buat(context, bitmap, baris, file)

    val kecil = Bitmap.createScaledBitmap(
        bitmap,
        240,
        (240f * bitmap.height / bitmap.width).toInt().coerceAtLeast(1),
        true,
    )
    val thumb = File(folder, "$nama.jpg")
    FileOutputStream(thumb).use { kecil.compress(Bitmap.CompressFormat.JPEG, 80, it) }

    return HasilSimpan(file, thumb, baris.joinToString("\n") { it.teks })
}

/** F4.3.3: JPEG mentah dari kamera tidak perlu disimpan setelah PDF jadi. */
private fun bersihkanCache(context: Context) {
    context.cacheDir.listFiles()
        ?.filter { it.name.startsWith("scan_") && it.name.endsWith(".jpg") }
        ?.forEach { it.delete() }
}

private fun bukaPdf(context: Context, dokumen: Dokumen) {
    val berkas = File(dokumen.path)
    if (!berkas.exists()) {
        Toast.makeText(context, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
        return
    }
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        berkas,
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "Tidak ada aplikasi pembaca PDF", Toast.LENGTH_SHORT).show()
        }
}
