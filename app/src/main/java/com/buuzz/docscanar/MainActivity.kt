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
import com.buuzz.docscanar.data.Halaman
import com.buuzz.docscanar.data.Repositori
import com.buuzz.docscanar.gambar.Titik
import com.buuzz.docscanar.ui.ArsipScreen
import com.buuzz.docscanar.ui.DokumenScreen
import com.buuzz.docscanar.ui.HasilScreen
import com.buuzz.docscanar.ui.IzinKamera
import com.buuzz.docscanar.ui.KameraScreen
import com.buuzz.docscanar.ui.ProsesScreen
import com.buuzz.docscanar.ui.theme.DocScanTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed interface Layar {
    data class Kamera(val target: Long?) : Layar
    data class Proses(val file: File, val target: Long?) : Layar
    data class Hasil(val asli: Bitmap, val sudut: List<Titik>, val target: Long?) : Layar
    data object Arsip : Layar
    data class Dokumen(val id: Long) : Layar
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocScanTheme {
                val repo = remember { Repositori(this, DokumenDb(this)) }
                var izinDiberikan by remember { mutableStateOf(false) }
                var layar by remember { mutableStateOf<Layar>(Layar.Kamera(null)) }
                var menyimpan by remember { mutableStateOf(false) }
                var daftar by remember { mutableStateOf<List<Dokumen>>(emptyList()) }
                var kueri by remember { mutableStateOf("") }
                var dokumen by remember { mutableStateOf<Dokumen?>(null) }
                var halaman by remember { mutableStateOf<List<Halaman>>(emptyList()) }
                val scope = rememberCoroutineScope()

                LaunchedEffect(layar, kueri) {
                    when (val aktif = layar) {
                        is Layar.Arsip -> daftar = withContext(Dispatchers.IO) { repo.cari(kueri) }
                        is Layar.Dokumen -> withContext(Dispatchers.IO) {
                            dokumen = repo.dokumenById(aktif.id)
                            halaman = repo.halaman(aktif.id)
                        }
                        else -> Unit
                    }
                }

                if (!izinDiberikan) {
                    IzinKamera(onGranted = { izinDiberikan = true })
                } else {
                    when (val aktif = layar) {
                        is Layar.Kamera -> KameraScreen(
                            onFoto = { layar = Layar.Proses(it, aktif.target) },
                            onArsip = { layar = Layar.Arsip },
                        )

                        is Layar.Proses -> ProsesScreen(
                            file = aktif.file,
                            onSelesai = { asli, sudut ->
                                layar = Layar.Hasil(asli, sudut, aktif.target)
                            },
                            onGagal = {
                                Toast.makeText(this, "Foto gagal dibaca", Toast.LENGTH_SHORT).show()
                                layar = Layar.Kamera(aktif.target)
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
                                            val id = aktif.target
                                                ?: repo.buatDokumen(namaBaru())
                                            repo.tambahHalaman(id, bitmap)
                                            bersihkanCache(this@MainActivity)
                                            id
                                        }
                                    }
                                    menyimpan = false
                                    hasil.onSuccess { id ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Halaman tersimpan",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        layar = Layar.Dokumen(id)
                                    }.onFailure {
                                        Log.e("DocScanAR", "gagal simpan halaman", it)
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
                            onScanUlang = { layar = Layar.Kamera(aktif.target) },
                        )

                        is Layar.Arsip -> ArsipScreen(
                            dokumen = daftar,
                            kueri = kueri,
                            onKueri = { kueri = it },
                            onBuka = { layar = Layar.Dokumen(it.id) },
                            onKembali = { layar = Layar.Kamera(null) },
                        )

                        is Layar.Dokumen -> {
                            val d = dokumen
                            if (d == null) {
                                LaunchedEffect(aktif.id) { layar = Layar.Arsip }
                            } else {
                                DokumenScreen(
                                    dokumen = d,
                                    halaman = halaman,
                                    onKembali = { layar = Layar.Arsip },
                                    onTambahHalaman = { layar = Layar.Kamera(d.id) },
                                    onHapusHalaman = { h ->
                                        scope.launch {
                                            withContext(Dispatchers.IO) { repo.hapusHalaman(h) }
                                            halaman = withContext(Dispatchers.IO) { repo.halaman(d.id) }
                                        }
                                    },
                                    onNaik = { h ->
                                        scope.launch {
                                            val baru = naik(halaman, h)
                                            withContext(Dispatchers.IO) { repo.urutkanUlang(d.id, baru) }
                                            halaman = withContext(Dispatchers.IO) { repo.halaman(d.id) }
                                        }
                                    },
                                    onTurun = { h ->
                                        scope.launch {
                                            val baru = turun(halaman, h)
                                            withContext(Dispatchers.IO) { repo.urutkanUlang(d.id, baru) }
                                            halaman = withContext(Dispatchers.IO) { repo.halaman(d.id) }
                                        }
                                    },
                                    onBukaPdf = {
                                        scope.launch {
                                            val file = withContext(Dispatchers.IO) { repo.pdfDari(d.id) }
                                            bukaPdf(this@MainActivity, file)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun namaBaru(): String =
    "Scan " + SimpleDateFormat("d MMM yyyy HH.mm", Locale("id", "ID")).format(Date())

private fun naik(daftar: List<Halaman>, halaman: Halaman): List<Long> {
    val ids = daftar.map { it.id }.toMutableList()
    val i = ids.indexOf(halaman.id)
    if (i > 0) {
        val tmp = ids[i - 1]
        ids[i - 1] = ids[i]
        ids[i] = tmp
    }
    return ids
}

private fun turun(daftar: List<Halaman>, halaman: Halaman): List<Long> {
    val ids = daftar.map { it.id }.toMutableList()
    val i = ids.indexOf(halaman.id)
    if (i >= 0 && i < ids.lastIndex) {
        val tmp = ids[i + 1]
        ids[i + 1] = ids[i]
        ids[i] = tmp
    }
    return ids
}

/** F4.3.3: JPEG mentah dari kamera tidak perlu disimpan setelah halaman jadi. */
private fun bersihkanCache(context: Context) {
    context.cacheDir.listFiles()
        ?.filter { it.name.startsWith("scan_") && it.name.endsWith(".jpg") }
        ?.forEach { it.delete() }
}

private fun bukaPdf(context: Context, berkas: File) {
    if (!berkas.exists()) {
        Toast.makeText(context, "PDF belum dibuat", Toast.LENGTH_SHORT).show()
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
