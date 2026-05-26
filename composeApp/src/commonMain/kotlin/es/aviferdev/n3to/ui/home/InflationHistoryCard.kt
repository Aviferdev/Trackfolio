package es.aviferdev.n3to.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.InflationDataPoint
import es.aviferdev.n3to.ui.theme.CategoryPalette
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.inflation_card_no_data
import n3to.composeapp.generated.resources.inflation_history_year_col
import org.jetbrains.compose.resources.stringResource

val INFLATION_COUNTRIES = listOf("ES", "US", "GB", "DE", "FR", "IT", "JP", "CN", "BR", "IN")

val INFLATION_COUNTRY_COLORS = mapOf(
    "ES" to 0, "US" to 1, "GB" to 2, "DE" to 3, "FR" to 4,
    "IT" to 5, "JP" to 6, "CN" to 7, "BR" to 4, "IN" to 5
)

private val COUNTRY_FLAGS = mapOf(
    "ES" to "🇪🇸", "US" to "🇺🇸", "GB" to "🇬🇧", "DE" to "🇩🇪", "FR" to "🇫🇷",
    "IT" to "🇮🇹", "JP" to "🇯🇵", "CN" to "🇨🇳", "BR" to "🇧🇷", "IN" to "🇮🇳"
)

@Composable
fun InflationCountryChipRow(
    selectedCountries: List<String>,
    onToggleCountry: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        INFLATION_COUNTRIES.forEach { code ->
            val colorIdx = INFLATION_COUNTRY_COLORS[code] ?: 0
            val chipColor = CategoryPalette.getOrElse(colorIdx) { CategoryPalette[0] }
            CountryChip(
                code = code,
                flag = COUNTRY_FLAGS[code] ?: code,
                selected = code in selectedCountries,
                color = chipColor,
                onClick = { onToggleCountry(code) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InflationSummaryTab(
    isLoading: Boolean,
    data: Map<String, List<InflationDataPoint>>,
    selectedCountries: List<String>,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxWidth().height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.appColors.cyanAccent,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        data.isEmpty() || data.all { it.value.isEmpty() } -> {
            Box(
                modifier = modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.inflation_card_no_data),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }

        else -> {
            FlowRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                selectedCountries.forEach { code ->
                    val points = data[code]?.sortedBy { it.year } ?: return@forEach
                    if (points.isEmpty()) return@forEach
                    val colorIdx = INFLATION_COUNTRY_COLORS[code] ?: 0
                    val lineColor = CategoryPalette.getOrElse(colorIdx) { CategoryPalette[0] }
                    CountryInflationCard(
                        code = code,
                        flag = COUNTRY_FLAGS[code] ?: code,
                        points = points,
                        lineColor = lineColor,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (selectedCountries.size % 2 != 0) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun InflationHistoryTab(
    isLoading: Boolean,
    data: Map<String, List<InflationDataPoint>>,
    selectedCountries: List<String>,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.appColors.cyanAccent,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        selectedCountries.isEmpty() || data.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.inflation_card_no_data),
                    fontSize = 12.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }

        else -> {
            val allYears = selectedCountries
                .flatMap { data[it]?.map { pt -> pt.year } ?: emptyList() }
                .distinct()
                .sortedDescending()

            val yearColWidth = 52.dp
            val countryColWidth = 72.dp
            val borderColor = MaterialTheme.appColors.navyBorder
            val headerBg = MaterialTheme.appColors.navySurface

            Box(modifier = modifier.horizontalScroll(rememberScrollState())) {
                Column {
                    // Header row
                    Row(
                        modifier = Modifier
                            .background(headerBg)
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(
                            text = stringResource(Res.string.inflation_history_year_col),
                            isHeader = true,
                            color = MaterialTheme.appColors.textSecondary,
                            width = yearColWidth
                        )
                        selectedCountries.forEach { code ->
                            TableCell(
                                text = "${COUNTRY_FLAGS[code] ?: ""} $code",
                                isHeader = true,
                                color = MaterialTheme.appColors.textSecondary,
                                width = countryColWidth
                            )
                        }
                    }

                    HorizontalDivider(color = borderColor, thickness = 1.dp)

                    // Data rows
                    allYears.forEachIndexed { idx, year ->
                        val rowBg = if (idx % 2 == 0) MaterialTheme.appColors.navySurface
                        else MaterialTheme.appColors.navyDeep

                        Row(
                            modifier = Modifier.background(rowBg).padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableCell(
                                text = year.toString(),
                                isHeader = false,
                                color = MaterialTheme.appColors.textTertiary,
                                width = yearColWidth
                            )
                            selectedCountries.forEach { code ->
                                val point = data[code]?.find { it.year == year }
                                val rateText = point?.let { "${"%.1f".format(it.rate)}%" } ?: "—"
                                val rateColor = point?.let { rateSemanticColor(it.rate) }
                                    ?: MaterialTheme.appColors.textTertiary
                                TableCell(
                                    text = rateText,
                                    isHeader = false,
                                    color = rateColor,
                                    width = countryColWidth
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rateSemanticColor(rate: Double): Color = when {
    rate > 5.0 -> MaterialTheme.appColors.expense
    rate > 2.0 -> MaterialTheme.appColors.warnAmber
    rate < 0.0 -> MaterialTheme.appColors.cyanAccent
    else -> MaterialTheme.appColors.income
}

@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean,
    color: Color,
    width: Dp
) {
    Text(
        text = text,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        fontSize = if (isHeader) 11.sp else 12.sp,
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CountryChip(
    code: String,
    flag: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val bgColor = if (selected) color.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (selected) color else MaterialTheme.appColors.navyBorder
    val textColor = if (selected) color else MaterialTheme.appColors.textTertiary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = flag, fontSize = 12.sp)
        Text(
            text = code,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun CountryInflationCard(
    code: String,
    flag: String,
    points: List<InflationDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val latest = points.last()
    val previous = points.getOrNull(points.size - 2)
    val trend = when {
        previous == null -> null
        latest.rate > previous.rate -> true
        latest.rate < previous.rate -> false
        else -> null
    }
    val rateColor = rateSemanticColor(latest.rate)
    val borderColor = MaterialTheme.appColors.navyBorder

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.appColors.navySurface)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = flag, fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = code,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
            Text(
                text = latest.year.toString(),
                fontSize = 10.sp,
                color = MaterialTheme.appColors.textTertiary
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${"%.1f".format(latest.rate)}%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = rateColor
            )
            if (trend != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (trend) "↑" else "↓",
                    fontSize = 14.sp,
                    color = if (trend) MaterialTheme.appColors.expense else MaterialTheme.appColors.income,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Sparkline(
            points = points.takeLast(10),
            lineColor = lineColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        )
    }
}

@Composable
private fun Sparkline(
    points: List<InflationDataPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) return

    val rates = points.map { it.rate }
    val minRate = rates.min()
    val maxRate = rates.max()
    val range = (maxRate - minRate).coerceAtLeast(0.5)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val step = w / (points.size - 1)

        fun xOf(i: Int) = i * step
        fun yOf(rate: Double) = (h - ((rate - minRate) / range * h)).toFloat().coerceIn(2f, h - 2f)

        val path = Path()
        points.forEachIndexed { i, pt ->
            val x = xOf(i)
            val y = yOf(pt.rate)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        drawCircle(
            color = lineColor,
            radius = 3f,
            center = Offset(xOf(points.size - 1), yOf(points.last().rate))
        )
    }
}
