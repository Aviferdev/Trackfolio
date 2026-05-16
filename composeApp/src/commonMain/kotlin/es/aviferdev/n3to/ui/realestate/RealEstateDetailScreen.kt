package es.aviferdev.n3to.ui.realestate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealEstateDetailScreen(
    propertyId: String,
    onNavigateBack: () -> Unit,
    onNavigateToLoan: (String) -> Unit,
    viewModel: RealEstateDetailViewModel = koinViewModel(
        key = "re_$propertyId",
        parameters = { parametersOf(propertyId) }
    )
) {
    val state by viewModel.uiState.collectAsState()
    val rentalPeriods by viewModel.rentalPeriods.collectAsState()
    val transactions by viewModel.linkedTransactions.collectAsState()
    val financialSummary by viewModel.financialSummary.collectAsState()

    val property = state.property

    // Bottom Sheets
    if (state.showEditSheet && property != null) {
        AddEditPropertyBottomSheet(
            existingProperty = property,
            accountId = property.accountId,
            availableLoans = emptyList(),
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { viewModel.saveProperty(it) }
        )
    }
    if (state.showValueSheet && property != null) {
        UpdatePropertyValueSheet(
            property = property,
            onDismiss = { viewModel.hideValueSheet() },
            onUpdate = { viewModel.updateValue(it) }
        )
    }
    if (state.showChangeRentalStatusSheet && property != null) {
        ChangeRentalStatusSheet(
            currentStatus = property.rentalStatus,
            onDismiss = { viewModel.hideChangeRentalStatusSheet() },
            onConfirm = { status, date, rent -> viewModel.changeStatus(status, date, rent) }
        )
    }
    if (state.showArchiveDialog) {
        DeleteConfirmDialog(
            title = "Archivar propiedad",
            message = "¿Estás seguro de que quieres archivar esta propiedad?\nSe ocultará de la pantalla de patrimonio.",
            onConfirm = { viewModel.archivePropertyAction() },
            onDismiss = { viewModel.hideArchiveDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property?.name ?: "Detalle propiedad", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = TextPrimary) } },
                actions = {
                    if (property != null) {
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Outlined.MoreVert, "Opciones", tint = TextPrimary) }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = SurfaceElevated) {
                            DropdownMenuItem(text = { Text("Editar", color = TextPrimary) }, onClick = { showMenu = false; viewModel.showEditSheet() }, leadingIcon = { Icon(Icons.Outlined.Edit, null, tint = TextSecondary) })
                            DropdownMenuItem(text = { Text("Actualizar valor", color = TextPrimary) }, onClick = { showMenu = false; viewModel.showValueSheet() }, leadingIcon = { Icon(Icons.Outlined.TrendingUp, null, tint = TextSecondary) })
                            DropdownMenuItem(text = { Text("Cambiar estado alquiler", color = TextPrimary) }, onClick = { showMenu = false; viewModel.showChangeRentalStatusSheet() }, leadingIcon = { Icon(Icons.Outlined.SwapHoriz, null, tint = TextSecondary) })
                            HorizontalDivider(color = BorderGray)
                            DropdownMenuItem(text = { Text("Archivar", color = ExpenseRed) }, onClick = { showMenu = false; viewModel.showArchiveDialog() }, leadingIcon = { Icon(Icons.Outlined.Archive, null, tint = ExpenseRed) })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite, titleContentColor = TextPrimary)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (state.isLoading || property == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryDark)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Header
                HeaderSection(property = property)

                // 2. Valor
                ValueSection(property = property, onUpdateValue = { viewModel.showValueSheet() })

                // 3. Mortgage Reminder Banner
                if (state.showMortgageReminder) {
                    MortgageReminderBanner(
                        propertyName = property.name,
                        onAddMortgage = { /* abre LoanPickerSheet */ },
                        onDismiss = { viewModel.dismissMortgageReminderAction() }
                    )
                }

                // 4. Hipoteca vinculada
                MortgageSection(linkedLoan = state.linkedLoan, onNavigateToLoan = onNavigateToLoan)

                // 5. Resumen financiero
                financialSummary?.let { PropertyFinancialSummaryCard(summary = it) }

                // 6. Historial de alquiler
                RentalPeriodHistorySection(periods = rentalPeriods)

                // 7. Transacciones vinculadas
                PropertyTransactionsSection(transactions = transactions)

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun HeaderSection(property: es.aviferdev.n3to.domain.model.RealEstateProperty) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(property.propertyType.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(property.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    Text(property.address, fontSize = 12.sp, color = TextTertiary)
                }
                StatusTag(
                    label = when (property.rentalStatus) {
                        RentalStatus.RENTED -> "\uD83D\uDCB0 Alquilada"
                        RentalStatus.VACANT -> "\uD83D\uDD12 Vacía"
                        RentalStatus.OWN_USE -> "\uD83C\uDFE0 Uso propio"
                    },
                    color = when (property.rentalStatus) {
                        RentalStatus.RENTED -> IncomeGreen
                        RentalStatus.VACANT -> WarnAmber
                        RentalStatus.OWN_USE -> TextTertiary
                    }
                )
            }
        }
    }
}

@Composable
private fun ValueSection(property: es.aviferdev.n3to.domain.model.RealEstateProperty, onUpdateValue: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Valor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                TextButton(onClick = onUpdateValue) { Text("Actualizar", color = PrimaryDark, fontSize = 12.sp) }
            }
            Spacer(Modifier.height(4.dp))
            DataRow("Valor estimado", formatAmountEuro(property.currentEstimatedValue))
            DataRow("Valor de compra", formatAmountEuro(property.purchaseValue))
            DataRow("% de propiedad", "${formatPercent(property.ownershipPercentage)}%")
            HorizontalDivider(color = BorderGray2, modifier = Modifier.padding(vertical = 4.dp))
            DataRow("Valor efectivo", formatAmountEuro(property.effectiveValue))
        }
    }
}

@Composable
private fun MortgageSection(linkedLoan: es.aviferdev.n3to.domain.model.Loan?, onNavigateToLoan: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (linkedLoan != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hipoteca", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                    TextButton(onClick = { onNavigateToLoan(linkedLoan.id) }) { Text("Ver", color = PrimaryDark, fontSize = 12.sp) }
                }
                Spacer(Modifier.height(4.dp))
                DataRow("Préstamo", linkedLoan.name)
                linkedLoan.lenderName?.let { DataRow("Entidad", it) }
                DataRow("Capital pendiente", formatAmountEuro(linkedLoan.outstandingPrincipal))
                DataRow("Cuota mensual", formatAmountEuro(linkedLoan.monthlyPayment))
            } else {
                Text("Sin hipoteca vinculada", fontSize = 13.sp, color = TextTertiary)
                Text("Vincula una hipoteca para un cálculo preciso del patrimonio.", fontSize = 11.sp, color = TextTertiary)
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
