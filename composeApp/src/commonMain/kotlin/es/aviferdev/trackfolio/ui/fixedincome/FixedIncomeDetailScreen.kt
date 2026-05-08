package es.aviferdev.trackfolio.ui.fixedincome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.model.FixedIncomeEventType
import es.aviferdev.trackfolio.domain.portfolio.ScheduledCoupon
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

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
    val currencyCode = "EUR"
    val balancesHidden = false
    val symbol = currencySymbol(currencyCode)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.row?.position?.name ?: "Posición de renta fija",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else if (state.row == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Posición no encontrada", color = TextSecondary)
            }
        } else {
            val row = state.row!!
            val position = row.position

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    FixedIncomeDetailHeader(
                        position = position,
                        row = row,
                        symbol = symbol,
                        balancesHidden = balancesHidden
                    )
                }

                if (position.interestFrequency != es.aviferdev.trackfolio.domain.model.InterestFrequency.AT_MATURITY && state.couponSchedule.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        CouponTimelineSection(
                            schedule = state.couponSchedule,
                            symbol = symbol,
                            balancesHidden = balancesHidden
                        )
                    }
                }

                if (state.maturitySimulation != null) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        MaturitySimulatorCard(
                            simulation = state.maturitySimulation!!,
                            symbol = symbol,
                            balancesHidden = balancesHidden
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    EventsHistorySection(
                        events = state.events,
                        symbol = symbol,
                        balancesHidden = balancesHidden,
                        onDeleteEvent = { event -> viewModel.showDeleteEventDialog(event) }
                    )
                }
            }
        }
    }

    if (state.showDeleteEventDialog && state.selectedEventForDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteEventDialog() },
            containerColor = SurfaceWhite,
            title = { Text("Eliminar evento", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary) },
            text = { Text("¿Eliminar el evento ${state.selectedEventForDelete!!.type.label}?", fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteEvent(state.selectedEventForDelete!!) }) {
                    Text("Eliminar", color = NegativeRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteEventDialog() }) {
                    Text("Cancelar", color = PrimaryDark)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FixedIncomeDetailHeader(
    position: es.aviferdev.trackfolio.domain.model.FixedIncomePosition,
    row: es.aviferdev.trackfolio.domain.model.FixedIncomeRow,
    symbol: String,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(position.type.emoji, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(position.type.label, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    if (position.ticker.isNotBlank()) {
                        Text(position.ticker, fontSize = 13.sp, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            FixedIncomeProgressBar(
                progress = position.progressPercent,
                color = when {
                    position.remainingDays <= 30 -> Color(0xFFE53935)
                    position.progressPercent > 0.75f -> Color(0xFFFF9800)
                    else -> Color(0xFF66BB6A)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDate(position.startDate),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
                val remainingDays = if (position.isOpen) position.remainingDays else 0
                Text(
                    text = if (position.isOpen) "$remainingDays días restantes" else "Cerrada",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = formatDate(position.maturityDate),
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Capital", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "${maskAmount(formatAmount(position.principal), balancesHidden)} $symbol",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Valor actual", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "${maskAmount(formatAmount(row.currentValue), balancesHidden)} $symbol",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("TAE", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "${formatPercent1(position.interestRate)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Frecuencia", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = position.interestFrequency.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Intereses devengados", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "+${maskAmount(formatAmount(position.accruedInterestToDate), balancesHidden)} $symbol",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF66BB6A)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Intereses cobrados", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "${maskAmount(formatAmount(row.collectedInterest), balancesHidden)} $symbol",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Rendimiento total", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    val pnlColor = if (row.totalProfit >= 0) Color(0xFF66BB6A) else Color(0xFFEF9A9A)
                    val sign = if (row.totalProfit >= 0) "+" else ""
                    Text(
                        text = "$sign${maskAmount(formatAmount(row.totalProfit), balancesHidden)} $symbol",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = pnlColor
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("% Rentabilidad", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(
                        text = "${formatPercent1(row.totalProfitPercent)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CouponTimelineSection(
    schedule: List<ScheduledCoupon>,
    symbol: String,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Calendario de cobros",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
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
                                    if (coupon.isPaid) PositiveGreen else PrimaryDark,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = formatDate(coupon.date),
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (coupon.isPaid) "✅ Cobrado" else "🔵 Pendiente",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = "${maskAmount(formatAmount(coupon.grossAmount), balancesHidden)} $symbol",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                if (index < schedule.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        modifier = Modifier.padding(start = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MaturitySimulatorCard(
    simulation: es.aviferdev.trackfolio.domain.portfolio.MaturitySimulation,
    symbol: String,
    balancesHidden: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Simulación de vencimiento",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            SimulatorRow(
                label = "Capital invertido",
                value = maskAmount(formatAmount(simulation.capitalInvested), balancesHidden),
                symbol = symbol
            )
            SimulatorRow(
                label = "Intereses brutos",
                value = "+ ${maskAmount(formatAmount(simulation.grossInterest), balancesHidden)}",
                symbol = symbol,
                valueColor = PositiveGreen
            )
            SimulatorRow(
                label = "Cupones ya cobrados",
                value = "- ${maskAmount(formatAmount(simulation.collectedCoupons), balancesHidden)}",
                symbol = symbol,
                valueColor = TextSecondary
            )
            SimulatorRow(
                label = "IRPF estimado (19%)",
                value = "- ${maskAmount(formatAmount(simulation.estimatedIrpf), balancesHidden)}",
                symbol = symbol,
                valueColor = NegativeRed
            )
            if (simulation.estimatedCommission > 0) {
                SimulatorRow(
                    label = "Comisiones estimadas",
                    value = "- ${maskAmount(formatAmount(simulation.estimatedCommission), balancesHidden)}",
                    symbol = symbol,
                    valueColor = NegativeRed
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Neto estimado al vencimiento",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = "${maskAmount(formatAmount(simulation.netAtMaturity), balancesHidden)} $symbol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryDark
                )
            }

            Spacer(Modifier.height(8.dp))

            val sign = if (simulation.netProfit >= 0) "+" else ""
            Text(
                text = "Beneficio neto total: $sign${maskAmount(formatAmount(simulation.netProfit), balancesHidden)} $symbol",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (simulation.netProfit >= 0) PositiveGreen else NegativeRed
            )
        }
    }
}

@Composable
private fun SimulatorRow(
    label: String,
    value: String,
    symbol: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextSecondary)
        Text(
            text = "$value $symbol",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun EventsHistorySection(
    events: List<FixedIncomeEvent>,
    symbol: String,
    balancesHidden: Boolean,
    onDeleteEvent: (FixedIncomeEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Historial de eventos",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            if (events.isEmpty()) {
                Text(
                    text = "Sin eventos registrados",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                events.forEach { event ->
                    EventItem(
                        event = event,
                        symbol = symbol,
                        balancesHidden = balancesHidden,
                        onDelete = { onDeleteEvent(event) }
                    )
                    if (events.last() != event) {
                        HorizontalDivider(
                            color = Color(0xFFE0E0E0),
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
    symbol: String,
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
                    color = TextPrimary
                )
                Text(
                    text = formatDate(event.date),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${maskAmount(formatAmount(event.netAmount), balancesHidden)} $symbol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (event.netAmount >= 0) PositiveGreen else NegativeRed
                )
                if (event.irpfPercent > 0 || event.commissionAmount > 0) {
                    Text(
                        text = "Bruto: ${maskAmount(formatAmount(event.grossAmount), balancesHidden)}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (event.irpfPercent > 0 || event.commissionAmount > 0) {
            Spacer(Modifier.height(4.dp))
            Row {
                if (event.irpfPercent > 0) {
                    Text(
                        text = "IRPF ${formatPercent1(event.irpfPercent)}%",
                        fontSize = 10.sp,
                        color = NegativeRed
                    )
                    Spacer(Modifier.width(8.dp))
                }
                if (event.commissionAmount > 0) {
                    Text(
                        text = "Comisión: ${maskAmount(formatAmount(event.commissionAmount), balancesHidden)}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}