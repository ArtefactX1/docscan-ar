package com.buuzz.docscanar

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.buuzz.docscanar.gambar.Titik
import com.buuzz.docscanar.ocr.OcrEngine
import com.buuzz.docscanar.pdf.PdfBuat
import com.buuzz.docscanar.ui.HasilScreen
import com.buuzz.docscanar.ui.IzinKamera
import com.buuzz.docscanar.ui.KameraScreen
import com.buuzz.docscanar.ui.ProsesScreen
import com.buuzz.docscanar.ui.theme.DocScanTheme
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Layar {
    data object Kamera : Layar
    data class Proses(val file: File) : Layar
    data class Hasil(val asli: Bitmap, val sudut: List<Titik>) : Layar
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocScanTheme {
                var izinDiberikan by remember { mutableStateOf(false) }
                var layar by remember { mutableStateOf<Layar>(Layar.Kamera) }
                var menyimpan by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                if (!izinDiberikan) {
                    IzinKamera(onGranted = { izinDiberikan = true })
                } else {
                    when (val aktif = layar) {
                        is Layar.Kamera -> KameraScreen(onFoto = { layar = Layar.Proses(it) })

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
                                        withContext(Dispatchers.IO) { simpanPdf(this@MainActivity, bitmap) }
                                    }
                                    menyimpan = false
                                    hasil.onSuccess { file ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "PDF tersimpan: ${file.name}",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                        layar = Layar.Kamera
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
                    }
                }
            }
        }
    }
}

private suspend fun simpanPdf(context: Context, bitmap: Bitmap): File {
    val baris = OcrEngine.baca(bitmap)
    val folder = File(context.filesDir, "pdf").apply { mkdirs() }
    val file = File(folder, "scan_${System.currentTimeMillis()}.pdf")
    PdfBuat.buat(context, bitmap, baris, file)
    return file
}
