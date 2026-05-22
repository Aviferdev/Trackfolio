package es.aviferdev.n3to.ui.onboarding

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme

import kotlinx.coroutines.delay
import n3to.composeapp.generated.resources.*
import n3to.composeapp.generated.resources.onboarding_fiscal_base_label
import n3to.composeapp.generated.resources.onboarding_fiscal_operations_label
import n3to.composeapp.generated.resources.onboarding_fiscal_rendimientos
import n3to.composeapp.generated.resources.onboarding_fiscal_retenciones
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Slide 5 — Informe fiscal listo.
 */
@Composable
fun SlideFiscal(modifier: Modifier = Modifier) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCNavyBorder = MaterialTheme.appColors.navyBorder
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextSecondary = MaterialTheme.appColors.textSecondary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    var opsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(250); opsVisible = true }
    val opsAlpha by animateFloatAsState(
        targetValue = if (opsVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = EaseInOutCubic)
    )

    data class OpRow(val emoji: String, val label: String, val value: String)

    val operations = listOf(
        OpRow("📄", stringResource(Res.string.onboarding_demo_factura), "−67,50 € IRPF"),
        OpRow("💼", stringResource(Res.string.onboarding_demo_nomina_marzo), "−450,00 € IRPF"),
        OpRow("📈", stringResource(Res.string.onboarding_demo_venta_aapl), "+134,70 € plusv.")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // ── Base imponible card (gradiente diagonal navy) ─
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = cardAlpha }
                .clip(RoundedCornerShape(16.dp))
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(heroCardBg1, heroCardBg2),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.onboarding_fiscal_base_label),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 0.7.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "29.955 €",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1.2).sp
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GridCell(
                        label = stringResource(Res.string.onboarding_fiscal_rendimientos),
                        value = "+33.600 €",
                        modifier = Modifier.weight(1f)
                    )
                    GridCell(
                        label = stringResource(Res.string.onboarding_fiscal_retenciones),
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
                .background(color = heroCardBg1, shape = RoundedCornerShape(13.dp))
                .border(1.dp, appCNavyBorder, RoundedCornerShape(13.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = stringResource(Res.string.onboarding_fiscal_operations_label),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = appCTextTertiary,
                    letterSpacing = 0.7.sp
                )

                Spacer(Modifier.height(10.dp))

                operations.forEachIndexed { index, op ->
                    if (index > 0) {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(appCNavyBorder)
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = op.emoji, fontSize = 14.sp, color = Color.Unspecified)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = op.label,
                            fontSize = 11.sp,
                            color = appCTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = op.value,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = appCTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCell(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = Color.White.copy(alpha = 0.10f), shape = RoundedCornerShape(9.dp))
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
