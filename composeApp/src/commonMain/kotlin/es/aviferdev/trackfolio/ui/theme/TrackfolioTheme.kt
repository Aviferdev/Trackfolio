package es.aviferdev.trackfolio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Paleta Revolut-style (DARK MODE — única paleta soportada) ────────────────
// Diseño denso y funcional inspirado en Revolut/Wise.
// Acento índigo eléctrico, fondo casi-negro, superficies de bajo contraste.

/** Acento principal — índigo eléctrico. */
val PrimaryDark = Color(0xFF5B57F5)

/** Variante oscura del acento (para gradientes, sombras). */
val PrimaryVariant = Color(0xFF4440D4)

/** Acento semi-transparente (chips activos, fondos de badge). */
val PrimaryAlpha = Color(0x335B57F5)

/** Verde para ingresos / valores positivos. */
val IncomeGreen = Color(0xFF2DC265)

/** Alias de compatibilidad. */
val PositiveGreen = IncomeGreen

/** Rojo para gastos / errores / acciones destructivas. */
val ExpenseRed = Color(0xFFEC4246)

/** Alias de compatibilidad. */
val NegativeRed = ExpenseRed

/** Amarillo para advertencias / vencimientos. */
val WarnAmber = Color(0xFFF59E0B)

/** Cian para banners secundarios / renta fija. */
val SecondaryTeal = Color(0xFF06B6D4)

/** Fondo principal de pantallas — casi negro. */
val BackgroundGray = Color(0xFF0D0D0D)

/** Superficies de nivel 1 (cards, sheets, top bars). */
val SurfaceWhite = Color(0xFF141414)

/** Superficies de nivel 2 (inputs, chips inactivos, rows). */
val SurfaceElevated = Color(0xFF1C1C1C)

/** Superficies de nivel 3 (elementos anidados). */
val Surface3 = Color(0xFF232323)

/** Superficies de nivel 3 (elementos anidados). */
val Surface4 = Color(0xFF1C1C1C)

/** Divisor / borde sutil nivel 1. */
val BorderGray = Color(0xFF242424)

/** Divisor / borde nivel 2 (hover, énfasis). */
val BorderGray2 = Color(0xFF2C2C2C)

/** Texto principal — blanco puro. */
val TextPrimary = Color(0xFFFFFFFF)

/** Texto secundario — 72 % opacidad. */
val TextSecondary = Color(0xB8FFFFFF)   // ~72 %

/** Texto terciario — 42 % opacidad. */
val TextTertiary = Color(0x6BFFFFFF)   // ~42 %

/** Texto deshabilitado / hint — 24 % opacidad. */
val TextDisabled = Color(0x3DFFFFFF)   // ~24 %

/**
 * Paleta para colorear categorías en el donut chart del portfolio.
 * Pensada para destacar sobre fondo oscuro; se asigna por índice (sortOrder).
 * Cuando hay más categorías que colores se cicla.
 * El gris se reserva para "Sin categoría".
 */
val CategoryPalette: List<Color> = listOf(
    Color(0xFF5B57F5),   // índigo
    Color(0xFFF59E0B),   // ámbar
    Color(0xFF22C55E),   // verde
    Color(0xFFB66DCB),   // violeta
    Color(0xFF06B6D4),   // cian
    Color(0xFFEF7A85),   // rosa
    Color(0xFFD4C04F),   // amarillo
    Color(0xFF8FA3B0)    // azul grisáceo
)

/** Color para activos sin categoría asignada. */
val UncategorizedColor: Color = Color(0xFF6E7480)

// ─── Material color scheme ────────────────────────────────────────────────────
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

@Composable
fun TrackfolioTheme(content: @Composable () -> Unit) {
    // La aplicación está bloqueada en modo oscuro.
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
