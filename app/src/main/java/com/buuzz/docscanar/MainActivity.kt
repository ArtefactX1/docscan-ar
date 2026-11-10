package com.buuzz.docscanar

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.buuzz.docscanar.gambar.Titik
import com.buuzz.docscanar.ui.HasilScreen
import com.buuzz.docscanar.ui.IzinKamera
import com.buuzz.docscanar.ui.KameraScreen
import com.buuzz.docscanar.ui.ProsesScreen
import com.buuzz.docscanar.ui.theme.DocScanTheme
import java.io.File
import java.io.FileOutputStream

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
                            onSimpan = { bitmap ->
                                val file = simpanJpeg(this, bitmap)
                                Toast.makeText(this, "Tersimpan: ${file.name}", Toast.LENGTH_SHORT).show()
                                layar = Layar.Kamera
                            },
                            onScanUlang = { layar = Layar.Kamera },
                        )
                    }
                }
            }
        }
    }
}

private fun simpanJpeg(context: Context, bitmap: Bitmap): File {
    val folder = File(context.filesDir, "scan").apply { mkdirs() }
    val file = File(folder, "scan_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
    return file
}
