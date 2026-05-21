package es.aviferdev.n3to.ui.fiscal.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.date_month_full_01
import n3to.composeapp.generated.resources.date_month_full_02
import n3to.composeapp.generated.resources.date_month_full_03
import n3to.composeapp.generated.resources.date_month_full_04
import n3to.composeapp.generated.resources.date_month_full_05
import n3to.composeapp.generated.resources.date_month_full_06
import n3to.composeapp.generated.resources.date_month_full_07
import n3to.composeapp.generated.resources.date_month_full_08
import n3to.composeapp.generated.resources.date_month_full_09
import n3to.composeapp.generated.resources.date_month_full_10
import n3to.composeapp.generated.resources.date_month_full_11
import n3to.composeapp.generated.resources.date_month_full_12
import n3to.composeapp.generated.resources.fiscal_active_profile
import n3to.composeapp.generated.resources.fiscal_profile_since
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

@Composable
internal fun monthName(month: Int): String = when (month) {
    1 -> stringResource(Res.string.date_month_full_01)
    2 -> stringResource(Res.string.date_month_full_02)
    3 -> stringResource(Res.string.date_month_full_03)
    4 -> stringResource(Res.string.date_month_full_04)
    5 -> stringResource(Res.string.date_month_full_05)
    6 -> stringResource(Res.string.date_month_full_06)
    7 -> stringResource(Res.string.date_month_full_07)
    8 -> stringResource(Res.string.date_month_full_08)
    9 -> stringResource(Res.string.date_month_full_09)
    10 -> stringResource(Res.string.date_month_full_10)
    11 -> stringResource(Res.string.date_month_full_11)
    12 -> stringResource(Res.string.date_month_full_12)
    else -> month.toString()
}

@Composable
internal fun YearStepper(year: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    val nowYear = nowYear()
    val isMax = year.toIntOrNull() == nowYear
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.appColors.navySurfaceLight)
                .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(9.dp))
        ) {
            Text(
                "‹",
                fontSize = 20.sp,
                color = MaterialTheme.appColors.cyanAccent,
                fontWeight = FontWeight.Light
            )
        }
        Text(
            year,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        IconButton(
            onClick = onNext,
            enabled = !isMax,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (!isMax) MaterialTheme.appColors.navySurfaceLight else Color.Transparent)
                .then(
                    if (!isMax) Modifier.border(
                        0.5.dp,
                        MaterialTheme.appColors.navyBorder,
                        RoundedCornerShape(9.dp)
                    )
                    else Modifier
                )
        ) {
            Text(
                "›",
                fontSize = 20.sp,
                color = if (!isMax) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textTertiary,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
internal fun ReportCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.appColors.navySurface)
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(13.dp))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary,
                letterSpacing = (-0.2).sp
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
internal fun MetricCell(label: String, value: Double, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            label.uppercase(),
            fontSize = 9.sp,
            color = MaterialTheme.appColors.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = .4.sp
        )
        Spacer(Modifier.height(3.dp))
        Text(
            formatAmt(value),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
internal fun FiscalMetricCell(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.appColors.navySurfaceLight)
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(9.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.appColors.textTertiary)
            Spacer(Modifier.height(3.dp))
            Text(
                "${if (amount >= 0) "" else "−"}${formatAmt(abs(amount))} €",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
internal fun TaxProfileBadge(snapshot: TaxProfileSnapshot) {
    val flag = when (snapshot.profile.countryCode) {
        "ES" -> "🇪🇸"; "GB" -> "🇬🇧"; "US" -> "🇺🇸"; "DE" -> "🇩🇪"; else -> "🌐"
    }
    val label = when (snapshot.profile.countryCode) {
        "ES" -> "España"; "GB" -> "Reino Unido"; "US" -> "EE.UU."; "DE" -> "Alemania"
        else -> "Personalizado"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(MaterialTheme.appColors.navySurface)
            .border(
                0.5.dp,
                MaterialTheme.appColors.cyanAccent.copy(alpha = 0.25f),
                RoundedCornerShape(11.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(flag, fontSize = 20.sp)
            Column {
                Text(
                    stringResource(Res.string.fiscal_active_profile, label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.cyanAccent
                )
                Text(
                    stringResource(
                        Res.string.fiscal_profile_since,
                        snapshot.profile.currency,
                        snapshot.effectiveFrom
                    ),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
    }
}

internal fun formatAmt(value: Double): String {
    val sign = if (value < 0) "-" else ""
    val absVal = abs(value)
    val euros = absVal.toLong()
    val cents = ((absVal - euros) * 100 + .5).toLong().coerceIn(0, 99)
    val eurosStr = euros.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$sign$eurosStr,${cents.toString().padStart(2, '0')} €"
}

internal fun formatPct(value: Double): String {
    val i = value.toLong()
    val f = ((value - i) * 10 + .5).toLong().coerceIn(0, 9)
    return "$i,${f}%"
}
