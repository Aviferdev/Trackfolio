package es.aviferdev.n3to.ui.savingsrates

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import es.aviferdev.n3to.ui.common.component.NavyTabRow
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.formatDateLocalized
import es.aviferdev.n3to.ui.theme.formatPercent
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_update
import n3to.composeapp.generated.resources.savings_rates_cancellation_no
import n3to.composeapp.generated.resources.savings_rates_cancellation_yes
import n3to.composeapp.generated.resources.savings_rates_country
import n3to.composeapp.generated.resources.savings_rates_max
import n3to.composeapp.generated.resources.savings_rates_medium_term
import n3to.composeapp.generated.resources.savings_rates_min
import n3to.composeapp.generated.resources.savings_rates_offline
import n3to.composeapp.generated.resources.savings_rates_short_term
import n3to.composeapp.generated.resources.savings_rates_term
import n3to.composeapp.generated.resources.savings_rates_title
import n3to.composeapp.generated.resources.savings_rates_updated_format
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsRatesScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavingsRatesViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.savings_rates_title),
            navigateBack = onNavigateBack,
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = stringResource(Res.string.common_update),
                        tint = MaterialTheme.appColors.textSecondary
                    )
                }
            }
        )

        NavyTabRow(
            items = SavingsRateType.entries,
            selected = state.selectedTab,
            onSelect = { viewModel.selectTab(it) },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            content = { tab ->
                val text = if (tab == SavingsRateType.SHORT_TERM) stringResource(Res.string.savings_rates_short_term) else stringResource(Res.string.savings_rates_medium_term)
                Text(
                    text = text,
                    fontSize = 12.sp,
                    fontWeight = if (tab == state.selectedTab) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (tab == state.selectedTab) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
                )
            }
        )

        if (state.isStale) {
            Text(
                text = stringResource(Res.string.savings_rates_offline),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.warnAmber,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        state.lastUpdatedAt?.let { ts ->
            Text(
                text = stringResource(Res.string.savings_rates_updated_format, formatDateLocalized(ts)),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
        }

        val rates = if (state.selectedTab == SavingsRateType.SHORT_TERM)
            state.shortTermRates else state.mediumTermRates

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            when {
                state.isLoading && rates.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.cyanAccent)
                    }
                }

                state.error != null && rates.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.error!!,
                            color = MaterialTheme.appColors.expense,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item { Spacer(Modifier.height(4.dp)) }
                        itemsIndexed(rates, key = { index, _ -> index }) { _, rate ->
                            SavingsRateCard(rate = rate)
                        }
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavingsRateCard(rate: SavingsRate, modifier: Modifier = Modifier) {
    val currency = LocalCurrencySymbol.current
    Card(
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
                Text(
                    text = rate.entity,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${formatPercent(rate.interestRate * 100)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.income
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rate.maxAmount?.let { max ->
                    MetaChip(label = stringResource(Res.string.savings_rates_max), value = "$currency${formatAmount(max)}")
                }
                rate.minAmount?.let { min ->
                    MetaChip(label = stringResource(Res.string.savings_rates_min), value = "$currency${formatAmount(min)}")
                }
                rate.termMonths?.let { months ->
                    MetaChip(label = stringResource(Res.string.savings_rates_term), value = "${months}m")
                }
                rate.country?.let { country ->
                    MetaChip(label = stringResource(Res.string.savings_rates_country), value = country)
                }
            }

            rate.conditions?.let { cond ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = cond,
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary,
                    lineHeight = 15.sp
                )
            }

            if (rate.type == SavingsRateType.MEDIUM_TERM) {
                val badges = buildList {
                    rate.earlyRedemption?.let { if (it) add(stringResource(Res.string.savings_rates_cancellation_yes)) else add(stringResource(Res.string.savings_rates_cancellation_no)) }
                    rate.fgdGuaranteed?.let { if (it) add("FGD") }
                    rate.obligation720?.let { if (it) add("720") }
                }
                if (badges.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        badges.forEach { badge ->
                            Text(
                                text = badge,
                                fontSize = 10.sp,
                                color = MaterialTheme.appColors.cyanSubtle,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.appColors.navySelected,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.appColors.textTertiary)
        Text(
            text = value,
            fontSize = 11.sp,
            color = MaterialTheme.appColors.textSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}
