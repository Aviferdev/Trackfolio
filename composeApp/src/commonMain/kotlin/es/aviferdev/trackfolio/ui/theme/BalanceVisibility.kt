package es.aviferdev.trackfolio.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal que indica si los saldos deben ocultarse en la UI.
 *
 * Se provee desde `App.kt` observando el `StateFlow` de
 * `BalanceVisibilityManager`. Cualquier pantalla puede leerlo con
 * `LocalBalanceHidden.current` sin necesidad de inyectar el manager directamente.
 */
val LocalBalanceHidden = compositionLocalOf { false }

/** Cadena que sustituye al importe cuando los saldos están ocultos. */
const val HIDDEN_AMOUNT_MASK: String = "•••••"

/**
 * Devuelve [text] o la máscara según el flag [hidden].
 * Útil cuando ya tienes la cadena ya formateada (con sufijos de moneda, etc.).
 */
fun maskAmount(text: String, hidden: Boolean): String =
    if (hidden) HIDDEN_AMOUNT_MASK else text

/**
 * Versión Composable que lee directamente el [LocalBalanceHidden] del árbol.
 * Pasa por aquí cuando estés dentro de un `@Composable` y quieras ahorrar
 * el `LocalBalanceHidden.current` manual.
 *
 * Ejemplo de uso:
 * ```
 * Text(text = displayAmount("${formatAmount(balance)} €"))
 * ```
 */
@Composable
@ReadOnlyComposable
fun displayAmount(formattedText: String): String =
    if (LocalBalanceHidden.current) HIDDEN_AMOUNT_MASK else formattedText
