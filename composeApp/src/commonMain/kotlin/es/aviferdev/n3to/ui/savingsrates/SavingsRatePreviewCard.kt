package es.aviferdev.n3to.ui.savingsrates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.SavingsRate
import es.aviferdev.n3to.domain.model.SavingsRateType
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatPercent
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.savings_rates_loading
import n3to.composeapp.generated.resources.savings_rates_preview_title
import n3to.composeapp.generated.resources.savings_rates_up_to_format
import n3to.composeapp.generated.resources.savings_rates_view_all
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SavingsRatePreviewCard(
    onNavigateToSavingsRates: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavingsRatesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val topRates = state.shortTermRates.take(3)

    Card(
        onClick = onNavigateToSavingsRates,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.appColors.navyBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.warnAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(Res.string.savings_rates_preview_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textPrimary
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.savings_rates_view_all),
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.cyanAccent
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.cyanAccent,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            if (topRates.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.appColors.navyBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(8.dp))

                topRates.forEachIndexed { index, rate ->
                    SavingsRatePreviewRow(rate = rate)
                    if (index < topRates.lastIndex) {
                        Spacer(Modifier.height(6.dp))
                    }
                }
            } else if (state.isLoading) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(Res.string.savings_rates_loading),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun SavingsRatePreviewRow(rate: SavingsRate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rate.entity,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary
            )
            rate.maxAmount?.let { max ->
                Text(
                    text = stringResource(Res.string.savings_rates_up_to_format, formatAmount(max)),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
        Text(
            text = "${formatPercent(rate.interestRate * 100)}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.appColors.income
        )
    }
}
