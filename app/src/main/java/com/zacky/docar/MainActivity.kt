package com.zacky.docar

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.zacky.docar.data.Dokumen
import com.zacky.docar.data.DokumenDb
import com.zacky.docar.data.Halaman
import com.zacky.docar.data.Pengaturan
import com.zacky.docar.data.Repositori
import com.zacky.docar.gambar.Titik
import com.zacky.docar.ocr.BahasaOcr
import com.zacky.docar.ocr.OcrEngine
import com.zacky.docar.ui.ArsipScreen
import com.zacky.docar.ui.DokumenScreen
import com.zacky.docar.ui.HasilScreen
import com.zacky.docar.ui.IzinKamera
import com.zacky.docar.ui.KameraScreen
import com.zacky.docar.ui.PengaturanScreen
import com.zacky.docar.ui.ProsesScreen
import com.zacky.docar.ui.theme.DocScanTheme
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
    data object Setelan : Layar
    data class Dokumen(val id: Long) : Layar
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocScanTheme {
                val repo = remember { Repositori(this, DokumenDb(this)) }
                val pengaturan = remember { Pengaturan(this) }
                var izinDiberikan by remember { mutableStateOf(false) }
                var layar by remember { mutableStateOf<Layar>(Layar.Kamera(null)) }
                var menyimpan by remember { mutableStateOf(false) }
                var daftar by remember { mutableStateOf<List<Dokumen>>(emptyList()) }
                var kueri by remember { mutableStateOf("") }
                var dokumen by remember { mutableStateOf<Dokumen?>(null) }
                var halaman by remember { mutableStateOf<List<Halaman>>(emptyList()) }
                var galat by remember { mutableStateOf<String?>(null) }
                val scope = rememberCoroutineScope()
                var eksporId by remember { mutableStateOf<Long?>(null) }

                val simpanPdf = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("application/pdf")
                ) { uri ->
                    val id = eksporId
                    if (uri != null && id != null) {
                        scope.launch {
                            val ok = withContext(Dispatchers.IO) {
                                runCatching {
                                    val file = if (kueri.isBlank()) {
                                        repo.pdfDari(id).takeIf { it.exists() }
                                            ?: repo.bangunPdf(id)
                                    } else {
                                        repo.bangunPdf(id, kueri)
                                    } ?: return@runCatching false
                                    tulis(this@MainActivity, uri) { out ->
                                        file.inputStream().use { it.copyTo(out) }
                                    }
                                }.getOrDefault(false)
                            }
                            Toast.makeText(
                                this@MainActivity,
                                if (ok) "PDF diekspor" else "Ekspor PDF gagal",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }

                val simpanTxt = rememberLauncherForActivityResult(
                    ActivityResultContracts.CreateDocument("text/plain")
                ) { uri ->
                    val id = eksporId
                    if (uri != null && id != null) {
                        scope.launch {
                            val ok = withContext(Dispatchers.IO) {
                                runCatching {
                                    val teks = repo.halaman(id)
                                        .joinToString("\n\n") { it.teks }
                                    tulis(this@MainActivity, uri) { out ->
                                        out.write(teks.toByteArray())
                                    }
                                }.getOrDefault(false)
                            }
                            Toast.makeText(
                                this@MainActivity,
                                if (ok) "TXT diekspor" else "Ekspor TXT gagal",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                }

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
                            sudutMaks = pengaturan.sudutMaks,
                            modeSenter = pengaturan.modeSenter,
                            onModeSenter = pengaturan::ubahSenter,
                            onFoto = { layar = Layar.Proses(it, aktif.target) },
                            onArsip = { layar = Layar.Arsip },
                            onSetelan = { layar = Layar.Setelan },
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
                                            repo.tambahHalaman(
                                                id,
                                                bitmap,
                                                BahasaOcr.dari(pengaturan.bahasaOcr),
                                            )
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
                                        val jejak = it.stackTraceToString()
                                        runCatching {
                                            File(getExternalFilesDir(null), "last-error.txt")
                                                .writeText(jejak)
                                        }
                                        galat = jejak
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

                        is Layar.Setelan -> PengaturanScreen(
                            sudutMaks = pengaturan.sudutMaks,
                            modeSenter = pengaturan.modeSenter,
                            bahasaOcr = pengaturan.bahasaOcr,
                            onSudut = pengaturan::ubahSudut,
                            onSenter = pengaturan::ubahSenter,
                            onBahasa = pengaturan::ubahBahasa,
                            onSiapkanModel = {
                                scope.launch {
                                    val bahasa = BahasaOcr.dari(pengaturan.bahasaOcr)
                                    val ok = withContext(Dispatchers.IO) {
                                        OcrEngine.siapkanModel(bahasa)
                                    }
                                    Toast.makeText(
                                        this@MainActivity,
                                        if (ok) "Model ${bahasa.label} siap"
                                        else "Model ${bahasa.label} gagal diunduh",
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            },
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
                                    sorot = kueri,
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
                                    onEksporPdf = {
                                        eksporId = d.id
                                        simpanPdf.launch(namaBerkas(d.nama) + ".pdf")
                                    },
                                    onEksporTxt = {
                                        eksporId = d.id
                                        simpanTxt.launch(namaBerkas(d.nama) + ".txt")
                                    },
                                )
                            }
                        }
                    }
                }

                galat?.let { teks ->
                    AlertDialog(
                        onDismissRequest = { galat = null },
                        title = { Text("Gagal menyimpan") },
                        text = {
                            Text(
                                text = teks.take(1500),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { galat = null }) { Text("Tutup") }
                        },
                    )
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

/** JPEG mentah dari kamera tidak perlu disimpan setelah halaman jadi. */
private fun bersihkanCache(context: Context) {
    context.cacheDir.listFiles()
        ?.filter { it.name.startsWith("scan_") && it.name.endsWith(".jpg") }
        ?.forEach { it.delete() }
}

/** Tulis ke URI SAF. False kalau stream tidak bisa dibuka. */
private fun tulis(context: Context, uri: Uri, isi: (java.io.OutputStream) -> Unit): Boolean =
    context.contentResolver.openOutputStream(uri)?.use { out ->
        isi(out)
        true
    } ?: false

private fun namaBerkas(nama: String): String =
    nama.replace(Regex("[^\\p{L}\\p{N} _.-]"), "_").trim().ifEmpty { "dokumen" }

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
