package es.aviferdev.n3to.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.model.PortfolioValuePoint
import es.aviferdev.n3to.ui.common.LineChartWithTimeRange
import es.aviferdev.n3to.ui.common.component.EmptyStateView
import es.aviferdev.n3to.ui.common.component.NavyTab
import es.aviferdev.n3to.ui.fixedincome.FixedIncomePositionCard
import es.aviferdev.n3to.ui.portfolio.home.DistributionView
import es.aviferdev.n3to.ui.portfolio.home.PortfolioUiState
import es.aviferdev.n3to.ui.savingsrates.SavingsRatePreviewCard
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_go_to_settings
import n3to.composeapp.generated.resources.portfolio_distribution_view_category
import n3to.composeapp.generated.resources.portfolio_distribution_view_composition
import n3to.composeapp.generated.resources.portfolio_distribution_view_region
import n3to.composeapp.generated.resources.portfolio_distribution_view_sector
import n3to.composeapp.generated.resources.portfolio_empty_title
import n3to.composeapp.generated.resources.portfolio_evolution_title
import n3to.composeapp.generated.resources.portfolio_monthly_value
import n3to.composeapp.generated.resources.portfolio_no_portfolios_subtitle
import n3to.composeapp.generated.resources.portfolio_no_portfolios_title
import n3to.composeapp.generated.resources.portfolio_no_positions_select
import n3to.composeapp.generated.resources.portfolio_no_positions_wallet
import org.jetbrains.compose.resources.stringResource

/**
 * Contenido de una tab individual de Portfolio.
 *
 * @param portfolioId  null = "Todas", String = cartera concreta
 * @param isTodasTab   true cuando es la tab de "Todas"
 * @param state        estado UI calculado por el ViewModel para este tab
 * @param valueHistory histórico de valor para el gráfico
 * @param balancesHidden si los saldos deben ocultarse
 * @param portfolios   lista de carteras (para empty states)
 * @param onNavigateToSettings callback para ir a ajustes
 * @param onAssetClick callback al pulsar un activo
 * @param onFixedIncomeClick callback al pulsar una posición de RF
 * @param onSelectDistributionView callback al cambiar vista de distribución
 * @param onOpenUpdatePriceSheet callback para actualizar precio
 * @param onShowRegisterCouponSheet callback para registrar cupón
 */
