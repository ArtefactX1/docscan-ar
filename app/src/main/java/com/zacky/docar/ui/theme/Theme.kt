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
import androidx.compose.ui.unit.sp
import com.zacky.docar.R

val Latar = Color(0xFF0A0A0B)
val Permukaan = Color(0xFF141416)
val Garis = Color(0xFF2A2A2E)
val Teks = Color(0xFFF4F4F5)
val TeksRedup = Color(0xFF8B8B90)
val Aksen = Color(0xFFE7E9EC)
val Siap = Color(0xFF3DDC97)
val Peringatan = Color(0xFFF5A524)

private val Skema = darkColorScheme(
    primary = Aksen,
    onPrimary = Latar,
    background = Latar,
    onBackground = Teks,
    surface = Permukaan,
    onSurface = Teks,
    surfaceVariant = Color(0xFF1C1C1F),
    onSurfaceVariant = TeksRedup,
    outline = Garis,
)

private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private val Tipografi = Typography(
    headlineMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

@Composable
fun DocScanTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Skema, typography = Tipografi, content = content)
}
