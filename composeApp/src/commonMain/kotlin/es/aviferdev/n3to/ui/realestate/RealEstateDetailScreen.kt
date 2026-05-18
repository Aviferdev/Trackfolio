package es.aviferdev.n3to.ui.realestate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
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
    var expenseCategories by remember { mutableStateOf(listOf<Category>()) }
    val getCategoriesByType: GetCategoriesByTypeUseCase = koinInject()

    // Cargar categorías para los bottom sheets
    LaunchedEffect(property) {
        if (property != null && expenseCategories.isEmpty()) {
            val cats = getCategoriesByType(property.accountId, TransactionType.EXPENSE).firstOrNull() ?: emptyList()
            expenseCategories = cats.filter { it.name != "Ajuste de saldo" }
        }
    }

    // Bottom Sheets
    if (state.showEditSheet && property != null) {
        AddEditPropertyBottomSheet(
            existingProperty = property,
            accountId = property.accountId,
            availableLoans = emptyList(),
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { prop, expenses -> viewModel.saveProperty(prop, expenses) }
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
    if (state.showSellSheet && property != null) {
        SellPropertySheet(
            propertyName = property.name,
            propertyId = property.id,
            categories = expenseCategories,
            onDismiss = { viewModel.hideSellSheet() },
            onConfirm = { saleDate, saleValue, expenses ->
                viewModel.sellProperty(saleDate, saleValue, expenses)
            }
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
                            DropdownMenuItem(
                                text = { Text("Editar", color = TextPrimary) },
                                onClick = { showMenu = false; viewModel.showEditSheet() },
                                leadingIcon = { Icon(Icons.Outlined.Edit, null, tint = TextSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Actualizar valor", color = TextPrimary) },
                                onClick = { showMenu = false; viewModel.showValueSheet() },
                                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.TrendingUp, null, tint = TextSecondary) }
                            )
                            DropdownMenuItem(
                                text = { Text("Cambiar estado alquiler", color = TextPrimary) },
                                onClick = { showMenu = false; viewModel.showChangeRentalStatusSheet() },
                                leadingIcon = { Icon(Icons.Outlined.SwapHoriz, null, tint = TextSecondary) }
                            )
                            // Solo mostrar "Vender" si NO está vendida
                            if (!property.isSold) {
                                HorizontalDivider(color = BorderGray)
                                DropdownMenuItem(
                                    text = { Text("Vender propiedad", color = IncomeGreen) },
                                    onClick = { showMenu = false; viewModel.showSellSheet() },
                                    leadingIcon = { Icon(Icons.Outlined.AttachMoney, null, tint = IncomeGreen) }
                                )
                            }
                            HorizontalDivider(color = BorderGray)
                            DropdownMenuItem(
                                text = { Text("Archivar", color = ExpenseRed) },
                                onClick = { showMenu = false; viewModel.showArchiveDialog() },
                                leadingIcon = { Icon(Icons.Outlined.Archive, null, tint = ExpenseRed) }
                            )
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

                // 3. Sección de venta (solo si vendida)
                if (property.isSold) {
                    SaleInfoSection(property = property)
                }

                // 4. Mortgage Reminder Banner
                if (state.showMortgageReminder) {
                    MortgageReminderBanner(
                        propertyName = property.name,
                        onAddMortgage = { /* abre LoanPickerSheet */ },
                        onDismiss = { viewModel.dismissMortgageReminderAction() }
                    )
                }

                // 5. Hipoteca vinculada
                MortgageSection(linkedLoan = state.linkedLoan, onNavigateToLoan = onNavigateToLoan)

                // 6. Resumen financiero
                financialSummary?.let { PropertyFinancialSummaryCard(summary = it) }

                // 7. Gastos de compra
                val purchaseExpenses = transactions.filter { it.id.startsWith("prop_pexp_${property.id}") }
                if (purchaseExpenses.isNotEmpty()) {
                    PurchaseExpensesCard(expenses = purchaseExpenses)
                }

                // 8. Gastos de venta (solo si vendida)
                val saleExpenses = transactions.filter { it.id.startsWith("prop_sexp_${property.id}") }
                if (saleExpenses.isNotEmpty()) {
                    SaleExpensesCard(expenses = saleExpenses)
                }

                // 9. Historial de alquiler
                RentalPeriodHistorySection(periods = rentalPeriods)

                // 10. Transacciones vinculadas
                val manualTransactions = transactions.filter { tx ->
                    !tx.id.startsWith("prop_buy_") &&
                    !tx.id.startsWith("prop_sell_") &&
                    !tx.id.startsWith("prop_pexp_") &&
                    !tx.id.startsWith("prop_sexp_")
                }
                if (manualTransactions.isNotEmpty()) {
                    PropertyTransactionsSection(transactions = manualTransactions)
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

// ── Secciones existentes ──────────────────────────────────────────────────────

@Composable
private fun HeaderSection(property: RealEstateProperty) {
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
                    label = when {
                        property.isSold -> "\u2705 Vendida"
                        property.rentalStatus == RentalStatus.RENTED -> "\uD83D\uDCB0 Alquilada"
                        property.rentalStatus == RentalStatus.VACANT -> "\uD83D\uDD12 Vacía"
                        else -> "\uD83C\uDFE0 Uso propio"
                    },
                    color = when {
                        property.isSold -> IncomeGreen
                        property.rentalStatus == RentalStatus.RENTED -> IncomeGreen
                        property.rentalStatus == RentalStatus.VACANT -> WarnAmber
                        else -> TextTertiary
                    }
                )
            }
        }
    }
}

@Composable
private fun ValueSection(property: RealEstateProperty, onUpdateValue: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Valor", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                if (!property.isSold) {
                    TextButton(onClick = onUpdateValue) { Text("Actualizar", color = PrimaryDark, fontSize = 12.sp) }
                }
            }
            Spacer(Modifier.height(4.dp))
            DataRow("Valor estimado", formatAmountEuro(property.currentEstimatedValue))
            DataRow("Valor de compra", formatAmountEuro(property.purchaseValue))
            DataRow("% de propiedad", "${formatPercent(property.ownershipPercentage)}%")

            if (property.isSold && property.saleValue != null) {
                HorizontalDivider(color = BorderGray2, modifier = Modifier.padding(vertical = 4.dp))
                DataRow("Precio de venta", formatAmountEuro(property.saleValue))
                val gain = property.realizedGain ?: 0.0
                val pct = property.realizedGainPercent ?: 0.0
                DataRow(
                    "Plusvalía realizada",
                    "${if (gain >= 0) "+" else ""}${formatAmountEuro(gain)} (${if (pct >= 0) "+" else ""}${formatPercent(pct)}%)"
                )
            }

            HorizontalDivider(color = BorderGray2, modifier = Modifier.padding(vertical = 4.dp))
            DataRow("Valor efectivo", formatAmountEuro(property.effectiveValue))
        }
    }
}