@Composable
fun PortfolioTabContent(
    portfolioId: String?,
    isTodasTab: Boolean = portfolioId == null,
    state: PortfolioUiState,
    valueHistory: List<PortfolioValuePoint>,
    portfolios: List<Portfolio>,
    onNavigateToSettings: () -> Unit,
    onAssetClick: (String) -> Unit,
    onFixedIncomeClick: (String) -> Unit,
    onNavigateToSavingsRates: () -> Unit = {},
    onSelectDistributionView: (DistributionView) -> Unit,
    onOpenUpdatePriceSheet: (Asset) -> Unit,
    onShowRegisterCouponSheet: (FixedIncomePosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val balancesHidden = LocalBalanceHidden.current
    var closedExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Caso 1: No hay carteras creadas ──────────────────────────────
            if (isTodasTab && portfolios.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = "📂",
                        title = stringResource(Res.string.portfolio_no_portfolios_title),
                        subtitle = stringResource(Res.string.portfolio_no_portfolios_subtitle),
                        actionLabel = stringResource(Res.string.common_go_to_settings),
                        onAction = onNavigateToSettings
                    )
                }
                return@LazyColumn
            }

            // ── Caso 2: Cargando ─────────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.appColors.primary)
                    }
                }
                return@LazyColumn
            }

            // ── Caso 3: Sin posiciones (cartera vacía) ───────────────────────
            if (state.groups.isEmpty() && state.closedPositions.isEmpty()) {
                item {
                    if (isTodasTab) {
                        EmptyStateView(
                            icon = "📈",
                            title = stringResource(Res.string.portfolio_empty_title),
                            subtitle = stringResource(Res.string.portfolio_no_positions_select)
                        )
                    } else {
                        EmptyStateView(
                            icon = "📈",
                            title = stringResource(Res.string.portfolio_empty_title),
                            subtitle = stringResource(Res.string.portfolio_no_positions_wallet)
                        )
                    }
                }
                return@LazyColumn
            }

            // ── Caso 4: Con posiciones — contenido completo ──────────────────

            // PortfolioSummaryCard
            item {
                PortfolioSummaryCard(
                    totalInvested = state.totalInvested,
                    totalCurrentValue = state.totalCurrentValue,
                    totalPnL = state.totalPnL,
                    totalPnLPercent = state.totalPnLPercent,
                    totalRealizedPnL = state.totalRealizedPnL,
                    totalUnrealizedPnL = state.totalUnrealizedPnL,
                    positionsCount = state.openPositionsCount,
                    balancesHidden = balancesHidden,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // CompoundEffectCard
            item {
                CompoundEffectCard(
                    compoundEffect = state.compoundEffect,
                    balancesHidden = balancesHidden,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Line Chart
            if (valueHistory.isNotEmpty()) {
                item {
                    LineChartWithTimeRange(
                        title = stringResource(Res.string.portfolio_evolution_title),
                        subtitle = stringResource(Res.string.portfolio_monthly_value),
                        points = valueHistory.map { it.date to it.value },
                        lineColor = MaterialTheme.appColors.cyanAccent,
                        balancesHidden = balancesHidden,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Distribution card
            val hasDistribution = state.distribution.isNotEmpty()
                    || state.compositionDistribution.isNotEmpty()
                    || state.regionDistribution.isNotEmpty()
                    || state.sectorDistribution.isNotEmpty()

            if (hasDistribution) {
                item {
                    val currentDist = when (state.selectedDistributionView) {
                        DistributionView.CATEGORY -> state.distribution
                        DistributionView.COMPOSITION -> state.compositionDistribution
                        DistributionView.REGION -> state.regionDistribution
                        DistributionView.SECTOR -> state.sectorDistribution
                    }
                    PortfolioDistributionCard(
                        slices = currentDist,
                        totalCurrentValue = state.combinedCurrentValue,
                        balancesHidden = balancesHidden,
                        selectedView = state.selectedDistributionView,
                        fixedIncomePercent = state.fixedIncomeSummary?.let { fi ->
                            if (state.combinedCurrentValue > 0) (fi.totalCurrentValue / state.combinedCurrentValue) * 100 else 0.0
                        } ?: 0.0,
                        viewSelector = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                DistributionView.entries.forEach { view ->
                                    NavyTab(
                                        selected = state.selectedDistributionView == view,
                                        onClick = { onSelectDistributionView(view) },
                                        content = {
                                            Text(
                                                text = when (view) {
                                                    DistributionView.CATEGORY -> stringResource(Res.string.portfolio_distribution_view_category)
                                                    DistributionView.COMPOSITION -> stringResource(Res.string.portfolio_distribution_view_composition)
                                                    DistributionView.REGION -> stringResource(Res.string.portfolio_distribution_view_region)
                                                    DistributionView.SECTOR -> stringResource(Res.string.portfolio_distribution_view_sector)
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = if (state.selectedDistributionView == view) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (state.selectedDistributionView == view) MaterialTheme.appColors.textPrimary else MaterialTheme.appColors.textSecondary
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Grupos de posiciones abiertas
            state.groups.forEach { group ->
                item(key = "hdr_${group.category?.id ?: "none"}") {
                    CategoryGroupHeader(
                        group = group,
                        balancesHidden = balancesHidden
                    )
                }
                items(group.rows, key = { "open_${it.asset.id}" }) { row ->
                    AssetCard(
                        row = row,
                        balancesHidden = balancesHidden,
                        onClick = { onAssetClick(row.asset.id) },
                        onUpdatePrice = { onOpenUpdatePriceSheet(row.asset) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
                if (group.fixedIncomeRows.isNotEmpty()) {
                    item(key = "fi_hdr_${group.category?.id ?: "none"}") {
                        FixedIncomeSectionHeader(
                            count = group.fixedIncomeRows.size,
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(group.fixedIncomeRows, key = { "fi_${it.position.id}" }) { fiRow ->
                        FixedIncomePositionCard(
                            row = fiRow,
                            balancesHidden = balancesHidden,
                            onClick = { onFixedIncomeClick(fiRow.position.id) },
                            onRegisterCoupon = if (fiRow.position.hasPeriodicCoupons) {
                                { onShowRegisterCouponSheet(fiRow.position) }
                            } else null,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Posiciones cerradas
            val totalClosedCount =
                state.closedPositions.size + state.closedFixedIncomePositions.size
            if (totalClosedCount > 0) {
                item(key = "closed_hdr") {
                    ClosedPositionsHeader(
                        count = totalClosedCount,
                        expanded = closedExpanded,
                        onToggle = { closedExpanded = !closedExpanded }
                    )
                }
                item(key = "closed_list") {
                    AnimatedVisibility(
                        visible = closedExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            state.closedPositions.forEach { row ->
                                ClosedAssetCard(
                                    row = row,
                                    balancesHidden = balancesHidden,
                                    onClick = { onAssetClick(row.asset.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                )
                            }
                            state.closedFixedIncomePositions.forEach { fiRow ->
                                ClosedFixedIncomeCard(
                                    row = fiRow,
                                    balancesHidden = balancesHidden,
                                    onClick = { onFixedIncomeClick(fiRow.position.id) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isTodasTab) {
                item(key = "savings_rates_preview") {
                    SavingsRatePreviewCard(
                        onNavigateToSavingsRates = onNavigateToSavingsRates,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
