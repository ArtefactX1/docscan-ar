package com.zacky.docar.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.zacky.docar.ui.theme.Latar
import com.zacky.docar.ui.theme.Teks
import com.zacky.docar.ui.theme.TeksRedup

@Composable
fun IzinKamera(onGranted: () -> Unit) {
    val context = LocalContext.current
    var ditolak by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGranted() else ditolak = true
    }

    LaunchedEffect(Unit) {
        val sudah = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        if (sudah) onGranted() else launcher.launch(Manifest.permission.CAMERA)
    }

    if (!ditolak) {
        Box(Modifier.fillMaxSize().background(Latar))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Latar)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Izin kamera dibutuhkan",
            style = MaterialTheme.typography.headlineMedium,
            color = Teks,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "DocScan AR memakai kamera untuk memindai dokumen. Tidak ada gambar yang dikirim keluar perangkat.",
            style = MaterialTheme.typography.bodySmall,
            color = TeksRedup,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
            Text("Coba lagi")
        }
    }
}
