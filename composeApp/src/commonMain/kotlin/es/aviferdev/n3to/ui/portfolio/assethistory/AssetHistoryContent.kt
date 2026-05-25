package es.aviferdev.n3to.ui.portfolio.assethistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.ui.common.button.FloatingButtonAdd
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.portfolio.AssetHistoryUiState
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.asset_history_new_movement
import n3to.composeapp.generated.resources.asset_history_title_fallback
import n3to.composeapp.generated.resources.error_asset_not_found
import n3to.composeapp.generated.resources.portfolio_dividend_register
import n3to.composeapp.generated.resources.portfolio_dividend_title_alt
import n3to.composeapp.generated.resources.portfolio_transfer_confirm
import n3to.composeapp.generated.resources.portfolio_update_price_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AssetHistoryContent(
    state: AssetHistoryUiState,
    balancesHidden: Boolean,
    fabMenuOpen: Boolean,
    onFabClick: () -> Unit,
    onFabDismiss: () -> Unit,
    onBack: () -> Unit,
    onRefreshClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onEditTransaction: (AssetTransaction) -> Unit,
    onDeleteTransaction: (AssetTransaction) -> Unit,
    onAddDividendClick: () -> Unit,
    onDeleteDividend: (String) -> Unit,
    onTransferClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarWithActionsApp(
                title = state.asset?.ticker ?: stringResource(Res.string.asset_history_title_fallback),
                subtitle = state.asset?.name,
                navigateBack = onBack,
                actions = {
                    val canRefresh = !AssetCategoryType.isFixedIncome(
                        state.asset?.assetCategoryId ?: ""
                    )
                    if (canRefresh) {
                        IconButton(onClick = onRefreshClick) {
                            Icon(
                                Icons.Outlined.Refresh,
                                contentDescription = stringResource(Res.string.portfolio_update_price_title),
                                tint = MaterialTheme.appColors.textSecondary
                            )
                        }
                    }
                }
            )

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MaterialTheme.appColors.primary) }

                state.asset == null -> Box(
                    Modifier.fillMaxSize().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(Res.string.error_asset_not_found),
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Summary card (price + position)
                    item {
                        AssetSummaryCard(
                            ticker = state.asset.ticker,
                            currentPrice = state.asset.currentPrice,
                            position = state.position,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Maturity (fixed income)
                    state.asset.maturityDate?.let { md ->
                        if (AssetCategoryType.isFixedIncome(state.asset.assetCategoryId)) {
                            item {
                                MaturityDateCard(
                                    maturityDate = md,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // FIFO breakdown
                    state.breakdown?.takeIf { it.hasAnyData }?.let { bd ->
                        item {
                            FifoBreakdownSection(
                                breakdown = bd,
                                balancesHidden = balancesHidden,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Transactions header
                    item {
                        SectionLabel(
                            "Movimientos",
                            modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
                        )
                    }

                    if (state.transactionsDesc.isEmpty()) {
                        item { EmptyCard(modifier = Modifier.padding(horizontal = 16.dp)) }
                    } else {
                        items(state.transactionsDesc, key = { it.id }) { tx ->
                            TxRow(
                                tx = tx,
                                platform = state.platforms.firstOrNull { it.id == tx.platformId },
                                balancesHidden = balancesHidden,
                                onEdit = { onEditTransaction(tx) },
                                onDelete = { onDeleteTransaction(tx) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Dividends
                    if (state.dividends.isNotEmpty()) {
                        item {
                            SectionLabel(
                                stringResource(Res.string.portfolio_dividend_title_alt),
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 18.dp,
                                    bottom = 6.dp
                                )
                            )
                        }
                        items(state.dividends, key = { it.id }) { dividend ->
                            DividendRow(
                                dividend = dividend,
                                balancesHidden = balancesHidden,
                                onDelete = {
                                    onDeleteDividend(
                                        dividend.linkedAssetTransactionId ?: dividend.id
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        if (state.asset != null) {
            val isFixedIncome = AssetCategoryType.isFixedIncome(state.asset.assetCategoryId)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 28.dp)
            ) {
                FloatingButtonAdd(
                    onClick = onFabClick,
                    modifier = Modifier.size(52.dp),
                )
                DropdownMenu(
                    expanded = fabMenuOpen,
                    onDismissRequest = onFabDismiss,
                    containerColor = MaterialTheme.appColors.surface
                ) {
                    if (!isFixedIncome) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.asset_history_new_movement),
                                    color = MaterialTheme.appColors.textPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = { Text("💱", fontSize = 14.sp) },
                            onClick = { onFabDismiss(); onAddTransactionClick() }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.portfolio_dividend_register),
                                    color = MaterialTheme.appColors.textPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ShowChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = { onFabDismiss(); onAddDividendClick() }
                        )
                    }
                    if (state.isTransferable) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.portfolio_transfer_confirm),
                                    color = MaterialTheme.appColors.textPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = { Text("🔄", fontSize = 14.sp) },
                            onClick = { onFabDismiss(); onTransferClick() }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AssetHistoryContentPreview() {
    val fakeAsset = es.aviferdev.n3to.domain.model.Asset(
        id = "asset_1",
        accountId = "acc_1",
        ticker = "AAPL",
        name = "Apple Inc.",
        notes = null,
        createdAt = 1704067200000,
        currentPrice = 178.50,
        assetCategoryId = "stocks"
    )

    val fakePosition = AssetPosition(
        netQuantity = 10.0,
        averageCostOfRemaining = 150.0,
        totalInvestedRemaining = 1500.0,
        realizedPnL = 50.0,
        currentValue = 1785.0,
        unrealizedPnL = 235.0,
        unrealizedPnLPercent = 15.67,
        totalPnL = 285.0,
        totalPnLPercent = 19.0,
        hasCurrentPrice = true,
        dividendIncome = 0.0
    )

    val fakeState = AssetHistoryUiState(
        asset = fakeAsset,
        position = fakePosition,
        transactionsDesc = listOf(
            AssetTransaction(
                id = "tx_1",
                assetId = "asset_1",
                type = AssetTransactionType.BUY,
                quantity = 5.0,
                pricePerUnit = 150.0,
                date = 1704067200000,
                platformId = "plat_1",
                createdAt = 1704067200000
            ),
            AssetTransaction(
                id = "tx_2",
                assetId = "asset_1",
                type = AssetTransactionType.BUY,
                quantity = 5.0,
                pricePerUnit = 150.0,
                date = 1706745600000,
                platformId = "plat_1",
                createdAt = 1706745600000
            )
        ),
        dividends = listOf(
            Transaction(
                id = "div_1",
                accountId = "acc_1",
                amount = 25.0,
                type = TransactionType.INCOME,
                categoryId = null,
                date = 1709251200000,
                notes = "Dividendo Apple",
                createdAt = 1709251200000,
                linkedAssetTransactionId = "tx_1"
            )
        ),
        platforms = listOf(
            Platform(
                id = "plat_1",
                name = "Interactive Brokers",
                icon = "📊",
                createdAt = 1704067200000
            )
        ),
        isLoading = false,
        isTransferable = false
    )

    N3toTheme {
        AssetHistoryContent(
            state = fakeState,
            balancesHidden = false,
            fabMenuOpen = false,
            onFabClick = {},
            onFabDismiss = {},
            onBack = {},
            onRefreshClick = {},
            onAddTransactionClick = {},
            onEditTransaction = {},
            onDeleteTransaction = {},
            onAddDividendClick = {},
            onDeleteDividend = {},
            onTransferClick = {}
        )
    }
}
