package es.aviferdev.trackfolio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryDark    = Color(0xFF0D1F3C)
val IncomeGreen    = Color(0xFF2E7D32)
val ExpenseRed     = Color(0xFFC62828)
val BackgroundGray = Color(0xFFF5F5F5)
val SurfaceWhite   = Color(0xFFFFFFFF)
val BorderGray     = Color(0xFFE0E0E0)
val TextPrimary    = Color(0xFF1A1A1A)
val TextSecondary  = Color(0xFF757575)

private val LightColors = lightColorScheme(
    primary           = PrimaryDark,
    onPrimary         = Color.White,
    background        = BackgroundGray,
    onBackground      = TextPrimary,
    surface           = SurfaceWhite,
    onSurface         = TextPrimary,
    surfaceVariant    = BackgroundGray,
    outline           = BorderGray,
    secondary         = Color(0xFF455A7A),
    onSecondary       = Color.White,
    tertiary          = Color(0xFF2E7D32),
    onTertiary        = Color.White
)

private val DarkColors = darkColorScheme(
    primary           = Color(0xFF4A7FBF),
    onPrimary         = Color.White,
    background        = Color(0xFF111318),
    onBackground      = Color(0xFFE2E2E9),
    surface           = Color(0xFF1C1C22),
    onSurface         = Color(0xFFE2E2E9),
    surfaceVariant    = Color(0xFF242428),
    outline           = Color(0xFF3A3A44),
    secondary         = Color(0xFF7FA8D4),
    onSecondary       = Color(0xFF1A1A2E),
    tertiary          = Color(0xFF4CAF50),
    onTertiary        = Color.Black
)

@Composable
fun TrackfolioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
