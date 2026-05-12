package es.aviferdev.trackfolio.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
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
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = Color(0xFF141414),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2C2C2C))
            )
        }
    ) {
        AddTransactionSheetContent(
            onDismiss                = onDismiss,
            uiState                  = uiState,
            isEditing                = viewModel.isEditing,
            type                     = viewModel.type,
            onTypeChange             = { viewModel.onTypeChange(it) },
            amount                   = viewModel.amount,
            onAmountChange           = { viewModel.onAmountChange(it) },
            categories               = viewModel.categories,
            selectedCategoryId       = viewModel.selectedCategoryId,
            onCategoryChange         = { viewModel.onCategoryChange(it) },
            selectedIncomeType       = viewModel.selectedIncomeType,
            onIncomeTypeChange       = { viewModel.onIncomeTypeChange(it) },
            calculatedNet            = viewModel.calculatedNet,
            grossAmount              = viewModel.grossAmount,
            onGrossAmountChange      = { viewModel.onGrossAmountChange(it) },
            socialSecurityAmount     = viewModel.socialSecurityAmount,
            onSocialSecurityChange   = { viewModel.onSocialSecurityChange(it) },
            irpfInputMode            = viewModel.irpfInputMode,
            onIrpfInputModeChange    = { viewModel.onIrpfInputModeChange(it) },
            irpfPercent              = viewModel.irpfPercent,
            onIrpfPercentChange      = { viewModel.onIrpfPercentChange(it) },
            irpfFixedAmount          = viewModel.irpfFixedAmount,
            onIrpfFixedAmountChange  = { viewModel.onIrpfFixedAmountChange(it) },
            commissionAmount         = viewModel.commissionAmount,
            onCommissionChange       = { viewModel.onCommissionChange(it) },
            issuers                  = viewModel.issuers,
            selectedIssuerId         = viewModel.selectedIssuerId,
            onIssuerSelected         = { viewModel.onIssuerSelected(it) },
            notes                    = viewModel.notes,
            onNotesChange            = { viewModel.onNotesChange(it) },
            dateMillis               = viewModel.dateMillis,
            onDateChange             = { viewModel.onDateChange(it) },
            isValid                  = viewModel.isValid,
            onSave                   = { viewModel.save() },
            onRequestCategoryPicker  = onRequestCategoryPicker,
            onIncomeTypeTap          = { onRequestCategoryPicker?.invoke(TransactionType.INCOME) }
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
    categories: List<es.aviferdev.trackfolio.domain.model.Category>,
    selectedCategoryId: String,
    onCategoryChange: (String) -> Unit,
    selectedIncomeType: IncomeType?,
    onIncomeTypeChange: (IncomeType) -> Unit,
    calculatedNet: Double?,
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

        // ── Title + close button ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = if (isEditing) "Editar transacción" else "Nueva transacción",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary,
                modifier   = Modifier.weight(1f)
            )
            IconButton(
                onClick  = onDismiss,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Close,
                    contentDescription = "Cerrar",
                    tint               = TextSecondary,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Type toggle — full width, less rounded ───────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypePill(
                label         = "Ingreso",
                selected      = type == TransactionType.INCOME,
                selectedColor = IncomeGreen,
                onClick       = { onTypeChange(TransactionType.INCOME) },
                modifier      = Modifier.weight(1f)
            )
            TypePill(
                label         = "Gasto",
                selected      = type == TransactionType.EXPENSE,
                selectedColor = ExpenseRed,
                onClick       = { onTypeChange(TransactionType.EXPENSE) },
                modifier      = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Amount ───────────────────────────────────────────────────────
        DarkAmountInput(
            value         = amount,
            onValueChange = onAmountChange,
            label         = "Importe",
            color         = if (type == TransactionType.INCOME) IncomeGreen else ExpenseRed
        )

        Spacer(Modifier.height(16.dp))

        // ── Category / Income Type row ───────────────────────────────────
        if (type == TransactionType.EXPENSE) {
            val categoryName = categories.find { it.id == selectedCategoryId }?.name ?: ""
            DarkTappableRow(
                icon = Icons.Outlined.Folder,
                label = "Categoría",
                value = if (categoryName.isNotEmpty()) categoryName else "Seleccionar categoría…",
                onClick = { onRequestCategoryPicker?.invoke(TransactionType.EXPENSE) }
            )
        } else {
            DarkTappableRow(
                icon = Icons.Outlined.AccountBalance,
                label = "Tipo de ingreso",
                value = selectedIncomeType?.label ?: "Seleccionar tipo…",
                onClick = onIncomeTypeTap
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Description row ──────────────────────────────────────────────
        var showNotes by remember { mutableStateOf(notes.isNotEmpty()) }
        Column {
            DarkTappableRow(
                icon = Icons.Outlined.Description,
                label = "Descripción",
                value = if (notes.isNotEmpty()) notes else "Añadir nota…",
                onClick = { showNotes = !showNotes }
            )

            AnimatedVisibility(
                visible = showNotes,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    DarkTextField(
                        value         = notes,
                        onValueChange = onNotesChange,
                        placeholder   = "Añadir nota…"
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Date row ────────────────────────────────────────────────────
        DateRow(
            dateMillis = dateMillis,
            onDateSelected = onDateChange
        )

        // ── Income‑specific fields (when type selected) ──────────────────
        if (type == TransactionType.INCOME && selectedIncomeType != null) {
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            val incType = selectedIncomeType

            when (incType) {
                IncomeType.EXEMPT_INCOME -> {
                    DarkInlineField(
                        label       = "Importe",
                        value       = grossAmount,
                        onValueChange = onGrossAmountChange,
                        placeholder = "0,00",
                        suffix      = "€"
                    )
                }
                else -> {
                    DarkInlineField(
                        label       = "Importe bruto",
                        value       = grossAmount,
                        onValueChange = onGrossAmountChange,
                        placeholder = "0,00",
                        suffix      = "€"
                    )

                    if (incType.hasSocialSecurity) {
                        Spacer(Modifier.height(10.dp))
                        DarkInlineField(
                            label       = "Cotizaciones Seg. Social",
                            value       = socialSecurityAmount,
                            onValueChange = onSocialSecurityChange,
                            placeholder = "0,00",
                            suffix      = "€"
                        )
                    }

                    if (incType.hasIrpf) {
                        Spacer(Modifier.height(10.dp))
                        IrpfSection(
                            irpfInputMode      = irpfInputMode,
                            onIrpfInputModeChange = onIrpfInputModeChange,
                            irpfPercent        = irpfPercent,
                            onIrpfPercentChange  = onIrpfPercentChange,
                            irpfFixedAmount    = irpfFixedAmount,
                            onIrpfFixedAmountChange = onIrpfFixedAmountChange
                        )
                    }

                    if (incType.hasCommission) {
                        Spacer(Modifier.height(10.dp))
                        DarkInlineField(
                            label       = "Comisiones",
                            value       = commissionAmount,
                            onValueChange = onCommissionChange,
                            placeholder = "0,00",
                            suffix      = "€"
                        )
                    }

                    // Net calculated summary
                    if (calculatedNet != null) {
                        Spacer(Modifier.height(12.dp))
                        CalculatedNetRow(net = calculatedNet)
                    }

                    // Issuer
                    Spacer(Modifier.height(10.dp))
                    IssuerSelector(
                        issuers        = issuers,
                        selectedId     = selectedIssuerId,
                        onSelect       = onIssuerSelected,
                        issuerTypeLabel = incType.issuerType.label
                    )
                }
            }
        }

        // ── Error ───────────────────────────────────────────────────────
        if (uiState is AddTransactionUiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text(
                text     = (uiState as AddTransactionUiState.Error).message,
                color    = ExpenseRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Save button ─────────────────────────────────────────────────
        Button(
            onClick  = onSave,
            enabled  = isValid && uiState !is AddTransactionUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = PrimaryDark,
                disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
            )
        ) {
            if (uiState is AddTransactionUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color    = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (isEditing) "Guardar cambios" else "Guardar transacción",
                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Components
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun TypePill(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .border(1.dp, if (selected) selectedColor else BorderGray, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun DarkTappableRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrimaryAlpha),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint    = PrimaryDark,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                value,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = if (value.contains("…")) TextTertiary else TextPrimary
            )
        }

        Text(
            "›",
            fontSize = 20.sp,
            color    = TextTertiary
        )
    }
}

@Composable
private fun DarkAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    color: Color,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label.uppercase(),
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value           = value,
            onValueChange   = onValueChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle       = TextStyle(
                fontSize   = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = Color.Transparent,
                textAlign  = TextAlign.Center,
                letterSpacing = (-1).sp
            ),
            decorationBox = {
                Text(
                    text      = if (value.isEmpty()) "0,00 €" else "$value €",
                    fontSize  = 34.sp,
                    fontWeight= FontWeight.ExtraBold,
                    color     = if (value.isEmpty()) TextTertiary else color,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp,
                    lineHeight = 36.sp,
                    modifier  = Modifier.fillMaxWidth()
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        BasicTextField(
            value         = value,
            onValueChange = onValueChange,
            singleLine    = true,
            textStyle     = TextStyle(
                fontSize   = 13.sp,
                color      = TextPrimary
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 13.sp,
                        color    = TextTertiary
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRow(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    val instant = Instant.fromEpochMilliseconds(dateMillis)
    val ld      = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val months  = listOf(
        "enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre"
    )
    val dateText = "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"

    DarkTappableRow(
        icon    = Icons.Outlined.CalendarMonth,
        label   = "Fecha",
        value   = dateText,
        onClick = { showPicker = true }
    )

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedUtc ->
                        val selectedLocal = Instant.fromEpochMilliseconds(selectedUtc)
                            .toLocalDateTime(TimeZone.UTC).date
                        val localInstant = selectedLocal.atStartOfDayIn(TimeZone.currentSystemDefault())
                        onDateSelected(localInstant.toEpochMilliseconds())
                    }
                    showPicker = false
                }) {
                    Text("Aceptar", color = PrimaryDark, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceWhite)
        ) {
            DatePicker(
                state  = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryDark,
                    todayDateBorderColor      = PrimaryDark
                )
            )
        }
    }
}

@Composable
private fun DarkInlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suffix: String = "",
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value         = value,
                onValueChange = onValueChange,
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle     = TextStyle(
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.Transparent
                ),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                fontSize = 14.sp,
                                color    = TextTertiary
                            )
                        } else {
                            Text(
                                value,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color    = TextPrimary
                            )
                        }
                        inner()
                        if (suffix.isNotEmpty()) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                suffix,
                                fontSize   = 14.sp,
                                color      = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IrpfSection(
    irpfInputMode: IrpfInputMode,
    onIrpfInputModeChange: (IrpfInputMode) -> Unit,
    irpfPercent: String,
    onIrpfPercentChange: (String) -> Unit,
    irpfFixedAmount: String,
    onIrpfFixedAmountChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Retención IRPF",
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IrpfModeChip(
                label    = "%",
                selected = irpfInputMode == IrpfInputMode.PERCENT,
                onClick  = { onIrpfInputModeChange(IrpfInputMode.PERCENT) }
            )
            IrpfModeChip(
                label    = "€",
                selected = irpfInputMode == IrpfInputMode.AMOUNT,
                onClick  = { onIrpfInputModeChange(IrpfInputMode.AMOUNT) }
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value         = if (irpfInputMode == IrpfInputMode.PERCENT) irpfPercent else irpfFixedAmount,
                onValueChange = { if (irpfInputMode == IrpfInputMode.PERCENT) onIrpfPercentChange(it) else onIrpfFixedAmountChange(it) },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle     = TextStyle(
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.Transparent
                ),
                decorationBox = { inner ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val currentValue = if (irpfInputMode == IrpfInputMode.PERCENT) irpfPercent else irpfFixedAmount
                        if (currentValue.isEmpty()) {
                            Text(
                                if (irpfInputMode == IrpfInputMode.PERCENT) "0 %" else "0,00 €",
                                fontSize = 14.sp,
                                color    = TextTertiary
                            )
                        } else {
                            Text(
                                currentValue,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color    = TextPrimary
                            )
                        }
                        inner()
                        Text(
                            if (irpfInputMode == IrpfInputMode.PERCENT) "%" else "€",
                            fontSize   = 14.sp,
                            color      = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IrpfModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) PrimaryDark.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) PrimaryDark else BorderGray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) PrimaryDark else TextSecondary
        )
    }
}

@Composable
private fun CalculatedNetRow(net: Double) {
    val formatted = formatAmount(net)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        color    = IncomeGreen.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "Neto estimado",
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = IncomeGreen
            )
            Text(
                "$formatted €",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Bold,
                color      = IncomeGreen
            )
        }
    }
}

