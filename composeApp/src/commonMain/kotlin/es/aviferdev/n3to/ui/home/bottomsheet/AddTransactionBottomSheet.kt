package es.aviferdev.n3to.ui.home.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.home.components.CalculatedNetRow
import es.aviferdev.n3to.ui.home.components.DarkAmountInput
import es.aviferdev.n3to.ui.home.components.DarkInlineField
import es.aviferdev.n3to.ui.home.components.DarkTappableRow
import es.aviferdev.n3to.ui.home.components.DarkTextField
import es.aviferdev.n3to.ui.home.components.DateRow
import es.aviferdev.n3to.ui.home.components.IrpfCompactField
import es.aviferdev.n3to.ui.home.components.IssuerSelector
import es.aviferdev.n3to.ui.home.components.ModeChip
import es.aviferdev.n3to.ui.home.components.TypePill
import es.aviferdev.n3to.ui.home.viewmodel.AddTransactionUiState
import es.aviferdev.n3to.ui.home.viewmodel.AddTransactionViewModel
import es.aviferdev.n3to.ui.home.viewmodel.IncomeInputMode
import es.aviferdev.n3to.ui.home.viewmodel.IrpfInputMode
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_amount_label
import n3to.composeapp.generated.resources.common_category_label
import n3to.composeapp.generated.resources.common_close
import n3to.composeapp.generated.resources.common_description_label
import n3to.composeapp.generated.resources.fiscal_commissions_short
import n3to.composeapp.generated.resources.portfolio_add_tx_gross
import n3to.composeapp.generated.resources.transaction_income_type_label
import n3to.composeapp.generated.resources.transaction_mode_fiscal
import n3to.composeapp.generated.resources.transaction_mode_net_only
import n3to.composeapp.generated.resources.transaction_net_amount_label
import n3to.composeapp.generated.resources.transaction_note_placeholder
import n3to.composeapp.generated.resources.transaction_type_expense
import n3to.composeapp.generated.resources.transaction_type_income
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

