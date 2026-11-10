# Task

Status per 11 Nov 2026. `[x]` selesai, `[~]` sebagian, `[ ]` belum.

## F0. Fondasi

- [x] **F0.1.1** Scaffold proyek Compose
- [x] **F0.1.2** Alur izin kamera
- [x] **F0.1.3** Dependensi kamera, sensor, OCR, PDF
  - Room dan DataStore belum ditambahkan; belum ada yang memakainya.

## F1. Panduan Kerataan AR

### F1.1 Tampilkan Indikator Kerataan 3D

- [x] **F1.1.1** Baca gyroscope dan accelerometer
- [x] **F1.1.2** Hitung sudut kemiringan
- [x] **F1.1.3** Overlay mengikuti rotasi
  - Berubah: overlay kini mengikuti segi empat hasil deteksi realtime, bukan grid tetap.

### F1.2 Ukur Sudut & Guncangan

- [x] **F1.2.1** Simpangan dari target
  - Diimplementasikan sebagai sudut total, bukan roll dan pitch terpisah.
- [x] **F1.2.2** Deteksi guncangan
- [x] **F1.2.3** Gabungkan jadi status siap

### F1.3 Nilai Cahaya & Bayangan

- [x] **F1.3.1** Ambient light sensor
- [ ] **F1.3.2** Kecerahan rata-rata dari frame kamera
- [ ] **F1.3.3** Deteksi bayangan kiri-kanan dari frame
- [x] **F1.3.4** HUD status

## F2. Auto-Capture Presisi

### F2.1 Syarat Auto-Capture

- [x] **F2.1.1** Ambang dan fungsi `siap`
- [x] **F2.1.2** Wajib bertahan 500 ms

### F2.2 Ambil Foto Otomatis

- [x] **F2.2.1** ImageCapture saat syarat lolos
- [~] **F2.2.2** Kunci capture dan animasi flash
  - Kunci dan teks "Terkunci" ada; animasi flash belum.
- [x] **F2.2.3** Torch otomatis saat redup
  - Ditambah tombol siklus auto/nyala/mati.

### F2.3 Ambil Manual

- [x] **F2.3.1** Tombol ambil manual
- [x] **F2.3.2** Scan ulang dari layar hasil

## F3. Perbaikan Gambar Otomatis

### F3.1 Deteksi Tepi Kertas

- [x] **F3.1.1** Praproses (grayscale, blur, Canny)
- [x] **F3.1.2** Kontur segi empat terbesar
- [x] **F3.1.3** Tolak kontur tidak wajar
- [x] **F3.1.4** Deteksi realtime pada frame kamera
  - Tambahan di luar rencana awal.

### F3.2 Koreksi Perspektif

- [x] **F3.2.1** Urutkan empat sudut
- [x] **F3.2.2** Warp ke rasio A4
- [x] **F3.2.3** Editor sudut manual + kaca pembesar
  - Tambahan di luar rencana awal.

### F3.3 Perjelas Teks

- [~] **F3.3.1** Adaptive threshold dan unsharp mask
  - Memakai CLAHE, bukan unsharp mask.
- [x] **F3.3.2** Hapus bayangan lewat estimasi background
- [x] **F3.3.3** Mode Asli dan Abu-abu
  - Tambahan di luar rencana awal.

## F4. OCR & PDF Searchable

### F4.1 Ekstrak Teks

- [x] **F4.1.1** ML Kit pada bitmap hasil perbaikan
- [x] **F4.1.2** Susun baris dari block
- [x] **F4.1.3** Petakan bounding box ke koordinat PDF

### F4.2 Susun PDF Searchable

- [x] **F4.2.1** Render halaman dari bitmap
- [x] **F4.2.2** Text layer invisible
- [ ] **F4.2.3** Isi metadata dokumen

### F4.3 Simpan Dokumen

- [x] **F4.3.1** Tulis PDF ke penyimpanan aplikasi
- [x] **F4.3.2** Catat metadata ke SQLite
  - Memakai `SQLiteOpenHelper` langsung, bukan Room: satu tabel, tanpa generator.
- [x] **F4.3.3** Bersihkan file sementara setelah simpan

## F5. Arsip & Cari Teks

- [x] **F5.1.1** Query arsip ke daftar
- [x] **F5.1.2** Thumbnail halaman pertama
- [x] **F5.2.1** Tabel FTS untuk isi OCR
- [~] **F5.2.2** Layar pencarian dengan sorotan
  - Pencarian FTS jalan; sorotan kata di dalam PDF belum.
- [ ] **F5.3.1** Gabung halaman ke dokumen
- [ ] **F5.3.2** Jaga urutan halaman
- [ ] **F5.4.1** Ekspor PDF lewat SAF
- [ ] **F5.4.2** Ekspor TXT

## F6. Pengaturan Pemindaian

Belum dimulai.

- [ ] **F6.1.1** Slider ambang sudut
- [ ] **F6.1.2** Pakai nilai tersimpan di mesin kesiapan
- [ ] **F6.2.1** Pilihan mode flash
- [ ] **F6.2.2** Terapkan mode flash saat capture
- [ ] **F6.3.1** Daftar bahasa OCR
- [ ] **F6.3.2** Unduh model bahasa tambahan

## Catatan

- `prd.md` belum ditulis ke repo; isinya masih di percakapan.
- APK rilis 9,2 MB, ditandatangani kunci debug.
- Uji perangkat terakhir: simpan PDF berhasil setelah perbaikan keep rule `fontbox`.