@Composable
private fun IssuerSelector(
    issuers: List<Issuer>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    issuerTypeLabel: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            issuerTypeLabel,
            fontSize   = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        if (issuers.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(issuers) { issuer ->
                    IssuerChip(
                        name     = issuer.name,
                        icon     = issuer.icon,
                        selected = issuer.id == selectedId,
                        onClick  = { onSelect(issuer.id) }
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                color    = SurfaceElevated
            ) {
                Text(
                    "Sin emisores. Añádelos en Ajustes.",
                    modifier  = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    fontSize  = 12.sp,
                    color     = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun IssuerChip(name: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) SurfaceWhite else Color.Transparent)
            .border(
                if (selected) 1.5.dp else 0.5.dp,
                if (selected) PrimaryDark else BorderGray,
                RoundedCornerShape(50.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                name,
                fontSize   = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected) TextPrimary else TextSecondary
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    val negative = amount < 0
    val abs = if (negative) -amount else amount
    val rounded = (abs * 100).toLong()
    val euros = rounded / 100
    val cents = rounded % 100
    val eurosStr = euros.toString().reversed().chunked(3).joinToString(".").reversed()
    return if (negative) "-$eurosStr,${cents.toString().padStart(2, '0')}" else "$eurosStr,${cents.toString().padStart(2, '0')}"
}
