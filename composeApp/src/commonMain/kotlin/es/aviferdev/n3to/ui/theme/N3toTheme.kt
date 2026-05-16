package es.aviferdev.n3to.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Paleta Revolut-style (DARK MODE) ──────────────────────────────────────────
val PrimaryDark = Color(0xFF5B57F5)
val PrimaryVariant = Color(0xFF4440D4)
val PrimaryAlpha = Color(0x335B57F5)
val IncomeGreen = Color(0xFF2DC265)
val PositiveGreen = IncomeGreen
val ExpenseRed = Color(0xFFEC4246)
val NegativeRed = ExpenseRed
val WarnAmber = Color(0xFFF59E0B)
val SecondaryTeal = Color(0xFF06B6D4)

// Dark mode
val BackgroundGray = Color(0xFF0D0D0D)
val SurfaceWhite = Color(0xFF141414)
val SurfaceElevated = Color(0xFF1C1C1C)
val Surface3 = Color(0xFF232323)
val Surface4 = Color(0xFF1C1C1C)
val BorderGray = Color(0xFF242424)
val BorderGray2 = Color(0xFF2C2C2C)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xB8FFFFFF)
val TextTertiary = Color(0x6BFFFFFF)
val TextDisabled = Color(0x3DFFFFFF)

// Light mode
val BackgroundWhite = Color(0xFFF5F5F5)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightElevated = Color(0xFFF0F0F0)
val BorderLight = Color(0xFFE0E0E0)
val BorderLight2 = Color(0xFFD0D0D0)
val TextPrimaryLight = Color(0xFF1A1A1A)
val TextSecondaryLight = Color(0xFF666666)
val TextTertiaryLight = Color(0xFF999999)

val CategoryPalette: List<Color> = listOf(
    Color(0xFF5B57F5),
    Color(0xFFF59E0B),
    Color(0xFF22C55E),
    Color(0xFFB66DCB),
    Color(0xFF06B6D4),
    Color(0xFFEF7A85),
    Color(0xFFD4C04F),
    Color(0xFF8FA3B0)
)

val UncategorizedColor: Color = Color(0xFF6E7480)

// ─── Material color schemes ────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = PrimaryAlpha,
    onPrimaryContainer = PrimaryDark,
    background = BackgroundGray,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderGray,
    outlineVariant = BorderGray2,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0x1906B6D4),
    onSecondaryContainer = SecondaryTeal,
    tertiary = IncomeGreen,
    onTertiary = Color(0xFF0B1F0B),
    tertiaryContainer = Color(0x1922C55E),
    onTertiaryContainer = IncomeGreen,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0x19EF4444),
    onErrorContainer = ExpenseRed,
    inverseSurface = Color(0xFFE2E2E9),
    inverseOnSurface = BackgroundGray,
    scrim = Color(0xA6000000)
)

private val LightColors = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E7FF),
    onPrimaryContainer = PrimaryDark,
    background = BackgroundWhite,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    outlineVariant = BorderLight2,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = SecondaryTeal,
    tertiary = IncomeGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = IncomeGreen,
    error = ExpenseRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = ExpenseRed,
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFF5F5F5),
    scrim = Color(0xA6000000)
)

/**
 * Tema de Trackfolio.
 *
 * @param darkTheme Si es `true`, usa el esquema oscuro. Si es `false`, usa el claro.
 * @param content Contenido a renderizar con el tema.
 */
@Composable
fun N3toTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
