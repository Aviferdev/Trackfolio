package es.aviferdev.n3to.ui.theme

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal que indica si los saldos deben ocultarse en la UI.
 *
 * Se provee desde `App.kt` observando el `StateFlow` de
 * `BalanceVisibilityManager`. Cualquier pantalla puede leerlo con
 * `LocalBalanceHidden.current` sin necesidad de inyectar el manager directamente.
 */
val LocalBalanceHidden = compositionLocalOf { false }

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
