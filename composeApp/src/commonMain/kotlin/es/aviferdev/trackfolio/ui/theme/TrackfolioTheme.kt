package es.aviferdev.trackfolio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─── Paleta de la aplicación (DARK MODE — única paleta soportada) ─────────────
// Mantenemos los nombres históricos (PrimaryDark, BackgroundGray, SurfaceWhite,
// TextPrimary, TextSecondary, BorderGray) por compatibilidad con todo el código
// existente, pero sus valores corresponden a una paleta dark mode coherente.

/** Color de acento principal — azul medio que mantiene contraste con texto blanco. */
val PrimaryDark    = Color(0xFF3D6EAD)

/** Verde para ingresos / valores positivos. */
val IncomeGreen    = Color(0xFF66BB6A)

/** Alias para PositiveGreen (compatibilidad). */
val PositiveGreen = IncomeGreen

/** Rojo para gastos / errores / acciones destructivas. */
val ExpenseRed     = Color(0xFFEF5350)

/** Alias para NegativeRed (compatibilidad). */
val NegativeRed = ExpenseRed

/** Fondo principal de pantallas. */
val BackgroundGray = Color(0xFF111318)

/** Color de superficies (cards, sheets, app bars). */
val SurfaceWhite   = Color(0xFF1C1C22)

/** Color de superficies elevadas (selección, tags activos). */
val SurfaceElevated = Color(0xFF2A2D36)

/** Borde / divisor sutil. */
val BorderGray     = Color(0xFF3A3A44)

/** Texto principal. */
val TextPrimary    = Color(0xFFE2E2E9)

/** Texto secundario / labels. */
val TextSecondary  = Color(0xFF9AA0AC)

/**
 * Paleta para colorear categorías en el donut chart del portfolio. Pensada
 * para destacar bien sobre fondo oscuro (Background/Surface). Se asigna por
 * índice (sortOrder de la categoría); cuando hay más categorías que colores,
 * se cicla. El gris se reserva para "Sin categoría".
 */
val CategoryPalette: List<Color> = listOf(
    Color(0xFF3D6EAD),
    Color(0xFFE0A458),
    Color(0xFF66BB6A),
    Color(0xFFB66DCB),
    Color(0xFF4FC3D7),
    Color(0xFFEF7A85),
    Color(0xFFD4C04F),
    Color(0xFF8FA3B0)
)

/** Color reservado para activos sin categoría asignada. */
val UncategorizedColor: Color = Color(0xFF6E7480)

// ─── Material color scheme ────────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary           = PrimaryDark,
    onPrimary         = Color.White,
    background        = BackgroundGray,
    onBackground      = TextPrimary,
    surface           = SurfaceWhite,
    onSurface         = TextPrimary,
    surfaceVariant    = SurfaceElevated,
    onSurfaceVariant  = TextSecondary,
    outline           = BorderGray,
    secondary         = Color(0xFF7FA8D4),
    onSecondary       = Color.White,
    tertiary          = IncomeGreen,
    onTertiary        = Color(0xFF0B1F0B),
    error             = ExpenseRed,
    onError           = Color.White
)

@Composable
fun TrackfolioTheme(
    content: @Composable () -> Unit
) {
    // La aplicación está bloqueada en modo oscuro — no se ofrece modo claro.
    MaterialTheme(
        colorScheme = DarkColors,
        content     = content
    )
}
