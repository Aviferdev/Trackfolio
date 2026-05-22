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
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.realestate.components.DataRow
import es.aviferdev.n3to.ui.realestate.components.HeaderSection
import es.aviferdev.n3to.ui.realestate.components.MortgageSection
import es.aviferdev.n3to.ui.realestate.components.PurchaseExpensesCard
import es.aviferdev.n3to.ui.realestate.components.SaleExpensesCard
import es.aviferdev.n3to.ui.realestate.components.SaleInfoSection
import es.aviferdev.n3to.ui.realestate.components.ValueSection
import es.aviferdev.n3to.ui.realestate.components.WhySeparateInfoBanner
import es.aviferdev.n3to.ui.theme.*
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

// ── Secciones extraídas a realestate/components/ ─────────────────────────────
