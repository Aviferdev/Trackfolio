package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.ui.common.StatusTag
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.realestate_archive_confirm_msg
import n3to.composeapp.generated.resources.realestate_archive_confirm_title
import n3to.composeapp.generated.resources.realestate_archive_label
import n3to.composeapp.generated.resources.realestate_capital_gain_label
import n3to.composeapp.generated.resources.realestate_change_rental_status
import n3to.composeapp.generated.resources.realestate_detail_title
import n3to.composeapp.generated.resources.realestate_edit_label
import n3to.composeapp.generated.resources.realestate_effective_value_label
import n3to.composeapp.generated.resources.realestate_estimated_value_label
import n3to.composeapp.generated.resources.realestate_expense_default
import n3to.composeapp.generated.resources.realestate_lender_label
import n3to.composeapp.generated.resources.realestate_loan_label
import n3to.composeapp.generated.resources.realestate_monthly_payment_label
import n3to.composeapp.generated.resources.realestate_mortgage_hint
import n3to.composeapp.generated.resources.realestate_mortgage_label
import n3to.composeapp.generated.resources.realestate_no_mortgage
import n3to.composeapp.generated.resources.realestate_options_menu_cd
import n3to.composeapp.generated.resources.realestate_own_use_badge
import n3to.composeapp.generated.resources.realestate_ownership_percent_label
import n3to.composeapp.generated.resources.realestate_pending_capital_label
import n3to.composeapp.generated.resources.realestate_purchase_expenses
import n3to.composeapp.generated.resources.realestate_purchase_value_label
import n3to.composeapp.generated.resources.realestate_rented_badge
import n3to.composeapp.generated.resources.realestate_sale_date_label
import n3to.composeapp.generated.resources.realestate_sale_expenses
import n3to.composeapp.generated.resources.realestate_sale_price_label
import n3to.composeapp.generated.resources.realestate_sell_property
import n3to.composeapp.generated.resources.realestate_sold_badge
import n3to.composeapp.generated.resources.realestate_sold
import n3to.composeapp.generated.resources.realestate_update_label
import n3to.composeapp.generated.resources.realestate_update_value
import n3to.composeapp.generated.resources.realestate_vacant_badge
import n3to.composeapp.generated.resources.realestate_value_label
import n3to.composeapp.generated.resources.realestate_view_label
import n3to.composeapp.generated.resources.realestate_why_separate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
            title = stringResource(Res.string.realestate_archive_confirm_title),
            message = stringResource(Res.string.realestate_archive_confirm_msg),
            onConfirm = { viewModel.archivePropertyAction() },
            onDismiss = { viewModel.hideArchiveDialog() }
        )
    }
    if (state.showSellSheet && property != null) {
        SellPropertySheet(
            propertyName = property.name,
            propertyId = property.id,
            categories = state.expenseCategories,
            onDismiss = { viewModel.hideSellSheet() },
            onConfirm = { saleDate, saleValue, expenses ->
                viewModel.sellProperty(saleDate, saleValue, expenses)
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.background)
    ) {
        TopBarWithActionsApp(
            title = property?.name ?: stringResource(Res.string.realestate_detail_title),
            navigateBack = onNavigateBack,
            actions = {
                if (property != null) {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            stringResource(Res.string.realestate_options_menu_cd),
                            tint = MaterialTheme.appColors.textPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        containerColor = MaterialTheme.appColors.surfaceElevated
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.realestate_edit_label),
                                    color = MaterialTheme.appColors.textPrimary
                                )
                            },
                            onClick = { showMenu = false; viewModel.showEditSheet() },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Edit,
                                    null,
                                    tint = MaterialTheme.appColors.textSecondary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.realestate_update_value),
                                    color = MaterialTheme.appColors.textPrimary
                                )
                            },
                            onClick = { showMenu = false; viewModel.showValueSheet() },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.TrendingUp,
                                    null,
                                    tint = MaterialTheme.appColors.textSecondary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.realestate_change_rental_status),
                                    color = MaterialTheme.appColors.textPrimary
                                )
                            },
                            onClick = { showMenu = false; viewModel.showChangeRentalStatusSheet() },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.SwapHoriz,
                                    null,
                                    tint = MaterialTheme.appColors.textSecondary
                                )
                            }
                        )
                        // Solo mostrar "Vender" si NO está vendida
                        if (!property.isSold) {
                            HorizontalDivider(color = MaterialTheme.appColors.border)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(Res.string.realestate_sell_property),
                                        color = MaterialTheme.appColors.income
                                    )
                                },
                                onClick = { showMenu = false; viewModel.showSellSheet() },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.AttachMoney,
                                        null,
                                        tint = MaterialTheme.appColors.income
                                    )
                                }
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.appColors.border)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.realestate_archive_label),
                                    color = MaterialTheme.appColors.expense
                                )
                            },
                            onClick = { showMenu = false; viewModel.showArchiveDialog() },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Archive,
                                    null,
                                    tint = MaterialTheme.appColors.expense
                                )
                            }
                        )
                    }
                }
            }
        )

        if (state.isLoading || property == null) {
            Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.primary)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().weight(1f).verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Header
                HeaderSection(property = property)

                // Info: por qué los inmuebles están separados del portfolio
                WhySeparateInfoBanner()

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
                val purchaseExpenses =
                    transactions.filter { it.id.startsWith("prop_pexp_${property.id}") }
                if (purchaseExpenses.isNotEmpty()) {
                    PurchaseExpensesCard(expenses = purchaseExpenses)
                }

                // 8. Gastos de venta (solo si vendida)
                val saleExpenses =
                    transactions.filter { it.id.startsWith("prop_sexp_${property.id}") }
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
private fun WhySeparateInfoBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.appColors.primary.copy(alpha = 0.08f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = MaterialTheme.appColors.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            stringResource(Res.string.realestate_why_separate),
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun HeaderSection(property: RealEstateProperty) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(property.propertyType.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        property.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Text(
                        property.address,
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
                StatusTag(
                    label = when {
                        property.isSold -> "\u2705 ${stringResource(Res.string.realestate_sold_badge)}"
                        property.rentalStatus == RentalStatus.RENTED -> "\uD83D\uDCB0 ${
                            stringResource(
                                Res.string.realestate_rented_badge
                            )
                        }"

                        property.rentalStatus == RentalStatus.VACANT -> "\uD83D\uDD12 ${
                            stringResource(
                                Res.string.realestate_vacant_badge
                            )
                        }"

                        else -> "\uD83C\uDFE0 ${stringResource(Res.string.realestate_own_use_badge)}"
                    },
                    color = when {
                        property.isSold -> MaterialTheme.appColors.income
                        property.rentalStatus == RentalStatus.RENTED -> MaterialTheme.appColors.income
                        property.rentalStatus == RentalStatus.VACANT -> MaterialTheme.appColors.warnAmber
                        else -> MaterialTheme.appColors.textTertiary
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.realestate_value_label),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textPrimary
                )
                if (!property.isSold) {
                    TextButton(onClick = onUpdateValue) {
                        Text(
                            stringResource(Res.string.realestate_update_label),
                            color = MaterialTheme.appColors.primary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            DataRow(
                stringResource(Res.string.realestate_estimated_value_label),
                formatAmountEuro(property.currentEstimatedValue)
            )
            DataRow(
                stringResource(Res.string.realestate_purchase_value_label),
                formatAmountEuro(property.purchaseValue)
            )
            DataRow(
                stringResource(Res.string.realestate_ownership_percent_label),
                "${formatPercent(property.ownershipPercentage)}%"
            )

            if (property.isSold && property.saleValue != null) {
                HorizontalDivider(
                    color = MaterialTheme.appColors.border2,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                DataRow(
                    stringResource(Res.string.realestate_sale_price_label),
                    formatAmountEuro(property.saleValue)
                )
                val gain = property.realizedGain ?: 0.0
                val pct = property.realizedGainPercent ?: 0.0
                DataRow(
                    stringResource(Res.string.realestate_capital_gain_label),
                    "${if (gain >= 0) "+" else ""}${formatAmountEuro(gain)} (${if (pct >= 0) "+" else ""}${
                        formatPercent(
                            pct
                        )
                    }%)"
                )
            }

            HorizontalDivider(
                color = MaterialTheme.appColors.border2,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DataRow(
                stringResource(Res.string.realestate_effective_value_label),
                formatAmountEuro(property.effectiveValue)
            )
        }
    }
}

@Composable
private fun SaleInfoSection(property: RealEstateProperty) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.income.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("\u2705", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.realestate_sold),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.appColors.income
                )
            }
            Spacer(Modifier.height(8.dp))
            val saleDateStr = property.saleDate?.let { formatDetailDate(it) } ?: ""
            DataRow(stringResource(Res.string.realestate_sale_date_label), saleDateStr)
        }
    }
}

