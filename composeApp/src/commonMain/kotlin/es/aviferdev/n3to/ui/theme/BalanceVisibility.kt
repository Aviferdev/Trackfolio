package es.aviferdev.n3to.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

val LocalBalanceHidden = compositionLocalOf { false }
val LocalFiscalAmountsHidden = compositionLocalOf { false }
val LocalBottomNavPadding = compositionLocalOf { 0.dp }

const val HIDDEN_AMOUNT_MASK: String = "•••••••"

fun maskAmount(text: String, hidden: Boolean): String = if (hidden) {
    HIDDEN_AMOUNT_MASK
} else {
    text
}
