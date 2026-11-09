package uz.varaq.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Forest = Color(0xFF263F36)
val Cream = Color(0xFFF7F3EB)
val Gold = Color(0xFFC19A6B)
val SoftMint = Color(0xFFE8EEE5)
val Ink = Color(0xFF292B29)
val Muted = Color(0xFF7E8279)

private val light = lightColorScheme(
    primary = Forest, onPrimary = Color.White,
    secondary = Gold, onSecondary = Ink,
    background = Cream, onBackground = Ink,
    surface = Color(0xFFFFFDF8), onSurface = Ink,
    surfaceVariant = SoftMint, onSurfaceVariant = Forest,
    outline = Color(0xFFD8D8CD), outlineVariant = Color(0xFFE8E6DE)
)
private val dark = darkColorScheme(
    primary = Color(0xFFB3D1BC), onPrimary = Color(0xFF17281F),
    secondary = Color(0xFFDDC39C), onSecondary = Color(0xFF292015),
    background = Color(0xFF151B18), onBackground = Color(0xFFEDECE4),
    surface = Color(0xFF202923), onSurface = Color(0xFFEDECE4),
    surfaceVariant = Color(0xFF304038), onSurfaceVariant = Color(0xFFE0E6DC),
    outline = Color(0xFF69756B), outlineVariant = Color(0xFF3C493F)
)
private val typography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 34.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontSize = 28.sp, fontWeight = FontWeight.Medium),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontSize = 23.sp, fontWeight = FontWeight.Medium),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontSize = 21.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun VaraqTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) dark else light, typography = typography, content = content)
}
