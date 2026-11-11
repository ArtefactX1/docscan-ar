# AGENTS.md

Panduan untuk agent coding yang bekerja di repo ini.

## Proyek

DocScan AR — pemindai dokumen Android native. Kamera → panduan kerataan sensor → auto-capture → koreksi perspektif → OCR → PDF searchable. Semua pemrosesan lokal, tidak ada backend.

## Perintah

```bash
./gradlew assembleDebug          # APK debug
./gradlew assembleRelease        # APK rilis (R8 + shrink)
./gradlew testDebugUnitTest      # unit test
./gradlew installDebug           # pasang ke perangkat
```

Android SDK di `C:\Android\sdk` (lihat `local.properties`). JDK 17+.

## Struktur

```
app/src/main/java/com/zacky/docar/
├── MainActivity.kt        navigasi: Kamera → Proses → Hasil
├── kamera/                CameraX preview, capture, analisis frame realtime
├── sensor/                sudut, guncangan, kesiapan, pembacaan sensor
├── gambar/                OpenCV: deteksi tepi, warp, perbaikan kontras
├── ocr/                   ML Kit text recognition
├── pdf/                   PdfBox: PDF A4 + text layer invisible
├── data/                  SQLite, repositori, preferensi pemindaian
└── ui/                    layar Compose
```

## Aturan kode

- **Kotlin + Compose saja.** Tidak ada XML layout, tidak ada fragment.
- **Logika murni dipisah dari Android** supaya bisa diuji JVM: lihat `sensor/SudutHitung.kt`, `sensor/Guncangan.kt`, `sensor/Kesiapan.kt`, `gambar/UrutSudut.kt`. Tambahkan unit test untuk setiap fungsi murni baru.
- **Pemrosesan berat di `Dispatchers.Default` atau `Dispatchers.IO`.** Jangan pernah di main thread.
- **Jangan tambah dependensi** kalau stdlib atau API Android sudah cukup. Setiap dependensi baru harus dijelaskan alasannya di commit message.
- **Komentar hanya untuk alasan**, bukan untuk mengulang kode. Bahasa Indonesia.

## Aturan UI

- Palet: latar `#0A0A0B`, permukaan `#141416`, garis `#2A2A2E`, teks `#F4F4F5`, teks redup `#8B8B90`, aksen `#E7E9EC`, siap `#3DDC97`, peringatan `#F5A524`. Token ada di `ui/theme/Theme.kt`.
- Font Poppins (400/500/600/700), dibundel di `res/font`. Jangan pakai di bawah 13sp.
- Angka yang berubah real-time wajib lewat `NumText` (slot lebar tetap) — Poppins tidak punya tabular figures.
- Tap target minimal 48dp. Hormati `WindowInsets.safeDrawing`.
- Jangan pakai emoji sebagai ikon. Ikon digambar dengan `Canvas` atau vektor.
- Status jangan hanya dibedakan warna; sertakan teks.

## Batasan yang sudah diputuskan

- Android native, bukan Flutter/React Native. Sensor ambient light dan overlay AR butuh native.
- Overlay "AR" memakai sensor fusion, bukan ARCore.
- `abiFilters` hanya `arm64-v8a`. Menambah ABI lain akan menggandakan ukuran native.
- OpenCV adalah **build kustom** (`core` + `imgproc`, statis). Kelas Java dari `app/libs/opencv-4.12.0.jar`, native dari `app/src/main/jniLibs/arm64-v8a/libopencv_java4.so`. Jangan kembalikan ke dependensi Maven.
- ML Kit memakai varian **unbundled** (model diunduh lewat Play Services sekali). Jangan ganti ke `text-recognition` bundled tanpa membahas ukuran APK.
- BouncyCastle dikecualikan dari PdfBox karena PDF yang dibuat tidak pernah terenkripsi.

## Alur kerja

1. Satu task per sesi. Baca file yang akan disentuh sebelum mengubah.
2. Diff sekecil mungkin. Jangan refactor di luar scope.
3. Verifikasi: `./gradlew testDebugUnitTest assembleDebug` harus lulus.
4. Perubahan yang butuh perangkat (kamera, sensor, OCR) dicatat sebagai daftar uji manual, bukan diklaim sudah jalan.
5. Commit message bahasa Indonesia, satu baris, menjelaskan alasannya.

## Jebakan yang sudah pernah kena

- `pointerInput` dengan state yang berubah sebagai key akan me-restart gesture di tengah drag. Pakai `rememberUpdatedState` dan kunci ke objek stabil.
- `PreviewView` harus `FIT_CENTER` agar pemetaan koordinat deteksi ke overlay akurat. `FILL_CENTER` memotong gambar dan membuat overlay meleset.
- `ImageAnalysis` harus `STRATEGY_KEEP_ONLY_LATEST` dan ditutup (`image.close()`) di `finally`.
- PdfBox butuh `PDFBoxResourceLoader.init(context)` sebelum dipakai.
- Jangan mengecualikan `LiberationSans-Regular.ttf` lewat `ignoreAssetsPatterns`. `PDType1Font.<clinit>` memuatnya lewat `FontMappers` sebelum Helvetica dipakai; tanpa aset itu simpan PDF gagal dengan `FileNotFoundException`. Hanya `cmap` yang boleh dikecualikan.
