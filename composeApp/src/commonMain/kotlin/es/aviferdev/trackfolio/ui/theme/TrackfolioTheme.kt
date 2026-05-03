package es.aviferdev.trackfolio.ui.theme

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
    primary        = PrimaryDark,
    onPrimary      = Color.White,
    background     = BackgroundGray,
    onBackground   = TextPrimary,
    surface        = SurfaceWhite,
    onSurface      = TextPrimary,
    surfaceVariant = BackgroundGray,
    outline        = BorderGray
)

private val DarkColors = darkColorScheme(
    primary        = Color(0xFF4A7FBF),
    onPrimary      = Color.White,
    background     = Color(0xFF121212),
    onBackground   = Color(0xFFE0E0E0),
    surface        = Color(0xFF1E1E1E),
    onSurface      = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    outline        = Color(0xFF444444)
)

@Composable
fun TrackfolioTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