@Composable
private fun PurchaseExpensesCard(expenses: List<es.aviferdev.n3to.domain.model.Transaction>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(Res.string.realestate_purchase_expenses),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            expenses.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tx.categoryId ?: stringResource(Res.string.realestate_expense_default),
                        fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary
                    )
                    Text(
                        "-${formatAmountEuro(kotlin.math.abs(tx.amount))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.Medium
                    )
                }
                tx.notes?.let { note ->
                    Text(
                        note,
                        fontSize = 10.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SaleExpensesCard(expenses: List<es.aviferdev.n3to.domain.model.Transaction>) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(Res.string.realestate_sale_expenses),
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            expenses.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tx.categoryId ?: stringResource(Res.string.realestate_expense_default),
                        fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary
                    )
                    Text(
                        "-${formatAmountEuro(kotlin.math.abs(tx.amount))}",
                        fontSize = 12.sp,
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.Medium
                    )
                }
                tx.notes?.let { note ->
                    Text(
                        note,
                        fontSize = 10.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MortgageSection(
    linkedLoan: es.aviferdev.n3to.domain.model.Loan?,
    onNavigateToLoan: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (linkedLoan != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(Res.string.realestate_mortgage_label),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    TextButton(onClick = { onNavigateToLoan(linkedLoan.id) }) {
                        Text(
                            stringResource(
                                Res.string.realestate_view_label
                            ), color = MaterialTheme.appColors.primary, fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                DataRow(stringResource(Res.string.realestate_loan_label), linkedLoan.name)
                linkedLoan.lenderName?.let {
                    DataRow(
                        stringResource(Res.string.realestate_lender_label),
                        it
                    )
                }
                DataRow(
                    stringResource(Res.string.realestate_pending_capital_label),
                    formatAmountEuro(linkedLoan.outstandingPrincipal)
                )
                DataRow(
                    stringResource(Res.string.realestate_monthly_payment_label),
                    formatAmountEuro(linkedLoan.monthlyPayment)
                )
            } else {
                Text(
                    stringResource(Res.string.realestate_no_mortgage),
                    fontSize = 13.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
                Text(
                    stringResource(Res.string.realestate_mortgage_hint),
                    fontSize = 11.sp,
                    color = MaterialTheme.appColors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary)
        Text(
            value,
            fontSize = 13.sp,
            color = MaterialTheme.appColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
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
