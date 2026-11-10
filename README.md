# DocScan AR

Pemindai dokumen Android dengan panduan kerataan berbasis sensor dan koreksi perspektif otomatis. Semua pemrosesan berjalan di perangkat — tidak ada gambar yang dikirim ke server.

## Status

Fase 1 berjalan sebagian. Yang sudah ada:

- Preview kamera CameraX dengan overlay kerataan AR.
- Auto-capture saat sudut ≤2°, HP stabil 500 ms, dan cahaya cukup.
- Deteksi tepi kertas (OpenCV Canny + kontur) dan koreksi perspektif ke rasio A4.
- Editor sudut manual: geser 4 titik kalau deteksi otomatis meleset.
- Dua mode hasil: **Asli** (warna dipertahankan) dan **Abu-abu** (bayangan dibuang, siap OCR).
- OCR lokal dengan ML Kit, lalu simpan sebagai **PDF searchable** (teks tak terlihat di atas gambar).

Belum ada: arsip dokumen, pencarian teks, ekspor TXT.

## Kebutuhan

- Android 8.0 (API 26) ke atas, kamera belakang, akselerometer.
- JDK 17 atau lebih baru.
- Android SDK: `platforms;android-35`, `build-tools;35.0.0`.

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

APK debug keluar di `app/build/outputs/apk/debug/app-debug.apk`, rilis di `app/build/outputs/apk/release/app-release.apk`.

Rilis memakai R8 + resource shrinking dan ditandatangani kunci debug supaya bisa dipasang sendiri. Ganti `signingConfig` sebelum dibagikan.

Pasang ke perangkat:

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Kalau Android SDK belum ada, arahkan lewat `local.properties`:

```properties
sdk.dir=C\:\\Android\\sdk
```

## Ukuran

APK rilis ~26,7 MB. Komposisinya:

| Bagian | Ukuran |
|---|---|
| OpenCV (`libopencv_java4.so`) | 22,4 MB |
| dex setelah R8 | 4,4 MB |
| aset PdfBox (AFM, glyphlist) | 0,5 MB |

OpenCV adalah sisa terbesar. Menekannya butuh build OpenCV kustom dengan modul `core` + `imgproc` saja (perkiraan 6-8 MB), yang menuntut NDK dan source OpenCV.

OCR memakai ML Kit varian **unbundled**: model diunduh sekali lewat Play Services saat pemindaian pertama, setelah itu berjalan offline. Ini menghemat ~16 MB dibanding model yang dibundel.

## Uji

```bash
./gradlew testDebugUnitTest
```

## Struktur

```
app/src/main/java/com/buuzz/docscanar/
├── MainActivity.kt          navigasi kamera → proses → hasil
├── kamera/KameraPreview.kt  CameraX, auto-capture, torch
├── sensor/                  sudut, guncangan, kesiapan, pembacaan sensor
├── gambar/                  deteksi tepi, warp perspektif, perbaikan kontras
├── ocr/OcrEngine.kt         ML Kit text recognition
├── pdf/PdfBuat.kt           PDF A4 + text layer tak terlihat
└── ui/                      layar kamera, proses, hasil, izin
mockup/index.html            mockup HTML statis (referensi visual)
```

## Cara kerja

1. Sensor menentukan kesiapan: sudut ≤2°, tidak goyah, tidak berputar, lux ≥50.
2. Syarat harus bertahan 500 ms sebelum JPEG diambil otomatis.
3. Tepi kertas dicari dengan Canny + kontur segi empat terbesar (>20% frame).
4. Empat sudut diurutkan lalu di-warp ke 1240×1754.
5. Mode Abu-abu membagi gambar dengan versi blur-nya untuk membuang bayangan, lalu CLAHE.

## Lisensi

MIT — lihat [LICENSE](LICENSE).
