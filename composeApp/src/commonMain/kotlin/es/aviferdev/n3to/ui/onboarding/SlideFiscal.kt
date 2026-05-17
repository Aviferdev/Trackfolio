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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.BorderGray2
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.PrimaryVariant
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryLight
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Slide 5 — Informe fiscal listo.
 *
 * Muestra una tarjeta degradada con la base imponible y un grid
 * de rendimientos/retenciones, seguido de una lista de operaciones
 * marcadas con sus implicaciones fiscales.
 */
@Composable
fun SlideFiscal(modifier: Modifier = Modifier) {

    // ── Entrance animations ──────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    var opsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(250)
        opsVisible = true
    }
    val opsAlpha by animateFloatAsState(
        targetValue = if (opsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    // Operations data
    data class OpRow(val emoji: String, val label: String, val value: String)
    val operations = listOf(
        OpRow("📄", "Factura freelance", "−67,50 € IRPF"),
        OpRow("💼", "Nómina marzo", "−450,00 € IRPF"),
        OpRow("📈", "Venta AAPL (3 ud.)", "+134,70 € plusv.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // ── Base imponible card (gradient) ───────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = cardAlpha }
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryDark, PrimaryLight),
                        start = Offset.Zero,
                        end = Offset(600f, 600f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                // Label
                Text(
                    text = "BASE IMPONIBLE 2025",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 0.7.sp
                )

                Spacer(Modifier.height(8.dp))

                // Amount
                Text(
                    text = "29.955 €",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1.2).sp
                )

                Spacer(Modifier.height(14.dp))

                // Two-column grid: Rendimientos / Retenciones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GridCell(
                        label = "RENDIMIENTOS",
                        value = "+33.600 €",
                        modifier = Modifier.weight(1f)
                    )
                    GridCell(
                        label = "RETENCIONES",
                        value = "−4.032 €",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Operations card ──────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = opsAlpha }
                .background(
                    color = PrimaryDark.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(13.dp)
                )
                .border(1.dp, BorderGray2, RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "OPERACIONES MARCADAS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary,
                    letterSpacing = 0.7.sp
                )

                Spacer(Modifier.height(10.dp))

                operations.forEachIndexed { index, op ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .padding(vertical = 9.dp)
                                .background(BorderGray)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = op.emoji,
                            fontSize = 14.sp,
                            color = Color.Unspecified
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = op.label,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = op.value,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Small cell used inside the gradient card grid.
 * Shows an uppercase label and a value underneath.
 */
@Composable
private fun GridCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White.copy(alpha = 0.14f),
                shape = RoundedCornerShape(9.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.65f),
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun SlideFiscalPreview() {
    N3toTheme {
        SlideFiscal()
    }
}
