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
import androidx.compose.ui.tooling.preview.Preview
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
                    "Eliminar evento",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "¿Eliminar el evento ${state.selectedEventForDelete!!.type.label}?",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(state.selectedEventForDelete!!) }) {
                    Text("Eliminar", color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteEventDialog() }) {
                    Text("Cancelar", color = CyanAccent)
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
                title = state.row?.position?.name ?: "Posición de renta fija",
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
                    Text("Posición no encontrada", color = Color.White.copy(alpha = 0.5f))
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
                                            text = "Registrar liquidación",
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
                                                    text = "Cancelar anticipadamente",
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
                                                    text = "Liquidar al vencimiento",
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
                                                    text = "Vender en mercado secundario",
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
                                                    text = "Liquidar al vencimiento",
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

// ─── Hero Header ──────────────────────────────────────────────────────────────

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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Renta fija · ${if (position.isOpen) "ACTIVO" else "CERRADO"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyanAccent.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${maskAmount(formatAmount(position.principal), balancesHidden)} €",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-1.2).sp
                )
                Text(
                    text = "Principal nominal",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
            StatusTag(
                label = position.type.label.uppercase(),
                color = CyanAccent
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailCell(
                label = "Cupón anual",
                value = "${formatPercent(position.interestRate)}% · ${maskAmount(formatAmount(position.principal * position.interestRate / 100.0), balancesHidden)} €",
                modifier = Modifier.weight(1f)
            )
            DetailCell(
                label = "Vencimiento",
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
                label = "TIR neta est.",
                value = "${formatPercent(tirNet)}%",
                modifier = Modifier.weight(1f)
            )
            DetailCell(
                label = "Plataforma",
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
                text = "Calendario de cobros",
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
                                text = if (coupon.isPaid) "Cobrado" else "Pendiente",
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
                text = "Simulación de vencimiento",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            SimulatorRow(
                label = "Capital invertido",
                value = maskAmount(formatAmount(simulation.capitalInvested), balancesHidden)
            )
            SimulatorRow(
                label = "Intereses brutos",
                value = "+ ${maskAmount(formatAmount(simulation.grossInterest), balancesHidden)}",
                valueColor = PnLPositive
            )
            SimulatorRow(
                label = "Cupones ya cobrados",
                value = "- ${maskAmount(formatAmount(simulation.collectedCoupons), balancesHidden)}",
                valueColor = Color.White.copy(alpha = 0.5f)
            )
            SimulatorRow(
                label = "Retención estimada (19%)",
                value = "- ${maskAmount(formatAmount(simulation.estimatedIrpf), balancesHidden)}",
                valueColor = PnLNegative
            )
            if (simulation.estimatedCommission > 0) {
                SimulatorRow(
                    label = "Comisiones estimadas",
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
                    text = "Neto al vencimiento",
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
                text = "Beneficio neto: $sign${maskAmount(formatAmount(simulation.netProfit), balancesHidden)} €",
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
                text = "Historial de eventos",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            if (events.isEmpty()) {
                Text(
                    text = "Sin eventos registrados",
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
                if (event.irpfPercent > 0 || event.commissionAmount > 0) {
                    Text(
                        text = "Bruto: ${maskAmount(formatAmount(event.grossAmount), balancesHidden)}",
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
                        text = "Retención ${formatPercent(event.irpfPercent)}%",
                        fontSize = 10.sp,
                        color = PnLNegative
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (event.commissionAmount > 0) {
                    Text(
                        text = "Comisión: ${maskAmount(formatAmount(event.commissionAmount), balancesHidden)}",
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
