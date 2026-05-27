package es.aviferdev.n3to.ui.fixedincome.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.portfolio.MaturitySimulation
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.theme.*
import kotlin.math.pow
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fixedincome_annual_coupon
import n3to.composeapp.generated.resources.fixedincome_est_nir
import n3to.composeapp.generated.resources.fixedincome_header_label
import n3to.composeapp.generated.resources.fixedincome_maturity_label
import n3to.composeapp.generated.resources.fixedincome_nominal_label
import n3to.composeapp.generated.resources.fixedincome_platform_label
import n3to.composeapp.generated.resources.fixedincome_status_active
import n3to.composeapp.generated.resources.fixedincome_status_closed
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FixedIncomeDetailHeader(
    position: FixedIncomePosition,
    row: FixedIncomeRow,
    balancesHidden: Boolean,
    simulation: MaturitySimulation? = null
) {
    val currency = LocalCurrencySymbol.current
    val heroCardBg1 = MaterialTheme.appColors.heroCardStart
    val heroCardBg2 = MaterialTheme.appColors.heroCardEnd
    val appCCyanGlow = MaterialTheme.appColors.cyanGlow
    val appCTextPrimary = MaterialTheme.appColors.textPrimary
    val appCTextTertiary = MaterialTheme.appColors.textTertiary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(heroCardBg1, heroCardBg2),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 100.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(appCCyanGlow.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = orbRadius
                    ),
                    radius = orbRadius,
                    center = Offset(cx, cy)
                )
            }
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    val statusLabel = if (position.isOpen)
                        stringResource(Res.string.fixedincome_status_active)
                    else
                        stringResource(Res.string.fixedincome_status_closed)
                    Text(
                        stringResource(Res.string.fixedincome_header_label, statusLabel),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.warnAmber
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${maskAmount(formatAmount(position.principal), balancesHidden)} $currency",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = appCTextPrimary,
                        letterSpacing = (-0.8).sp
                    )
                    Text(
                        stringResource(Res.string.fixedincome_nominal_label),
                        fontSize = 11.sp,
                        color = appCTextTertiary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                StatusTag(
                    label = position.type.label.uppercase(),
                    color = MaterialTheme.appColors.warnAmber
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailCell(
                    label = stringResource(Res.string.fixedincome_annual_coupon),
                    value = "${formatPercent(position.interestRate)}% · ${
                        maskAmount(
                            formatAmount(
                                position.principal * position.interestRate / 100.0
                            ), balancesHidden
                        )
                    } $currency",
                    modifier = Modifier.weight(1f)
                )
                DetailCell(
                    label = stringResource(Res.string.fixedincome_maturity_label),
                    value = formatDateLocalized(position.maturityDate),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tirNet = if (simulation != null && position.totalTermDays > 0) {
                    val ratio = simulation.netAtMaturity / simulation.capitalInvested
                    (ratio.pow(365.0 / position.totalTermDays) - 1.0) * 100.0
                } else position.interestRate
                DetailCell(
                    label = stringResource(Res.string.fixedincome_est_nir),
                    value = "${formatPercent(tirNet)}%",
                    modifier = Modifier.weight(1f)
                )
                DetailCell(
                    label = stringResource(Res.string.fixedincome_platform_label),
                    value = position.platformId.ifEmpty { "—" },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun DetailCell(label: String, value: String, modifier: Modifier = Modifier) {
    val appCNavySurfaceLight = MaterialTheme.appColors.navySurfaceLight
    val appCCyanAccent = MaterialTheme.appColors.cyanAccent
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(appCNavySurfaceLight)
            .padding(10.dp)
    ) {
        val appCCyanAccent = MaterialTheme.appColors.cyanAccent
        Text(label, fontSize = 10.sp, color = appCCyanAccent.copy(alpha = 0.7f))
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textPrimary
        )
    }
}
