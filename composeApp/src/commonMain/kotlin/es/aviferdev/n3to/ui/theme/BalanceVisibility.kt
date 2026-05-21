package es.aviferdev.n3to.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * CompositionLocal que indica si los saldos deben ocultarse en la UI.
 *
 * Se provee desde `App.kt` observando el `StateFlow` de
 * `BalanceVisibilityManager`. Cualquier pantalla puede leerlo con
 * `LocalBalanceHidden.current` sin necesidad de inyectar el manager directamente.
 */
val LocalBalanceHidden = compositionLocalOf { false }

/**
 * CompositionLocal que expone el padding inferior que las screens deben aplicar
 * para que su contenido no quede oculto detrás de la floating bottom nav bar.
 *
 * Se provee desde `N3toContent` únicamente en las rutas donde la barra es visible.
 * Screens sin bottom bar reciben el valor por defecto (0.dp).
 */
val LocalBottomNavPadding = compositionLocalOf<Dp> { 0.dp }

const val HIDDEN_AMOUNT_MASK: String = "•••••"

/**
 * Devuelve [text] o la máscara según el flag [hidden].
 * Útil cuando ya tienes la cadena ya formateada (con sufijos de moneda, etc.).
 */
fun maskAmount(text: String, hidden: Boolean): String = if (hidden) {
    HIDDEN_AMOUNT_MASK
} else {
    text
}
