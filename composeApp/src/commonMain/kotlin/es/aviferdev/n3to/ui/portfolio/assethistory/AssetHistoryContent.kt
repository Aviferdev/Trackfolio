package es.aviferdev.n3to.ui.portfolio.assethistory


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.portfolio.AssetHistoryUiState
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
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
            .background(BackgroundGray)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBarApp(
                title = state.asset?.ticker ?: "Activo",
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
                                contentDescription = "Actualizar precio",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            )

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryDark) }

                state.asset == null -> Box(
                    Modifier.fillMaxSize().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        state.error ?: "Activo no encontrado",
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Summary card (price + position)
                    item {
                        AssetSummaryCard(
                            ticker = state.asset!!.ticker,
                            currentPrice = state.asset!!.currentPrice,
                                                        position = state.position,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Maturity (fixed income)
                    state.asset!!.maturityDate?.let { md ->
                        if (AssetCategoryType.isFixedIncome(state.asset!!.assetCategoryId)) {
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
                                "Dividendos",
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
                FloatingActionButton(
                    onClick = onFabClick,
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    containerColor = PrimaryDark,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Text("+", fontSize = 26.sp, fontWeight = FontWeight.Light, color = Color.White)
                }
                DropdownMenu(
                    expanded = fabMenuOpen,
                    onDismissRequest = onFabDismiss,
                    containerColor = SurfaceWhite
                ) {
                    if (!isFixedIncome) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Nuevo movimiento",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = { Text("💱", fontSize = 14.sp) },
                            onClick = { onFabDismiss(); onAddTransactionClick() }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Registrar dividendo",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = { Icon(Icons.Outlined.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = { onFabDismiss(); onAddDividendClick() }
                        )
                    }
                    if (state.isTransferable) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Traspasar fondo",
                                    color = TextPrimary,
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


// ─── Top bar ──────────────────────────────────────────────────────────────────
@Composable
private fun AssetTopBar(
    ticker: String?,
    name: String?,
    onBack: () -> Unit,
    onRefresh: (() -> Unit)? = null
) {
    Surface(color = SurfaceWhite, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary)
            }
            Spacer(Modifier.width(2.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ticker ?: "—",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-.3).sp
                )
                if (name != null) {
                    Text(name, fontSize = 12.sp, color = TextTertiary, maxLines = 1)
                }
            }
            if (onRefresh != null) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Outlined.Refresh,
                        "Actualizar precio",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    HorizontalDivider(color = BorderGray, thickness = .5.dp)
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
