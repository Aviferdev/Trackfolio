package es.aviferdev.n3to.ui.fixedincome

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.fixedincome.components.CouponTimelineSection
import es.aviferdev.n3to.ui.fixedincome.components.DistributionSection
import es.aviferdev.n3to.ui.fixedincome.components.EventsHistorySection
import es.aviferdev.n3to.ui.fixedincome.components.FixedIncomeDetailHeader
import es.aviferdev.n3to.ui.fixedincome.components.MaturitySimulatorCard
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.fixedincome_cancel_title
import n3to.composeapp.generated.resources.fixedincome_delete_event
import n3to.composeapp.generated.resources.fixedincome_delete_event_confirm
import n3to.composeapp.generated.resources.fixedincome_detail_title
import n3to.composeapp.generated.resources.fixedincome_liquidate_maturity
import n3to.composeapp.generated.resources.fixedincome_not_found
import n3to.composeapp.generated.resources.fixedincome_register_settlement
import n3to.composeapp.generated.resources.fixedincome_sell_secondary
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
            viewModel.updateRegionAndSector(
                region,
                sector
            )
        },
        onShowCloseSheet = { viewModel.showCloseSheetWithType(it) }
    )

    if (state.showDeleteEventDialog && state.selectedEventForDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteEventDialog() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.fixedincome_delete_event),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    stringResource(
                        Res.string.fixedincome_delete_event_confirm,
                        state.selectedEventForDelete!!.type.label
                    ),
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(state.selectedEventForDelete!!) }) {
                    Text(
                        stringResource(Res.string.common_delete),
                        color = MaterialTheme.appColors.expense
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteEventDialog() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.cyanAccent
                    )
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
    Box(modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        Column(Modifier.fillMaxSize()) {
            TopBarWithActionsApp(
                title = state.row?.position?.name
                    ?: stringResource(Res.string.fixedincome_detail_title),
                navigateBack = onBack
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.appColors.cyanAccent)
                }
            } else if (state.row == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.fixedincome_not_found),
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
            } else {
                val row = state.row
                val position = row.position

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = LocalBottomNavPadding.current + 16.dp
                    )
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
                            onUpdateRegionSector = onUpdateRegionSector,
                            allRegions = state.allRegions,
                            allSectors = state.allSectors
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
                                            containerColor = MaterialTheme.appColors.expense.copy(
                                                alpha = 0.15f
                                            ),
                                            contentColor = MaterialTheme.appColors.expense
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            MaterialTheme.appColors.expense.copy(alpha = 0.4f)
                                        )
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
                                                    containerColor = MaterialTheme.appColors.expense.copy(
                                                        alpha = 0.15f
                                                    ),
                                                    contentColor = MaterialTheme.appColors.expense
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.appColors.expense.copy(alpha = 0.4f)
                                                )
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
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.appColors.navyBorder
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.appColors.cyanAccent
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
                                                    containerColor = MaterialTheme.appColors.expense.copy(
                                                        alpha = 0.15f
                                                    ),
                                                    contentColor = MaterialTheme.appColors.expense
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.appColors.expense.copy(alpha = 0.4f)
                                                )
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
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.appColors.navyBorder
                                                ),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.appColors.cyanAccent
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
                        regionId = null,
                        sectorId = null,
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
