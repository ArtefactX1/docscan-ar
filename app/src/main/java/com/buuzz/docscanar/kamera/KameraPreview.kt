package com.buuzz.docscanar.kamera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.buuzz.docscanar.sensor.Kesiapan
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

class KameraController {
    internal var ambil: (() -> Unit)? = null

    fun capture() {
        ambil?.invoke()
    }
}

@Composable
fun rememberKameraController(): KameraController = remember { KameraController() }

@Composable
fun KameraPreview(
    controller: KameraController,
    siap: Boolean,
    lux: Float?,
    senter: Boolean?,
    onFoto: (File) -> Unit,
    onSegi: (SegiLayar?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FIT_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }
    val eksekutor = remember { Executors.newSingleThreadExecutor() }
    val utama = remember { ContextCompat.getMainExecutor(context) }
    val onSegiKini by rememberUpdatedState(onSegi)

    DisposableEffect(Unit) {
        imageAnalysis.setAnalyzer(eksekutor) { image ->
            AnalisDokumen { segi -> utama.execute { onSegiKini(segi) } }.analyze(image)
        }
        onDispose { eksekutor.shutdown() }
    }

    var kamera by remember { mutableStateOf<Camera?>(null) }
    var sedangAmbil by remember { mutableStateOf(false) }

    fun potret() {
        if (sedangAmbil) return
        sedangAmbil = true
        val file = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        val opsi = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture.takePicture(
            opsi,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    sedangAmbil = false
                    onFoto(file)
                }

                override fun onError(exception: ImageCaptureException) {
                    sedangAmbil = false
                }
            },
        )
    }

    DisposableEffect(lifecycleOwner) {
        val future = ProcessCameraProvider.getInstance(context)
        var provider: ProcessCameraProvider? = null

        future.addListener(
            {
                provider = future.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                provider?.unbindAll()
                kamera = provider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis,
                )
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            provider?.unbindAll()
            kamera = null
        }
    }

    // senter null = otomatis mengikuti sensor cahaya.
    val senterNyala = senter ?: (lux != null && lux < Kesiapan.LUX_MIN)

    LaunchedEffect(kamera, senterNyala) {
        runCatching {
            kamera?.cameraControl?.enableTorch(senterNyala)
        }
    }

    // Syarat harus bertahan 500 ms; LaunchedEffect batal sendiri kalau siap berubah jadi false.
    LaunchedEffect(siap) {
        if (siap) {
            delay(500)
            potret()
        }
    }

    SideEffect { controller.ambil = ::potret }

    AndroidView(factory = { previewView }, modifier = modifier)
}
