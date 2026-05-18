package es.aviferdev.n3to.ui.fixedincome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.portfolio.ScheduledCoupon
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.formatPercent
import kotlin.math.pow
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.DividerLight
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.fixedincome_annual_coupon
import n3to.composeapp.generated.resources.fixedincome_cancel_title
import n3to.composeapp.generated.resources.fixedincome_change_region
import n3to.composeapp.generated.resources.fixedincome_change_sector
import n3to.composeapp.generated.resources.fixedincome_coupon_calendar
import n3to.composeapp.generated.resources.fixedincome_coupon_paid
import n3to.composeapp.generated.resources.fixedincome_coupon_pending
import n3to.composeapp.generated.resources.fixedincome_coupons_received
import n3to.composeapp.generated.resources.fixedincome_delete_event
import n3to.composeapp.generated.resources.fixedincome_delete_event_confirm
import n3to.composeapp.generated.resources.fixedincome_detail_title
import n3to.composeapp.generated.resources.fixedincome_distribution_title
import n3to.composeapp.generated.resources.fixedincome_est_nir
import n3to.composeapp.generated.resources.fixedincome_estimated_commissions
import n3to.composeapp.generated.resources.fixedincome_estimated_irpf
import n3to.composeapp.generated.resources.fixedincome_event_history
import n3to.composeapp.generated.resources.fixedincome_gross_interest
import n3to.composeapp.generated.resources.fixedincome_gross_label
import n3to.composeapp.generated.resources.fixedincome_commission_label
import n3to.composeapp.generated.resources.fixedincome_header_label
import n3to.composeapp.generated.resources.fixedincome_invested_label
import n3to.composeapp.generated.resources.fixedincome_liquidate_maturity
import n3to.composeapp.generated.resources.fixedincome_maturity_label
import n3to.composeapp.generated.resources.fixedincome_maturity_simulation
import n3to.composeapp.generated.resources.fixedincome_net_maturity
import n3to.composeapp.generated.resources.fixedincome_net_profit_label
import n3to.composeapp.generated.resources.fixedincome_no_events
import n3to.composeapp.generated.resources.fixedincome_no_region
import n3to.composeapp.generated.resources.fixedincome_no_sector
import n3to.composeapp.generated.resources.fixedincome_nominal_label
import n3to.composeapp.generated.resources.fixedincome_not_found
import n3to.composeapp.generated.resources.fixedincome_platform_label
import n3to.composeapp.generated.resources.fixedincome_region_label
import n3to.composeapp.generated.resources.fixedincome_register_settlement
import n3to.composeapp.generated.resources.fixedincome_retention_label
import n3to.composeapp.generated.resources.fixedincome_sector_label
import n3to.composeapp.generated.resources.fixedincome_select_region
import n3to.composeapp.generated.resources.fixedincome_select_sector
import n3to.composeapp.generated.resources.fixedincome_sell_secondary
import n3to.composeapp.generated.resources.fixedincome_status_active
import n3to.composeapp.generated.resources.fixedincome_status_closed
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// ═══════════════════════════════════════════════════════════════════════════════
// WRAPPER
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedIncomeDetailScreen(
    positionId: String,
    onBack: () -> Unit,
    viewModel: FixedIncomeDetailViewModel = koinViewModel(
        key = "fi_$positionId",
        parameters = { parametersOf(positionId) }
    )
) {
    val state by viewModel.uiState.collectAsState()

    var fabMenuOpen by remember { mutableStateOf(false) }

    val balancesHidden = LocalBalanceHidden.current

    FixedIncomeDetailContent(
        state = state,
        balancesHidden = balancesHidden,
        onBack = onBack,
        onDeleteEvent = { event -> viewModel.showDeleteEventDialog(event) },
        onUpdateRegionSector = { region, sector ->
            viewModel.updateRegionAndSector(region, sector)
        },
        onShowCloseSheet = { viewModel.showCloseSheetWithType(it) }
    )

    if (state.showDeleteEventDialog && state.selectedEventForDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteEventDialog() },
            containerColor = NavySurface,
            title = {
                Text(
                    stringResource(Res.string.fixedincome_delete_event),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    stringResource(Res.string.fixedincome_delete_event_confirm, state.selectedEventForDelete!!.type.label),
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(state.selectedEventForDelete!!) }) {
                    Text(stringResource(Res.string.common_delete), color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteEventDialog() }) {
                    Text(stringResource(Res.string.common_cancel), color = CyanAccent)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (state.showRegisterCouponSheet && state.row != null) {
        RegisterCouponBottomSheet(
            positionName = state.row!!.position.name,
            onSave = { event ->
                viewModel.registerCouponEvent(event.copy(positionId = state.row!!.position.id))
            },
            onDismiss = { viewModel.hideRegisterCouponSheet() }
        )
    }

    if (state.showCloseSheet && state.row != null) {
        CloseFixedIncomeBottomSheet(
            position = state.row!!.position,
            preselectedCloseType = state.preselectedCloseType,
            onSave = { closeType, closeDate, event ->
                viewModel.closePosition(closeType, closeDate, event)
            },
            onDismiss = { viewModel.hideCloseSheet() }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CONTENT
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixedIncomeDetailContent(
    state: FixedIncomeDetailUiState,
    balancesHidden: Boolean,
    onBack: () -> Unit,
    onDeleteEvent: (FixedIncomeEvent) -> Unit,
    onUpdateRegionSector: (String?, String?) -> Unit,
    onShowCloseSheet: (es.aviferdev.n3to.domain.model.FixedIncomeCloseType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize().background(NavyDeep)) {
        Column(Modifier.fillMaxSize()) {
            TopBarApp(
                title = state.row?.position?.name ?: stringResource(Res.string.fixedincome_detail_title),
                navigateBack = onBack
            )

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else if (state.row == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(Res.string.fixedincome_not_found), color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                val row = state.row
                val position = row.position

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        FixedIncomeDetailHeader(
                            position = position,
                            row = row,
                            balancesHidden = balancesHidden,
                            simulation = state.maturitySimulation
                        )
                    }

                    if (position.interestFrequency != es.aviferdev.n3to.domain.model.InterestFrequency.AT_MATURITY && state.couponSchedule.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            CouponTimelineSection(
                                schedule = state.couponSchedule,
                                balancesHidden = balancesHidden
                            )
                        }
                    }

                    if (state.maturitySimulation != null) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            MaturitySimulatorCard(
                                simulation = state.maturitySimulation,
                                balancesHidden = balancesHidden
                            )
                        }
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                        EventsHistorySection(
                            events = state.events,
                            balancesHidden = balancesHidden,
                            onDeleteEvent = onDeleteEvent
                        )
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                        DistributionSection(
                            position = state.row.position,
                            onUpdateRegionSector = onUpdateRegionSector
                        )
                    }

                    val position = state.row.position
                    if (position.isOpen) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (position.isMatured) {
                                    Button(
                                        onClick = { onShowCloseSheet(es.aviferdev.n3to.domain.model.FixedIncomeCloseType.MATURITY) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = ExpenseRed.copy(alpha = 0.15f),
                                            contentColor = ExpenseRed
                                        ),
                                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.fixedincome_register_settlement),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    when {
                                        position.type.allowsEarlyCancellation -> {
                                            Button(
                                                onClick = { onShowCloseSheet(es.aviferdev.n3to.domain.model.FixedIncomeCloseType.EARLY_CANCELLATION) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ExpenseRed.copy(alpha = 0.15f),
                                                    contentColor = ExpenseRed
                                                ),
                                                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.fixedincome_cancel_title),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { onShowCloseSheet(es.aviferdev.n3to.domain.model.FixedIncomeCloseType.MATURITY) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, NavyBorder),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = CyanAccent
                                                )
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.fixedincome_liquidate_maturity),
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                        position.type.allowsSecondarySale -> {
                                            Button(
                                                onClick = { onShowCloseSheet(es.aviferdev.n3to.domain.model.FixedIncomeCloseType.SECONDARY_SALE) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ExpenseRed.copy(alpha = 0.15f),
                                                    contentColor = ExpenseRed
                                                ),
                                                border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.fixedincome_sell_secondary),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { onShowCloseSheet(es.aviferdev.n3to.domain.model.FixedIncomeCloseType.MATURITY) },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, NavyBorder),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = CyanAccent
                                                )
                                            ) {
                                                Text(
                                                    text = stringResource(Res.string.fixedincome_liquidate_maturity),
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// PREVIEW
// ═══════════════════════════════════════════════════════════════════════════════

@Preview
@Composable
fun FixedIncomeDetailContentPreview() {
    N3toTheme {
        FixedIncomeDetailContent(
            balancesHidden = false,
            state = FixedIncomeDetailUiState(
                isLoading = false,
                row = es.aviferdev.n3to.domain.model.FixedIncomeRow(
                    position = es.aviferdev.n3to.domain.model.FixedIncomePosition(
                        id = "fi-preview",
                        accountId = "acct-1",
                        name = "Depósito Ejemplo 3M",
                        ticker = "DEP-EJ3M",
                        type = es.aviferdev.n3to.domain.model.FixedIncomeType.DEPOSIT,
                        notes = null,
                        principal = 10000.0,
                        quantity = 1.0,
                        nominalPerUnit = 10000.0,
                        interestRate = 3.5,
                        interestFrequency = es.aviferdev.n3to.domain.model.InterestFrequency.AT_MATURITY,
                        startDate = 1700000000000,
                        maturityDate = 1700000000000 + 90L * 24L * 3600L * 1000L,
                        platformId = "",
                        issuerId = null,
                        region = "Europa",
                        sector = "Banca",
                        autoRenew = false,
                        archived = false,
                        closedAt = null,
                        closeType = null,
                        feeNote = null,
                        createdAt = 1700000000000
                    ),
                    collectedInterest = 0.0,
                    currentValue = 10000.0,
                    totalProfit = 0.0,
                    totalProfitPercent = 0.0
                ),
                events = emptyList(),
                couponSchedule = emptyList(),
                maturitySimulation = es.aviferdev.n3to.domain.portfolio.MaturitySimulation(
                    capitalInvested = 10000.0,
                    grossInterest = 87.5,
                    collectedCoupons = 0.0,
                    remainingInterest = 87.5,
                    estimatedIrpf = 16.62,
                    estimatedCommission = 0.0,
                    netAtMaturity = 10070.88,
                    netProfit = 70.88
                )
            ),
            onBack = {},
            onDeleteEvent = {},
            onUpdateRegionSector = { _, _ -> },
            onShowCloseSheet = {}
        )
    }
}
// ─── Distribution Section (Región y Sector) ───────────────────────────────────
@Composable
private fun DistributionSection(
    position: es.aviferdev.n3to.domain.model.FixedIncomePosition?,
    onUpdateRegionSector: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Valores predefinidos para región y sector
    val regions = listOf("Europa", "EE.UU.", "España", "Emerging Markets", "Global")
    val sectors = listOf("Gobierno", "Corporativo", "Banca", "Energía", "Inmobiliario", "Otro")

    var showRegionDialog by remember { mutableStateOf(false) }
    var showSectorDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_distribution_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            // Región
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.fixedincome_region_label), fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = position?.region ?: stringResource(Res.string.fixedincome_no_region),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.region != null) TextPrimary else TextTertiary
                    )
                }
                TextButton(onClick = { showRegionDialog = true }) {
                    Text(stringResource(Res.string.fixedincome_change_region), fontSize = 12.sp, color = PrimaryDark)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Sector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.fixedincome_sector_label), fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = position?.sector ?: stringResource(Res.string.fixedincome_no_sector),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.sector != null) TextPrimary else TextTertiary
                    )
                }
                TextButton(onClick = { showSectorDialog = true }) {
                    Text(stringResource(Res.string.fixedincome_change_sector), fontSize = 12.sp, color = PrimaryDark)
                }
            }
        }
    }

    // Diálogo para seleccionar región
    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = SurfaceWhite,
            title = { Text(stringResource(Res.string.fixedincome_select_region), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    regions.forEach { region ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(region, position?.sector)
                                showRegionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = region,
                                color = if (position?.region == region) PrimaryDark else TextPrimary,
                                fontWeight = if (position?.region == region) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) {
                    Text(stringResource(Res.string.common_cancel), color = TextSecondary)
                }
            }
        )
    }

    // Diálogo para seleccionar sector
    if (showSectorDialog) {
        AlertDialog(
            onDismissRequest = { showSectorDialog = false },
            containerColor = SurfaceWhite,
            title = { Text(stringResource(Res.string.fixedincome_select_sector), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    sectors.forEach { sector ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(position?.region, sector)
                                showSectorDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sector,
                                color = if (position?.sector == sector) PrimaryDark else TextPrimary,
                                fontWeight = if (position?.sector == sector) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSectorDialog = false }) {
                    Text(stringResource(Res.string.common_cancel), color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun FixedIncomeDetailHeader(
    position: es.aviferdev.n3to.domain.model.FixedIncomePosition,
    row: es.aviferdev.n3to.domain.model.FixedIncomeRow,
    balancesHidden: Boolean,
    simulation: es.aviferdev.n3to.domain.portfolio.MaturitySimulation? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(20.dp))
            .drawBehind {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(NavySurface, NavySurfaceLight),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                val orbRadius = 100.dp.toPx()
                val cx = size.width - 30.dp.toPx()
                val cy = 30.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyanGlow.copy(alpha = 0.14f), Color.Transparent),
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
            // Top row: label + amount + tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    val statusLabel = if (position.isOpen) stringResource(Res.string.fixedincome_status_active) else stringResource(Res.string.fixedincome_status_closed)
                    Text(
                        stringResource(Res.string.fixedincome_header_label, statusLabel),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = WarnAmber
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${maskAmount(formatAmount(position.principal), balancesHidden)} €",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.8).sp
                    )
                    Text(
                        stringResource(Res.string.fixedincome_nominal_label),
                        fontSize = 11.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
                // Tag
                StatusTag(
                    label = position.type.label.uppercase(),
                    color = WarnAmber
                )
            }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailCell(
                label = stringResource(Res.string.fixedincome_annual_coupon),
                value = "${formatPercent(position.interestRate)}% · ${maskAmount(formatAmount(position.principal * position.interestRate / 100.0), balancesHidden)} €",
                modifier = Modifier.weight(1f)
            )
            DetailCell(
                label = stringResource(Res.string.fixedincome_maturity_label),
                value = formatDate(position.maturityDate),
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

@Composable
private fun DetailCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(NavySurfaceLight)
            .padding(10.dp)
    ) {
        Text(label, fontSize = 10.sp, color = CyanAccent.copy(alpha = 0.7f))
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ─── Coupon Timeline ──────────────────────────────────────────────────────────

@Composable
private fun CouponTimelineSection(
    schedule: List<ScheduledCoupon>,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_coupon_calendar),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            schedule.forEachIndexed { index, coupon ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (coupon.isPaid) PositiveGreen else CyanAccent,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formatDate(coupon.date),
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (coupon.isPaid) "✅ ${stringResource(Res.string.fixedincome_coupon_paid)}" else "🔵 ${stringResource(Res.string.fixedincome_coupon_pending)}",
                                fontSize = 11.sp,
                                color = if (coupon.isPaid) PositiveGreen.copy(alpha = 0.8f) else CyanAccent.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Text(
                        text = "${maskAmount(formatAmount(coupon.grossAmount), balancesHidden)} €",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                if (index < schedule.lastIndex) {
                    HorizontalDivider(
                        color = NavyBorder,
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
            }
        }
    }
}

// ─── Maturity Simulator ───────────────────────────────────────────────────────

@Composable
private fun MaturitySimulatorCard(
    simulation: es.aviferdev.n3to.domain.portfolio.MaturitySimulation,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_maturity_simulation),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            SimulatorRow(
                label = stringResource(Res.string.fixedincome_invested_label),
                value = maskAmount(formatAmount(simulation.capitalInvested), balancesHidden)
            )
            SimulatorRow(
                label = stringResource(Res.string.fixedincome_gross_interest),
                value = "+ ${maskAmount(formatAmount(simulation.grossInterest), balancesHidden)}",
                valueColor = PnLPositive
            )
            SimulatorRow(
                label = stringResource(Res.string.fixedincome_coupons_received),
                value = "- ${maskAmount(formatAmount(simulation.collectedCoupons), balancesHidden)}",
                valueColor = Color.White.copy(alpha = 0.5f)
            )
            SimulatorRow(
                label = stringResource(Res.string.fixedincome_estimated_irpf, "19"),
                value = "- ${maskAmount(formatAmount(simulation.estimatedIrpf), balancesHidden)}",
                valueColor = PnLNegative
            )
            if (simulation.estimatedCommission > 0) {
                SimulatorRow(
                    label = stringResource(Res.string.fixedincome_estimated_commissions),
                    value = "- ${maskAmount(formatAmount(simulation.estimatedCommission), balancesHidden)}",
                    valueColor = PnLNegative
                )
            }

            HorizontalDivider(
                color = NavyBorder,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(Res.string.fixedincome_net_maturity),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "${maskAmount(formatAmount(simulation.netAtMaturity), balancesHidden)} €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
            }

            Spacer(Modifier.height(6.dp))

            val sign = if (simulation.netProfit >= 0) "+" else ""
            Text(
                text = "${stringResource(Res.string.fixedincome_net_profit_label)}: $sign${maskAmount(formatAmount(simulation.netProfit), balancesHidden)} €",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (simulation.netProfit >= 0) PnLPositive else PnLNegative
            )
        }
    }
}

@Composable
private fun SimulatorRow(
    label: String,
    value: String,
    valueColor: Color = Color.White.copy(alpha = 0.85f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.White.copy(alpha = 0.55f))
        Text(
            text = "$value €",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

// ─── Events History ───────────────────────────────────────────────────────────

@Composable
private fun EventsHistorySection(
    events: List<FixedIncomeEvent>,
    balancesHidden: Boolean,
    onDeleteEvent: (FixedIncomeEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(Res.string.fixedincome_event_history),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            if (events.isEmpty()) {
                Text(
                    text = stringResource(Res.string.fixedincome_no_events),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            } else {
                events.forEach { event ->
                    EventItem(
                        event = event,
                        balancesHidden = balancesHidden,
                        onDelete = { onDeleteEvent(event) }
                    )
                    if (events.last() != event) {
                        HorizontalDivider(
                            color = NavyBorder,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventItem(
    event: FixedIncomeEvent,
    balancesHidden: Boolean,
    onDelete: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = event.type.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = formatDate(event.date),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${maskAmount(formatAmount(event.netAmount), balancesHidden)} €",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (event.netAmount >= 0) PnLPositive else PnLNegative
                )
                val eventGrossText = stringResource(Res.string.fixedincome_gross_label)
                if (event.irpfPercent > 0 || event.commissionAmount > 0) {
                    Text(
                        text = "$eventGrossText: ${maskAmount(formatAmount(event.grossAmount), balancesHidden)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }

        if (event.irpfPercent > 0 || event.commissionAmount > 0) {
            Spacer(Modifier.height(4.dp))
            Row {
                if (event.irpfPercent > 0) {
                    Text(
                        text = "${stringResource(Res.string.fixedincome_retention_label)} ${formatPercent(event.irpfPercent)}%",
                        fontSize = 10.sp,
                        color = PnLNegative
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (event.commissionAmount > 0) {
                    Text(
                        text = "${stringResource(Res.string.fixedincome_commission_label)}: ${maskAmount(formatAmount(event.commissionAmount), balancesHidden)}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}

// ─── Distribution Section ─────────────────────────────────────────────────────

@Composable
private fun DistributionSection(
    position: es.aviferdev.n3to.domain.model.FixedIncomePosition?,
    onUpdateRegionSector: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val regions = listOf("Europa", "EE.UU.", "España", "Emerging Markets", "Global")
    val sectors = listOf("Gobierno", "Corporativo", "Banca", "Energía", "Inmobiliario", "Otro")

    var showRegionDialog by remember { mutableStateOf(false) }
    var showSectorDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(0.5.dp, NavyBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribución",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Región", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                    Text(
                        text = position?.region ?: "No asignada",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.region != null) Color.White else Color.White.copy(alpha = 0.35f)
                    )
                }
                TextButton(onClick = { showRegionDialog = true }) {
                    Text("Cambiar", fontSize = 12.sp, color = CyanAccent)
                }
            }

            HorizontalDivider(
                color = NavyBorder,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sector", fontSize = 11.sp, color = Color.White.copy(alpha = 0.45f))
                    Text(
                        text = position?.sector ?: "No asignado",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.sector != null) Color.White else Color.White.copy(alpha = 0.35f)
                    )
                }
                TextButton(onClick = { showSectorDialog = true }) {
                    Text("Cambiar", fontSize = 12.sp, color = CyanAccent)
                }
            }
        }
    }

    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = NavySurface,
            title = {
                Text(
                    "Seleccionar Región",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    regions.forEach { region ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(region, position?.sector)
                                showRegionDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = region,
                                color = if (position?.region == region) CyanAccent else Color.White.copy(alpha = 0.75f),
                                fontWeight = if (position?.region == region) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    if (showSectorDialog) {
        AlertDialog(
            onDismissRequest = { showSectorDialog = false },
            containerColor = NavySurface,
            title = {
                Text(
                    "Seleccionar Sector",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    sectors.forEach { sector ->
                        TextButton(
                            onClick = {
                                onUpdateRegionSector(position?.region, sector)
                                showSectorDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sector,
                                color = if (position?.sector == sector) CyanAccent else Color.White.copy(alpha = 0.75f),
                                fontWeight = if (position?.sector == sector) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSectorDialog = false }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}
