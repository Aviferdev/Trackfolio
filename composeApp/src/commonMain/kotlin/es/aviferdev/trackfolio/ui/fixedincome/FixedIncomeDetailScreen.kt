package es.aviferdev.trackfolio.ui.fixedincome

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.FixedIncomeEvent
import es.aviferdev.trackfolio.domain.portfolio.ScheduledCoupon
import es.aviferdev.trackfolio.ui.common.StatusTag
import es.aviferdev.trackfolio.ui.fixedincome.formatPercent1
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.theme.NegativeRed
import es.aviferdev.trackfolio.ui.theme.PositiveGreen
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import es.aviferdev.trackfolio.ui.theme.WarnAmber
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarnAmber,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            if (state.row?.position?.isOpen == true) {
                var fabMenuOpen by remember { mutableStateOf(false) }
                Box {
                    FloatingActionButton(
                        onClick = { fabMenuOpen = true },
                        containerColor = WarnAmber,
                        contentColor = Color.Black
                    ) {
                        Text("⚡", fontSize = 20.sp)
                    }
                    DropdownMenu(
                        expanded = fabMenuOpen,
                        onDismissRequest = { fabMenuOpen = false },
                        containerColor = SurfaceWhite
                    ) {
                        DropdownMenuItem(
                            text = { Text("Registrar cupón", color = TextPrimary) },
                            leadingIcon = { Text("💰", fontSize = 16.sp) },
                            onClick = {
                                fabMenuOpen = false
                                viewModel.showRegisterCouponSheet()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Liquidar posición", color = TextPrimary) },
                            leadingIcon = { Text("🔒", fontSize = 16.sp) },
                            onClick = {
                                fabMenuOpen = false
                                viewModel.showCloseSheet()
                            }
                        )
                    }
                }
            }
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

                // Sección de distribución (región y sector)
                item {
                    Spacer(Modifier.height(16.dp))
                    DistributionSection(
                        position = state.row?.position,
                        onUpdateRegionSector = { region, sector ->
                            viewModel.updateRegionAndSector(region, sector)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Botones de cierre contextual según el tipo de instrumento
                val position = state.row?.position
                if (position?.isOpen == true) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Si la posición ya está vencida, mostrar solo botón de liquidación
                            if (position.isMatured) {
                                Button(
                                    onClick = { viewModel.showCloseSheetWithType(es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType.MATURITY) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ExpenseRed.copy(alpha = 0.15f),
                                        contentColor = ExpenseRed
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = ExpenseRed.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Text(
                                        text = "Registrar liquidación",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            } else {
                                // Botones según el tipo de instrumento
                                when {
                                    // Depósitos: cancelación anticipada
                                    position.type.allowsEarlyCancellation -> {
                                        Button(
                                            onClick = { viewModel.showCloseSheetWithType(es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType.EARLY_CANCELLATION) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ExpenseRed.copy(alpha = 0.15f),
                                                contentColor = ExpenseRed
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = ExpenseRed.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Text(
                                                text = "Cancelar anticipadamente",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.showCloseSheetWithType(es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType.MATURITY) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "Liquidar al vencimiento",
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                    // Bonos/Letras/Obligaciones: venta en secundario
                                    position.type.allowsSecondarySale -> {
                                        Button(
                                            onClick = { viewModel.showCloseSheetWithType(es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType.SECONDARY_SALE) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = ExpenseRed.copy(alpha = 0.15f),
                                                contentColor = ExpenseRed
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = ExpenseRed.copy(alpha = 0.4f)
                                            )
                                        ) {
                                            Text(
                                                text = "Vender en mercado secundario",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.showCloseSheetWithType(es.aviferdev.trackfolio.domain.model.FixedIncomeCloseType.MATURITY) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
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

    // ── Dialogs ─────────────────────────────────────────────────────────────────
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

// ─── Distribution Section (Región y Sector) ───────────────────────────────────
@Composable
private fun DistributionSection(
    position: es.aviferdev.trackfolio.domain.model.FixedIncomePosition?,
    onUpdateRegionSector: (String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Valores predefinidos para región y sector
    val regions = listOf("Europa", "EE.UU.", "España", "Emerging Markets", "Global")
    val sectors = listOf("Gobierno", "Corporativo", "Banca", " Energía", "Inmobiliario", "Otro")

    var showRegionDialog by remember { mutableStateOf(false) }
    var showSectorDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribución",
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
                    Text("Región", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = position?.region ?: "No asignada",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.region != null) TextPrimary else TextTertiary
                    )
                }
                TextButton(onClick = { showRegionDialog = true }) {
                    Text("Cambiar", fontSize = 12.sp, color = PrimaryDark)
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
                    Text("Sector", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = position?.sector ?: "No asignado",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (position?.sector != null) TextPrimary else TextTertiary
                    )
                }
                TextButton(onClick = { showSectorDialog = true }) {
                    Text("Cambiar", fontSize = 12.sp, color = PrimaryDark)
                }
            }
        }
    }

    // Diálogo para seleccionar región
    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = SurfaceWhite,
            title = { Text("Seleccionar Región", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
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
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Diálogo para seleccionar sector
    if (showSectorDialog) {
        AlertDialog(
            onDismissRequest = { showSectorDialog = false },
            containerColor = SurfaceWhite,
            title = { Text("Seleccionar Sector", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
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
                    Text("Cancelar", color = TextSecondary)
                }
            }
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
    // Hero card with WarnAmber background (matching JSX design)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = WarnAmber.copy(alpha = 0.15f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WarnAmber.copy(alpha = 0.2f)))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Top row: label + amount + tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "Renta fija · ${if (position.isOpen) "ACTIVO" else "CERRADO"}",
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
                        "Nominal",
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

            Spacer(Modifier.height(14.dp))

            // 2x2 grid with details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cupón anual
                DetailCell(
                    label = "Cupón anual",
                    value = "${position.interestRate?.let { "${formatPercent1(it)}%" } ?: "—"} · ${maskAmount(formatAmount(position.principal * (position.interestRate ?: 0.0) / 100.0), balancesHidden)} €",
                    modifier = Modifier.weight(1f)
                )
                // Vencimiento
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
                // TIR estimada (placeholder - would need actual calculation)
                DetailCell(
                    label = "TIR estimada",
                    value = position.interestRate?.let { "${formatPercent1(it)}%" } ?: "—",
                    modifier = Modifier.weight(1f)
                )
                // Plataforma (placeholder)
                DetailCell(
                    label = "Plataforma",
                    value = position.platformId.ifEmpty { "—" },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DetailCell(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WarnAmber.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(label, fontSize = 10.sp, color = WarnAmber.copy(alpha = 0.7f))
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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