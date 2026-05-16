package es.aviferdev.n3to.ui.onboarding

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.BorderGray2
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Slide 4 — Movimientos con sentido.
 *
 * Muestra una lista de 5 transacciones de ejemplo con iconos emoji,
 * categorías y montos, con entrada escalonada.
 */
@Composable
fun SlideMovimientos(modifier: Modifier = Modifier) {

    // Transaction data
    data class TxRow(
        val emoji: String,
        val label: String,
        val sublabel: String,
        val amount: String,
        val amountColor: Color,
        val badgeBg: Color
    )

    val transactions = listOf(
        TxRow("🏠", "Alquiler", "Hogar", "−850,00 €", ExpenseRed, Color(0xFFF97316).copy(alpha = 0.13f)),
        TxRow("💼", "Nómina", "Trabajo · 15% IRPF", "+2.800,00 €", IncomeGreen, IncomeGreen.copy(alpha = 0.13f)),
        TxRow("🚗", "Gasolina", "Transporte", "−65,40 €", ExpenseRed, Color(0xFF06B6D4).copy(alpha = 0.13f)),
        TxRow("🛒", "Mercadona", "Alimentación", "−112,30 €", ExpenseRed, IncomeGreen.copy(alpha = 0.13f)),
        TxRow("🎬", "Netflix", "Ocio", "−15,99 €", ExpenseRed, Color(0xFFF59E0B).copy(alpha = 0.13f))
    )

    // ── Staggered entrance animations ────────────────────
    val rowVisible = remember {
        transactions.indices.map { mutableStateOf(false) }.toMutableList()
    }

    LaunchedEffect(Unit) {
        transactions.indices.forEach { i ->
            delay(i * 100L)
            rowVisible[i].value = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        transactions.forEachIndexed { index, tx ->
            val rowAlpha by animateFloatAsState(
                targetValue = if (rowVisible[index].value) 1f else 0f,
                animationSpec = tween(durationMillis = 500, easing = EaseInOutCubic)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = rowAlpha }
                    .padding(bottom = 8.dp)
                    .background(
                        color = PrimaryDark.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(13.dp)
                    )
                    .border(1.dp, BorderGray2, RoundedCornerShape(13.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = tx.badgeBg,
                                shape = RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tx.emoji,
                            fontSize = 18.sp,
                            color = Color.Unspecified
                        )
                    }

                    Spacer(Modifier.size(12.dp))

                    // Label + sublabel
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = tx.sublabel,
                            fontSize = 10.sp,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Amount
                    Text(
                        text = tx.amount,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tx.amountColor
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SlideMovimientosPreview() {
    N3toTheme {
        SlideMovimientos()
    }
}
