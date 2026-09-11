package com.divyadhun.app.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.divyadhun.app.ThemeMode
private val Light = lightColorScheme(
 primary = Color(0xFF4A1018), onPrimary = Color(0xFFFFF9EF),
 primaryContainer = Color(0xFFF4DFDC), onPrimaryContainer = Color(0xFF4A1018),
 secondary = Color(0xFF795400), onSecondary = Color.White,
 secondaryContainer = Color(0xFFFFE7B0), onSecondaryContainer = Color(0xFF3D2B00),
 background = Color(0xFFFFF9EF), onBackground = Color(0xFF281C19),
 surface = Color(0xFFFFF9EF), onSurface = Color(0xFF281C19),
 surfaceVariant = Color(0xFFF0E5DC), onSurfaceVariant = Color(0xFF66534D),
 outline = Color(0xFF85726C), error = Color(0xFFBA1A1A)
)
private val Dark = darkColorScheme(
 primary = Color(0xFFF3B7BD), onPrimary = Color(0xFF4A1018),
 primaryContainer = Color(0xFF55262C), onPrimaryContainer = Color(0xFFFFDADD),
 secondary = Color(0xFFEAC16A), onSecondary = Color(0xFF402D00),
 secondaryContainer = Color(0xFF584113), onSecondaryContainer = Color(0xFFFFE7B0),
 background = Color(0xFF17100E), onBackground = Color(0xFFF5E5DC),
 surface = Color(0xFF17100E), onSurface = Color(0xFFF5E5DC),
 surfaceVariant = Color(0xFF352723), onSurfaceVariant = Color(0xFFD4BFB5),
 outline = Color(0xFFA68F84), error = Color(0xFFFFB4AB)
)
@Immutable data class DevotionalColors(val accent: Color, val gold: Color, val success: Color)
val LocalDevotionalColors = staticCompositionLocalOf { DevotionalColors(Color(0xFFF59E0B), Color(0xFFD5A33B), Color(0xFF356B43)) }
object Space { val xs = 4.dp; val sm = 8.dp; val md = 16.dp; val lg = 24.dp; val xl = 32.dp }
val DivyaTypography = Typography(
 displaySmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 44.sp),
 headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 32.sp, lineHeight = 40.sp),
 headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 34.sp, fontWeight = FontWeight.Medium),
 titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
 bodyLarge = TextStyle(fontSize = 17.sp, lineHeight = 27.sp),
 bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 24.sp),
 labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
)
fun ThemeMode.usesDark(systemDark: Boolean) = this == ThemeMode.DARK || (this == ThemeMode.SYSTEM && systemDark)
@Composable fun DivyaTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
 val dark = mode.usesDark(isSystemInDarkTheme())
 CompositionLocalProvider(LocalDevotionalColors provides DevotionalColors(Color(0xFFF59E0B), Color(0xFFD5A33B), if(dark) Color(0xFF9BD4A5) else Color(0xFF356B43))) {
  MaterialTheme(colorScheme = if (dark) Dark else Light, typography = DivyaTypography,
   shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)), content = content)
 }
}