@Composable
private fun SaleInfoSection(property: RealEstateProperty) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\u2705", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("Propiedad vendida", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IncomeGreen)
            }
            Spacer(Modifier.height(8.dp))
            val saleDateStr = property.saleDate?.let { formatDetailDate(it) } ?: ""
            DataRow("Fecha de venta", saleDateStr)
        }
    }
}

@Composable
private fun PurchaseExpensesCard(expenses: List<es.aviferdev.n3to.domain.model.Transaction>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gastos de compra", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            expenses.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tx.categoryId ?: "Gasto",
                        fontSize = 12.sp, color = TextSecondary
                    )
                    Text(
                        "-${formatAmountEuro(kotlin.math.abs(tx.amount))}",
                        fontSize = 12.sp, color = ExpenseRed, fontWeight = FontWeight.Medium
                    )
                }
                tx.notes?.let { note ->
                    Text(note, fontSize = 10.sp, color = TextTertiary, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun SaleExpensesCard(expenses: List<es.aviferdev.n3to.domain.model.Transaction>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gastos de venta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            expenses.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tx.categoryId ?: "Gasto",
                        fontSize = 12.sp, color = TextSecondary
                    )
                    Text(
                        "-${formatAmountEuro(kotlin.math.abs(tx.amount))}",
                        fontSize = 12.sp, color = ExpenseRed, fontWeight = FontWeight.Medium
                    )
                }
                tx.notes?.let { note ->
                    Text(note, fontSize = 10.sp, color = TextTertiary, modifier = Modifier.padding(start = 4.dp))
                }
            }
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

private fun formatDetailDate(epochMillis: Long): String {
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val ld = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
