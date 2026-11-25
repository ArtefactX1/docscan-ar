package com.zacky.docar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zacky.docar.R

val Latar = Color(0xFF0A0A0B)
val Permukaan = Color(0xFF141416)
val PermukaanNaik = Color(0xFF1B1B1E)

/** Divider saja, jangan dipakai sebagai keliling kotak. */
val Garis = Color(0x12FFFFFF)

val Teks = Color(0xFFF4F4F5)
val TeksRedup = Color(0xFF8B8B90)
val Aksen = Color(0xFFE7E9EC)
val Siap = Color(0xFF3DDC97)
val Peringatan = Color(0xFFF5A524)

val R1 = 4.dp
val R2 = 10.dp
val R3 = 16.dp
val RFull = 999.dp

val S1 = 4.dp
val S2 = 8.dp
val S3 = 12.dp
val S4 = 16.dp
val S5 = 24.dp
val S6 = 32.dp

private val Skema = darkColorScheme(
    primary = Aksen,
    onPrimary = Latar,
    background = Latar,
    onBackground = Teks,
    surface = Permukaan,
    onSurface = Teks,
    surfaceVariant = PermukaanNaik,
    onSurfaceVariant = TeksRedup,
    outline = Garis,
)

private val Instrument = FontFamily(
    Font(R.font.instrument_regular, FontWeight.Normal),
    Font(R.font.instrument_medium, FontWeight.Medium),
    Font(R.font.instrument_semibold, FontWeight.SemiBold),
)

/** Tabular figures bawaan, jadi angka tidak perlu slot lebar tetap. */
val Mono = FontFamily(Font(R.font.mono_medium, FontWeight.Medium))

private val Tipografi = Typography(
    headlineMedium = TextStyle(
        fontFamily = Instrument,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Instrument,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Instrument,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Instrument,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Instrument,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Instrument,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
)

/** Telemetri dan angka: mono supaya lebarnya tidak bergeser. */
val Angka = TextStyle(
    fontFamily = Mono,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 16.sp,
)

@Composable
fun DocScanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Skema, typography = Tipografi, content = content)
}
