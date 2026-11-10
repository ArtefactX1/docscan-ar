# OpenCV dipanggil lewat JNI, nama kelas harus utuh.
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

# PdfBox memakai reflection untuk font dan security handler.
-keep class com.tom_roush.pdfbox.** { *; }
-keep class com.tom_roush.fontbox.** { *; }
-dontwarn com.tom_roush.pdfbox.**
-dontwarn com.tom_roush.fontbox.**

# BouncyCastle sengaja dikecualikan; PdfBox hanya menyentuhnya untuk PDF terenkripsi.
-dontwarn org.bouncycastle.**
-dontwarn javax.naming.**
-dontwarn org.apache.**

# ML Kit memuat kelas model secara dinamis.
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
