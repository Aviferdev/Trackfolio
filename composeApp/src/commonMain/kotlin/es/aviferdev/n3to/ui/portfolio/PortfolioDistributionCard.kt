package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.toMaterialIcon
import androidx.compose.foundation.BorderStroke
import es.aviferdev.n3to.ui.portfolio.home.CategorySlice
import es.aviferdev.n3to.ui.portfolio.home.DistributionView
import es.aviferdev.n3to.ui.theme.CategoryPalette

import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import es.aviferdev.n3to.ui.theme.maskAmount
import org.jetbrains.compose.ui.tooling.preview.Preview
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_total
import n3to.composeapp.generated.resources.portfolio_distribution_by_category
import n3to.composeapp.generated.resources.portfolio_distribution_by_region
import n3to.composeapp.generated.resources.portfolio_distribution_by_sector
import n3to.composeapp.generated.resources.portfolio_distribution_subtitle
import n3to.composeapp.generated.resources.portfolio_distribution_view_composition
import n3to.composeapp.generated.resources.portfolio_uncategorized
import org.jetbrains.compose.resources.stringResource

/**
 * Donut chart con leyenda. Muestra el reparto del valor actual del portfolio
 * por categoría de activo. Calculado y coloreado en `PortfolioViewModel`.
 *
 * Pintamos los arcos manualmente con `Canvas` (sin librerías externas) para
 * mantener el módulo KMP ligero y evitar dependencias específicas de plataforma.
 */
@Composable
fun PortfolioDistributionCard(
    slices: List<CategorySlice>,
    totalCurrentValue: Double,
    balancesHidden: Boolean,
    selectedView: DistributionView = DistributionView.CATEGORY,
    modifier: Modifier = Modifier,
    viewSelector: (@Composable () -> Unit)? = null
) {
    val title = when (selectedView) {
        DistributionView.CATEGORY -> stringResource(Res.string.portfolio_distribution_by_category)
        DistributionView.COMPOSITION -> stringResource(Res.string.portfolio_distribution_view_composition)
        DistributionView.REGION -> stringResource(Res.string.portfolio_distribution_by_region)
        DistributionView.SECTOR -> stringResource(Res.string.portfolio_distribution_by_sector)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(Res.string.portfolio_distribution_subtitle),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            if (viewSelector != null) {
                Spacer(Modifier.height(10.dp))
                viewSelector()
            }

            Spacer(Modifier.height(16.dp))

            if (slices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.portfolio_uncategorized),
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // ── Donut ────────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutCanvas(
                            slices = slices,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                        )
                        // Total al centro
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(Res.string.common_total),
                                fontSize = 10.sp,
                                color = MaterialTheme.appColors.textSecondary
                            )
                            Text(
                                text = maskAmount(
                                    formatAmountEuro(totalCurrentValue),
                                    balancesHidden
                                ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.appColors.textPrimary
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // ── Leyenda ─────────────────────────────────────────────
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        slices.forEach { slice ->
                            LegendRow(slice = slice)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutCanvas(
    slices: List<CategorySlice>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val side = minOf(size.width, size.height)
        val strokeW = side * 0.22f
        val padding = strokeW / 2f
        val arcSize = Size(side - strokeW, side - strokeW)
        val topLeft = Offset(padding, padding)
        val gapDeg = 1.5f      // separación visual entre slices
        var startAngle = -90f      // empezar arriba (12 en punto)

        slices.forEachIndexed { index, slice ->
            val fullSweep = slice.percent.toFloat() * 360f / 100f
            val isLast = index == slices.lastIndex
            val sweep = if (isLast) fullSweep else fullSweep - gapDeg
            if (sweep > 0f) {
                drawArc(
                    color = slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeW)
                )
            }
            startAngle += fullSweep
        }
    }
}

@Composable
private fun LegendRow(slice: CategorySlice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(slice.color)
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = slice.icon.toMaterialIcon(),
            contentDescription = null,
            tint = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = slice.name,
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.weight(1f, fill = true),
            maxLines = 1
        )
        Text(
            text = "${formatPercentLegend(slice.percent)}%",
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.wrapContentSize()
        )
    }
}

/** Formatea el % de la leyenda con un decimal — "23,4". Sin signo. */
private fun formatPercentLegend(value: Double): String {
    val rounded = (value * 10.0).toLong()
    return "${rounded / 10},${rounded % 10}"
}

private fun createMockSlices(): List<CategorySlice> {
    return listOf(
        CategorySlice(
            categoryId = "1",
            name = "Acciones",
            icon = "📈",
            value = 45000.0,
            percent = 45.0,
            color = CategoryPalette[0]
        ),
        CategorySlice(
            categoryId = "2",
            name = "Renta Fija",
            icon = "📋",
            value = 30000.0,
            percent = 30.0,
            color = CategoryPalette[1]
        ),
        CategorySlice(
            categoryId = "3",
            name = "Cripto",
            icon = "🪙",
            value = 15000.0,
            percent = 15.0,
            color = CategoryPalette[2]
        ),
        CategorySlice(
            categoryId = "4",
            name = "ETF",
            icon = "📊",
            value = 10000.0,
            percent = 10.0,
            color = CategoryPalette[3]
        )
    )
}

@Preview
@Composable
private fun PortfolioDistributionCardPreview() {
    N3toTheme {
        PortfolioDistributionCard(
            slices = createMockSlices(),
            totalCurrentValue = 100000.0,
            balancesHidden = false
        )
    }
}

@Preview
@Composable
private fun PortfolioDistributionCardHiddenPreview() {
    N3toTheme {
        PortfolioDistributionCard(
            slices = createMockSlices(),
            totalCurrentValue = 100000.0,
            balancesHidden = true
        )
    }
}