// ═══════════════════════════════════════════════════════════════════════════════
// Bottom Sheet wrapper
// ═══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    onRequestCategoryPicker: ((TransactionType) -> Unit)? = null,
    viewModel: AddTransactionViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AddTransactionUiState.Success) {
            onDismiss()
            viewModel.clear()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.border2)
            )
        }
    ) {
        AddTransactionSheetContent(
            onDismiss = onDismiss,
            uiState = uiState,
            isEditing = viewModel.isEditing,
            type = viewModel.type,
            onTypeChange = { viewModel.onTypeChange(it) },
            amount = viewModel.amount,
            onAmountChange = { viewModel.onAmountChange(it) },
            categories = viewModel.categories,
            selectedCategoryId = viewModel.selectedCategoryId,
            onCategoryChange = { viewModel.onCategoryChange(it) },
            selectedIncomeType = viewModel.selectedIncomeType,
            onIncomeTypeChange = { viewModel.onIncomeTypeChange(it) },
            calculatedNet = viewModel.calculatedNet,
            incomeInputMode = viewModel.incomeInputMode,
            onIncomeModeChange = { viewModel.onIncomeModeChange(it) },
            netAmount = viewModel.netAmount,
            onNetAmountChange = { viewModel.onNetAmountChange(it) },
            grossAmount = viewModel.grossAmount,
            onGrossAmountChange = { viewModel.onGrossAmountChange(it) },
            socialSecurityAmount = viewModel.socialSecurityAmount,
            onSocialSecurityChange = { viewModel.onSocialSecurityChange(it) },
            irpfInputMode = viewModel.irpfInputMode,
            onIrpfInputModeChange = { viewModel.onIrpfInputModeChange(it) },
            irpfPercent = viewModel.irpfPercent,
            onIrpfPercentChange = { viewModel.onIrpfPercentChange(it) },
            irpfFixedAmount = viewModel.irpfFixedAmount,
            onIrpfFixedAmountChange = { viewModel.onIrpfFixedAmountChange(it) },
            commissionAmount = viewModel.commissionAmount,
            onCommissionChange = { viewModel.onCommissionChange(it) },
            withholdingTaxLabel = viewModel.withholdingTaxLabel,
            socialContributionLabel = viewModel.socialContributionLabel,
            showWithholdingField = viewModel.showWithholdingField,
            showSocialContributionField = viewModel.showSocialContributionField,
            issuers = viewModel.issuers,
            selectedIssuerId = viewModel.selectedIssuerId,
            onIssuerSelected = { viewModel.onIssuerSelected(it) },
            notes = viewModel.notes,
            onNotesChange = { viewModel.onNotesChange(it) },
            dateMillis = viewModel.dateMillis,
            onDateChange = { viewModel.onDateChange(it) },
            isValid = viewModel.isValid,
            onSave = { viewModel.save() },
            onRequestCategoryPicker = onRequestCategoryPicker,
            onIncomeTypeTap = { onRequestCategoryPicker?.invoke(TransactionType.INCOME) }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Content
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AddTransactionSheetContent(
    onDismiss: () -> Unit,
    uiState: AddTransactionUiState,
    isEditing: Boolean,
    type: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: String,
    onCategoryChange: (String) -> Unit,
    selectedIncomeType: IncomeType?,
    onIncomeTypeChange: (IncomeType) -> Unit,
    calculatedNet: Double?,
    incomeInputMode: IncomeInputMode,
    onIncomeModeChange: (IncomeInputMode) -> Unit,
    netAmount: String,
    onNetAmountChange: (String) -> Unit,
    grossAmount: String,
    onGrossAmountChange: (String) -> Unit,
    socialSecurityAmount: String,
    onSocialSecurityChange: (String) -> Unit,
    irpfInputMode: IrpfInputMode,
    onIrpfInputModeChange: (IrpfInputMode) -> Unit,
    irpfPercent: String,
    onIrpfPercentChange: (String) -> Unit,
    irpfFixedAmount: String,
    onIrpfFixedAmountChange: (String) -> Unit,
    commissionAmount: String,
    onCommissionChange: (String) -> Unit,
    withholdingTaxLabel: String,
    socialContributionLabel: String,
    showWithholdingField: Boolean,
    showSocialContributionField: Boolean,
    issuers: List<Issuer>,
    selectedIssuerId: String?,
    onIssuerSelected: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    dateMillis: Long,
    onDateChange: (Long) -> Unit,
    isValid: Boolean,
    onSave: () -> Unit,
    onRequestCategoryPicker: ((TransactionType) -> Unit)?,
    onIncomeTypeTap: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Editar transacción" else "Nueva transacción",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(Res.string.common_close),
                    tint = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypePill(
                label = stringResource(Res.string.transaction_type_income),
                selected = type == TransactionType.INCOME,
                selectedColor = MaterialTheme.appColors.income,
                onClick = { onTypeChange(TransactionType.INCOME) },
                modifier = Modifier.weight(1f)
            )
            TypePill(
                label = stringResource(Res.string.transaction_type_expense),
                selected = type == TransactionType.EXPENSE,
                selectedColor = MaterialTheme.appColors.expense,
                onClick = { onTypeChange(TransactionType.EXPENSE) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (type == TransactionType.EXPENSE) {
            DarkAmountInput(
                value = amount,
                onValueChange = onAmountChange,
                label = stringResource(Res.string.common_amount_label),
                color = MaterialTheme.appColors.expense
            )
            Spacer(Modifier.height(16.dp))
        }

        if (type == TransactionType.EXPENSE) {
            val categoryName = categories.find { it.id == selectedCategoryId }?.name ?: ""
            DarkTappableRow(
                icon = Icons.Outlined.Folder,
                label = stringResource(Res.string.common_category_label),
                value = if (categoryName.isNotEmpty()) categoryName else "Seleccionar categoría…",
                onClick = { onRequestCategoryPicker?.invoke(TransactionType.EXPENSE) }
            )
        } else {
            DarkTappableRow(
                icon = Icons.Outlined.AccountBalance,
                label = stringResource(Res.string.transaction_income_type_label),
                value = selectedIncomeType?.label ?: "Seleccionar tipo…",
                onClick = onIncomeTypeTap
            )
        }

        Spacer(Modifier.height(12.dp))

        var showNotes by remember { mutableStateOf(notes.isNotEmpty()) }
        Column {
            DarkTappableRow(
                icon = Icons.Outlined.Description,
                label = stringResource(Res.string.common_description_label),
                value = if (notes.isNotEmpty()) notes else stringResource(Res.string.transaction_note_placeholder),
                onClick = { showNotes = !showNotes }
            )
            AnimatedVisibility(
                visible = showNotes,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    DarkTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        placeholder = stringResource(Res.string.transaction_note_placeholder)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        DateRow(dateMillis = dateMillis, onDateSelected = onDateChange)

        if (type == TransactionType.INCOME && selectedIncomeType != null) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.appColors.border, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            val incType = selectedIncomeType

            if (incType == IncomeType.EXEMPT_INCOME) {
                DarkAmountInput(
                    value = grossAmount,
                    onValueChange = onGrossAmountChange,
                    label = "Importe",
                    color = MaterialTheme.appColors.income
                )
                Spacer(Modifier.height(16.dp))
                IssuerSelector(
                    issuers = issuers,
                    selectedId = selectedIssuerId,
                    onSelect = onIssuerSelected,
                    issuerTypeLabel = incType.issuerLabel
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeChip(
                        selected = incomeInputMode == IncomeInputMode.FISCAL,
                        onClick = { onIncomeModeChange(IncomeInputMode.FISCAL) },
                        modifier = Modifier.weight(1f),
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(stringResource(Res.string.transaction_mode_fiscal))
                            }
                        }
                    )
                    ModeChip(
                        selected = incomeInputMode == IncomeInputMode.NET_ONLY,
                        onClick = { onIncomeModeChange(IncomeInputMode.NET_ONLY) },
                        modifier = Modifier.weight(1f),
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(stringResource(Res.string.transaction_mode_net_only))
                            }
                        }
                    )
                }

                Spacer(Modifier.height(14.dp))

                if (incomeInputMode == IncomeInputMode.NET_ONLY) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.appColors.warnAmber.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.appColors.warnAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Sin detalle fiscal: este ingreso no aparecerá desglosado en el informe",
                                fontSize = 11.sp,
                                color = MaterialTheme.appColors.warnAmber,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    DarkAmountInput(
                        value = netAmount,
                        onValueChange = onNetAmountChange,
                        label = stringResource(Res.string.transaction_net_amount_label),
                        color = MaterialTheme.appColors.income
                    )
                    Spacer(Modifier.height(16.dp))
                    IssuerSelector(
                        issuers = issuers,
                        selectedId = selectedIssuerId,
                        onSelect = onIssuerSelected,
                        issuerTypeLabel = incType.issuerLabel
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DarkInlineField(
                            label = stringResource(Res.string.portfolio_add_tx_gross),
                            value = grossAmount,
                            onValueChange = onGrossAmountChange,
                            placeholder = "0,00",
                            suffix = "€",
                            modifier = Modifier.weight(1f)
                        )
                        if (showWithholdingField) {
                            IrpfCompactField(
                                label = withholdingTaxLabel,
                                irpfInputMode = irpfInputMode,
                                onIrpfInputModeChange = onIrpfInputModeChange,
                                irpfPercent = irpfPercent,
                                onIrpfPercentChange = onIrpfPercentChange,
                                irpfFixedAmount = irpfFixedAmount,
                                onIrpfFixedAmountChange = onIrpfFixedAmountChange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (showSocialContributionField) {
                        Spacer(Modifier.height(10.dp))
                        DarkInlineField(
                            label = socialContributionLabel,
                            value = socialSecurityAmount,
                            onValueChange = onSocialSecurityChange,
                            placeholder = "0,00",
                            suffix = "€"
                        )
                    }

                    if (incType.hasCommission) {
                        Spacer(Modifier.height(10.dp))
                        DarkInlineField(
                            label = stringResource(Res.string.fiscal_commissions_short),
                            value = commissionAmount,
                            onValueChange = onCommissionChange,
                            placeholder = "0,00",
                            suffix = "€"
                        )
                    }

                    if (calculatedNet != null) {
                        Spacer(Modifier.height(12.dp))
                        CalculatedNetRow(net = calculatedNet)
                    }

                    Spacer(Modifier.height(10.dp))
                    IssuerSelector(
                        issuers = issuers,
                        selectedId = selectedIssuerId,
                        onSelect = onIssuerSelected,
                        issuerTypeLabel = incType.issuerLabel
                    )
                }
            }
        }

        if (uiState is AddTransactionUiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = uiState.message,
                color = MaterialTheme.appColors.expense,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onSave,
            enabled = isValid && uiState !is AddTransactionUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.appColors.primary,
                disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.38f)
            )
        ) {
            if (uiState is AddTransactionUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (isEditing) "Guardar cambios" else "Guardar transacción",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
