package es.aviferdev.n3to.ui.valuable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.SectionHeader
import es.aviferdev.n3to.ui.common.dialog.DeleteConfirmDialog
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.formatAmountEuro
import es.aviferdev.n3to.ui.valuable.components.DetailRow
import es.aviferdev.n3to.ui.valuable.components.MetricItem
import es.aviferdev.n3to.ui.valuable.components.ValuableHeaderCard
import es.aviferdev.n3to.ui.valuable.components.formatDate
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValuableDetailScreen(
    valuableId: String,
    onNavigateBack: () -> Unit,
    viewModel: ValuableDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val summary = uiState.summary

    if (uiState.showEditSheet && summary != null) {
        AddEditValuableBottomSheet(
            existingValuable = summary.valuable,
            existingPurchaseExpenses = emptyList(), // simplified: would need to pass from state
            existingHoldingExpenses = emptyList(),
            accountId = summary.valuable.accountId,
            onDismiss = { viewModel.hideEditSheet() },
            onSave = { valuable, purchaseExpenses, holdingExpenses ->
                viewModel.saveValuable(valuable, purchaseExpenses, holdingExpenses)
            }
        )
    }

    if (uiState.showSellSheet && summary != null) {
        SellValuableBottomSheet(
            valuableId = summary.valuable.id,
            valuableName = summary.valuable.name,
            onDismiss = { viewModel.hideSellSheet() },
            onConfirm = { saleDate, salePrice, expenses ->
                viewModel.sellValuable(saleDate, salePrice, expenses)
            }
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteConfirmDialog(
            title = stringResource(Res.string.valuable_delete_title),
            message = stringResource(Res.string.valuable_delete_message),
            onConfirm = { viewModel.deleteValuable() },
            onDismiss = { viewModel.hideDeleteDialog() }
        )
    }

    // ── Loan picker dialog ───────────────────────────────────────────────
    if (uiState.showLoanPicker && summary != null) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLoanPicker() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.valuable_link_loan_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                if (uiState.availableLoans.isEmpty()) {
                    Text(
                        stringResource(Res.string.valuable_no_loans),
                        color = MaterialTheme.appColors.textTertiary,
                        fontSize = 14.sp
                    )
                } else {
                    Column {
                        uiState.availableLoans.forEach { loan ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.linkLoan(loan.id)
                                        viewModel.hideLoanPicker()
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    loan.name,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.appColors.textPrimary
                                )
                                Text(
                                    formatAmountEuro(loan.outstandingPrincipal),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.appColors.textSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.hideLoanPicker() }) {
                    Text(
                        stringResource(Res.string.common_close),
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
            }
        )
    }

    if (uiState.showValueDialog && summary != null) {
        var valueText by remember { mutableStateOf(summary.valuable.currentValue.toString()) }
        AlertDialog(
            onDismissRequest = { viewModel.hideValueDialog() },
            containerColor = MaterialTheme.appColors.navySurface,
            title = {
                Text(
                    stringResource(Res.string.valuable_update_value_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { valueText = it },
                    label = { Text(stringResource(Res.string.valuable_new_value_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.income,
                        unfocusedBorderColor = MaterialTheme.appColors.border,
                        focusedTextColor = MaterialTheme.appColors.textPrimary,
                        unfocusedTextColor = MaterialTheme.appColors.textPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    valueText.toDoubleOrNull()?.let { viewModel.updateEstimatedValue(it) }
                    viewModel.hideValueDialog()
                }) {
                    Text(
                        stringResource(Res.string.common_update),
                        color = MaterialTheme.appColors.income
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideValueDialog() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.textTertiary
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(summary?.valuable?.name ?: stringResource(Res.string.valuable_detail_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.valuable_back_cd)
                        )
                    }
                },
                actions = {
                    if (summary != null) {
                        if (!summary.valuable.isSold) {
                            IconButton(onClick = { viewModel.showSellSheet() }) {
                                Icon(
                                    Icons.Outlined.Sell,
                                    contentDescription = stringResource(Res.string.valuable_sell_cd)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.showEditSheet() }) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = stringResource(Res.string.common_edit)
                            )
                        }
                        IconButton(onClick = { viewModel.showDeleteDialog() }) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = stringResource(Res.string.common_delete)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.appColors.navyDeep,
                    titleContentColor = MaterialTheme.appColors.textPrimary
                )
            )
        },
        containerColor = MaterialTheme.appColors.navyDeep
    ) { padding ->
        if (summary == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.appColors.income)
            }
        } else {
            val valuable = summary.valuable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Hero card ──────────────────────────────────────────────
                ValuableHeaderCard(summary = summary)

                // ── Información general ────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionHeader(stringResource(Res.string.valuable_information_section))
                        Spacer(Modifier.height(8.dp))
                        DetailRow(stringResource(Res.string.common_name), valuable.name)
                        if (valuable.description.isNotBlank()) {
                            DetailRow("Descripción", valuable.description)
                        }
                        DetailRow(stringResource(Res.string.valuable_purchase_date_short), formatDate(valuable.purchaseDate))
                        DetailRow(stringResource(Res.string.valuable_purchase_price_short), formatAmountEuro(valuable.purchasePrice))
                        if (!valuable.isSold) {
                            DetailRow(stringResource(Res.string.valuable_current_value), formatAmountEuro(valuable.currentValue))
                        }
                        if (valuable.isSold) {
                            DetailRow(stringResource(Res.string.valuable_sale_date_short), formatDate(valuable.saleDate ?: 0L))
                            DetailRow(stringResource(Res.string.valuable_sale_price_short), formatAmountEuro(valuable.salePrice ?: 0.0))
                        }
                        if (valuable.linkedLoanId != null) {
                            DetailRow(
                                stringResource(Res.string.valuable_linked_loan),
                                summary.linkedLoan?.name ?: "ID: ${valuable.linkedLoanId}"
                            )
                        }
                        if (valuable.notes != null) {
                            DetailRow(stringResource(Res.string.valuable_notes), valuable.notes)
                        }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(
                            color = MaterialTheme.appColors.border,
                            thickness = 0.5.dp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!valuable.isSold) {
                                OutlinedButton(
                                    onClick = { viewModel.showValueDialog() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.appColors.primary)
                                ) {
                                    Text(
                                        stringResource(Res.string.valuable_update_value_btn),
                                        fontSize = 11.sp
                                    )
                                }
                                OutlinedButton(
                                    onClick = { viewModel.showLoanPicker() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.appColors.primary)
                                ) {
                                    Text(
                                        if (valuable.linkedLoanId != null) stringResource(Res.string.valuable_change_loan) else stringResource(
                                            Res.string.valuable_link_loan_title
                                        ),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Gastos ─────────────────────────────────────────────────
                if (summary.totalExpenses > 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.navySurface),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            SectionHeader(stringResource(Res.string.valuable_expenses_section))
                            Spacer(Modifier.height(8.dp))
                            if (summary.purchaseExpenses > 0) {
                                DetailRow(
                                    stringResource(Res.string.valuable_purchase_expenses_short),
                                    formatAmountEuro(summary.purchaseExpenses)
                                )
                            }
                            if (summary.holdingExpenses > 0) {
                                DetailRow(
                                    stringResource(Res.string.valuable_holding_expenses_short),
                                    formatAmountEuro(summary.holdingExpenses)
                                )
                            }
                            if (summary.saleExpenses > 0) {
                                DetailRow(stringResource(Res.string.valuable_sale_expenses_short), formatAmountEuro(summary.saleExpenses))
                            }
                            HorizontalDivider(
                                color = MaterialTheme.appColors.border,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            DetailRow(stringResource(Res.string.valuable_total_expenses), formatAmountEuro(summary.totalExpenses))
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Secciones extraídas a valuable/components/ ────────────────────────────────
