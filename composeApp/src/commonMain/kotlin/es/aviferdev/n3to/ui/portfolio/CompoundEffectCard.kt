package es.aviferdev.n3to.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.portfolio.CompoundEffect
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.maskAmount
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_compound_effect_estimated
import n3to.composeapp.generated.resources.portfolio_compound_effect_title
import n3to.composeapp.generated.resources.portfolio_compound_low_effect
import n3to.composeapp.generated.resources.portfolio_compound_no_effect
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.pow

private const val PROJECTION_YEARS = 30

/**
 * Tarjeta colapsable que muestra el efecto compuesto del portfolio.
 *
 * **Colapsada**: importe principal en una línea compacta.
 * **Expandida** (click): detalle de la métrica y proyección a [PROJECTION_YEARS] años.
 */
@Composable
fun CompoundEffectCard(
    compoundEffect: CompoundEffect?,
    balancesHidden: Boolean,
    modifier: Modifier = Modifier
) {
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    if (compoundEffect == null) return

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(heroCardBg1, heroCardBg2),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 90.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appCCyanGlow.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        CompactHeader(
            compoundEffect = compoundEffect,
            balancesHidden = balancesHidden,
            expanded = expanded
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                ExpandedContent(
                    compoundEffect = compoundEffect,
                    balancesHidden = balancesHidden
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  CABECERA COMPACTA
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CompactHeader(
    compoundEffect: CompoundEffect,
    balancesHidden: Boolean,
    expanded: Boolean
) {
    val showPositive = compoundEffect.totalReturn > 0.0
            && compoundEffect.compoundEffect > 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text("✨", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))

            if (showPositive) {
                Column {
                    Text(
                        maskAmount(
                            "+${formatAmount(compoundEffect.compoundEffect)} €",
                            balancesHidden
                        ),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.appColors.warnAmber
                    )
                    Text(
                        "extra por interés compuesto",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            } else {
                Text(
                    stringResource(Res.string.portfolio_compound_effect_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }

        Text(
            if (expanded) "▲" else "▼",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  CONTENIDO EXPANDIDO
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ExpandedContent(
    compoundEffect: CompoundEffect,
    balancesHidden: Boolean
) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
        Spacer(Modifier.height(20.dp))

        when {
            compoundEffect.totalReturn <= 0.0 -> {
                EmptyState(
                    message = stringResource(Res.string.portfolio_compound_no_effect)
                )
            }

            compoundEffect.compoundEffect <= 0.0 -> {
                EmptyState(
                    message = stringResource(Res.string.portfolio_compound_low_effect)
                )
            }

            else -> {
                MainMetric(compoundEffect, balancesHidden)
                Spacer(Modifier.height(6.dp))
                InsightText(compoundEffect)
                Spacer(Modifier.height(24.dp))
                ProjectionSection(compoundEffect, balancesHidden)
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.5.dp)
                Spacer(Modifier.height(14.dp))
                Disclaimer()
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Text(
        "+0,00 €",
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.4f),
        letterSpacing = (-1).sp
    )
    Spacer(Modifier.height(6.dp))
    Text(
        message,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.45f),
        lineHeight = 16.sp
    )
}

@Composable
private fun MainMetric(
    compoundEffect: CompoundEffect,
    balancesHidden: Boolean
) {
    Text(
        stringResource(Res.string.portfolio_compound_effect_estimated),
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.55f)
    )
    Spacer(Modifier.height(4.dp))
    Text(
        maskAmount(
            "+${formatAmount(compoundEffect.compoundEffect)} €",
            balancesHidden
        ),
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.appColors.warnAmber,
        letterSpacing = (-1).sp
    )
    Text(
        "extra gracias al interés compuesto",
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.5f)
    )
}

@Composable
private fun InsightText(compoundEffect: CompoundEffect) {
    if (compoundEffect.compoundEffectPercent <= 0.0) return
    Text(
        "El ${formatPercent(compoundEffect.compoundEffectPercent)}% de tu beneficio total " +
                "viene del efecto compuesto",
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.45f)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
//  PROYECCIÓN A 30 AÑOS
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProjectionSection(
    compoundEffect: CompoundEffect,
    balancesHidden: Boolean
) {
    if (compoundEffect.avgCagr <= 0.0) return

    val cagrDecimal = compoundEffect.avgCagr / 100.0
    val futureCompounded = compoundEffect.totalInvested * (1.0 + cagrDecimal).pow(PROJECTION_YEARS)
    val futureSimple = compoundEffect.totalInvested * (1.0 + cagrDecimal * PROJECTION_YEARS)
    val futureCompoundEffect = maxOf(0.0, futureCompounded - futureSimple)

    Text(
        "📈 Proyección a $PROJECTION_YEARS años",
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.55f)
    )
    Spacer(Modifier.height(2.dp))
    Text(
        "Si mantienes la misma tasa (${formatPercent(compoundEffect.avgCagr)}% anual)",
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.4f)
    )
    Spacer(Modifier.height(10.dp))

    Text(
        maskAmount(
            "+${formatAmount(futureCompoundEffect)} €",
            balancesHidden
        ),
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.appColors.warnAmber,
        letterSpacing = (-1).sp
    )
    Text(
        "extra gracias al interés compuesto en $PROJECTION_YEARS años",
        fontSize = 12.sp,
        color = Color.White.copy(alpha = 0.5f)
    )
}

@Composable
private fun Disclaimer() {
    Text(
        "⚡ Cálculo basado solo en renta variable. La renta fija paga intereses en efectivo.",
        fontSize = 10.sp,
        color = Color.White.copy(alpha = 0.3f)
    )
}

// ═══════════════════════════════════════════════════════════════════════════════
//  PREVIEWS
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
private fun CompoundEffectCardPositivePreview() {
    N3toTheme {
        CompoundEffectCard(
            compoundEffect = CompoundEffect(
                totalInvested = 10_000.0,
                totalValue = 16_105.0,
                totalReturn = 6_105.0,
                simpleReturnValue = 14_500.0,
                compoundEffect = 1_605.0,
                compoundEffectPercent = 26.3,
                positionCount = 3,
                avgCagr = 10.0,
                positions = emptyList()
            ),
            balancesHidden = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun CompoundEffectCardLossPreview() {
    N3toTheme {
        CompoundEffectCard(
            compoundEffect = CompoundEffect(
                totalInvested = 10_000.0,
                totalValue = 8_000.0,
                totalReturn = -2_000.0,
                simpleReturnValue = 8_000.0,
                compoundEffect = 0.0,
                compoundEffectPercent = 0.0,
                positionCount = 0,
                avgCagr = 0.0,
                positions = emptyList()
            ),
            balancesHidden = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
