package es.aviferdev.trackfolio.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Category
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
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
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
            )
        }
    ) {
        AddTransactionBottomSheetContent(
            uiState               = uiState,
            isEditing             = viewModel.isEditing,
            type                  = viewModel.type,
            onTypeChange          = { viewModel.onTypeChange(it) },
            amount                = viewModel.amount,
            onAmountChange        = { viewModel.onAmountChange(it) },
            categories            = viewModel.categories,
            selectedCategoryId    = viewModel.selectedCategoryId,
            onCategoryChange      = { viewModel.onCategoryChange(it) },
            calculatedNet         = viewModel.calculatedNet,
            selectedIncomeType    = viewModel.selectedIncomeType,
            onIncomeTypeChange    = { viewModel.onIncomeTypeChange(it) },
            grossAmount           = viewModel.grossAmount,
            onGrossAmountChange   = { viewModel.onGrossAmountChange(it) },
            socialSecurityAmount  = viewModel.socialSecurityAmount,
            onSocialSecurityChange = { viewModel.onSocialSecurityChange(it) },
            irpfInputMode         = viewModel.irpfInputMode,
            onIrpfInputModeChange = { viewModel.onIrpfInputModeChange(it) },
            irpfPercent           = viewModel.irpfPercent,
            onIrpfPercentChange   = { viewModel.onIrpfPercentChange(it) },
            irpfFixedAmount       = viewModel.irpfFixedAmount,
            onIrpfFixedAmountChange = { viewModel.onIrpfFixedAmountChange(it) },
            commissionAmount      = viewModel.commissionAmount,
            onCommissionChange    = { viewModel.onCommissionChange(it) },
            issuers               = viewModel.issuers,
            selectedIssuerId      = viewModel.selectedIssuerId,
            onIssuerSelected      = { viewModel.onIssuerSelected(it) },
            notes                 = viewModel.notes,
            onNotesChange         = { viewModel.onNotesChange(it) },
            dateMillis            = viewModel.dateMillis,
            onDateChange          = { viewModel.onDateChange(it) },
            isValid               = viewModel.isValid,
            onSave                = { viewModel.save() }
        )
    }
}

@Composable
fun AddTransactionBottomSheetContent(
    uiState: AddTransactionUiState,
    isEditing: Boolean,
    type: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: String,
    onCategoryChange: (String) -> Unit,
    calculatedNet: Double?,
    selectedIncomeType: IncomeType?,
    onIncomeTypeChange: (IncomeType) -> Unit,
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
    onSave: () -> Unit
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

        Text(
            text       = if (isEditing) "Editar movimiento" else "Nuevo movimiento",
            fontSize   = 16.sp,
            fontWeight = FontWeight.Medium,
            color      = TextPrimary
        )

        Spacer(Modifier.height(16.dp))

        // ── Selector de tipo (Ingreso / Gasto) ───────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TypeChip(
                label         = "Ingreso",
                selected      = type == TransactionType.INCOME,
                selectedColor = IncomeGreen,
                onClick       = { onTypeChange(TransactionType.INCOME) }
            )
            TypeChip(
                label         = "Gasto",
                selected      = type == TransactionType.EXPENSE,
                selectedColor = ExpenseRed,
                onClick       = { onTypeChange(TransactionType.EXPENSE) }
            )
        }

        Spacer(Modifier.height(16.dp))

        if (type == TransactionType.EXPENSE) {
            ExpenseForm(
                amount             = amount,
                onAmountChange     = onAmountChange,
                categories         = categories,
                selectedCategoryId = selectedCategoryId,
                onCategoryChange   = onCategoryChange
            )
        } else {
            IncomeForm(
                calculatedNet         = calculatedNet,
                selectedIncomeType    = selectedIncomeType,
                onIncomeTypeChange    = onIncomeTypeChange,
                grossAmount           = grossAmount,
                onGrossAmountChange   = onGrossAmountChange,
                socialSecurityAmount  = socialSecurityAmount,
                onSocialSecurityChange = onSocialSecurityChange,
                irpfInputMode         = irpfInputMode,
                onIrpfInputModeChange = onIrpfInputModeChange,
                irpfPercent           = irpfPercent,
                onIrpfPercentChange   = onIrpfPercentChange,
                irpfFixedAmount       = irpfFixedAmount,
                onIrpfFixedAmountChange = onIrpfFixedAmountChange,
                commissionAmount      = commissionAmount,
                onCommissionChange    = onCommissionChange,
                issuers               = issuers,
                selectedIssuerId      = selectedIssuerId,
                onIssuerSelected      = onIssuerSelected
            )
        }

        // ── Fecha ─────────────────────────────────────────────────────────
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))

        DateSelector(
            dateMillis     = dateMillis,
            onDateSelected = onDateChange
        )

        // ── Nota ──────────────────────────────────────────────────────────
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Nota (opcional)", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value         = notes,
                onValueChange = onNotesChange,
                placeholder   = { Text("Ej. Nómina enero", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(8.dp),
                colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
                singleLine    = true
            )
        }

        Spacer(Modifier.height(24.dp))

        if (uiState is AddTransactionUiState.Error) {
            Text(
                text     = (uiState as AddTransactionUiState.Error).message,
                color    = ExpenseRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick  = onSave,
            enabled  = isValid && uiState !is AddTransactionUiState.Loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = PrimaryDark,
                disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
            )
        ) {
            if (uiState is AddTransactionUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    if (isEditing) "Guardar cambios" else "Guardar",
                    fontSize = 16.sp, fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Formulario de GASTO ─────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExpenseForm(
    amount: String,
    onAmountChange: (String) -> Unit,
    categories: List<Category>,
    selectedCategoryId: String,
    onCategoryChange: (String) -> Unit
) {
    AmountInput(
        value         = amount,
        onValueChange = onAmountChange,
        prefix        = "− ",
        color         = ExpenseRed
    )

    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
    Spacer(Modifier.height(16.dp))

    // ── Selector de categoría con buscador condicional ─────────────────
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Categoría", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(10.dp))

        val allCategories = categories
        var searchQuery by remember { mutableStateOf("") }
        val showSearch = allCategories.size > 8

        AnimatedVisibility(
            visible = showSearch,
            enter   = expandVertically() + fadeIn(),
            exit    = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder   = {
                    Text(
                        "Buscar categoría…",
                        color    = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = "Buscar",
                        modifier           = Modifier.size(18.dp),
                        tint               = TextSecondary
                    )
                },
                singleLine = true,
                modifier   = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .height(48.dp),
                shape      = RoundedCornerShape(10.dp),
                textStyle  = TextStyle(fontSize = 13.sp, color = TextPrimary),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
        }

        val filtered = if (searchQuery.isBlank()) {
            allCategories
        } else {
            allCategories.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        FlowRow(
            modifier              = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            filtered.forEachIndexed { index, category ->
                val originalIndex = allCategories.indexOf(category)
                val accentColor   = CategoryPalette[originalIndex % CategoryPalette.size]

                CategoryChip(
                    label       = category.name,
                    selected    = category.id == selectedCategoryId,
                    accentColor = accentColor,
                    onClick     = { onCategoryChange(category.id) }
                )
            }
        }

        if (filtered.isEmpty() && searchQuery.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Sin resultados para \"$searchQuery\"",
                fontSize = 12.sp,
                color    = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Formulario de INGRESO ───────────────────────────────────────────────────

@Composable
private fun IncomeForm(
    calculatedNet: Double?,
    selectedIncomeType: IncomeType?,
    onIncomeTypeChange: (IncomeType) -> Unit,
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
    onIssuerSelected: (String) -> Unit
) {
    val net = calculatedNet
    if (net != null) {
        Text(
            text       = "Neto: ${fmtAmt(net)} €",
            fontSize   = 28.sp,
            color      = IncomeGreen,
            fontWeight = FontWeight.Bold
        )
    } else {
        Text(
            text     = "Selecciona un tipo de ingreso",
            fontSize = 14.sp,
            color    = TextSecondary
        )
    }

    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
    Spacer(Modifier.height(16.dp))

    // ── Selector de tipo de ingreso ───────────────────────────────────────
    Text("Tipo de ingreso", fontSize = 13.sp, color = TextSecondary)
    Spacer(Modifier.height(10.dp))
    val homeIncomeTypes = IncomeType.entries.filter { it != IncomeType.DIVIDEND && it != IncomeType.BOND_DEPOSIT }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        homeIncomeTypes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { incomeType ->
                    IncomeTypeChip(
                        incomeType = incomeType,
                        selected   = selectedIncomeType == incomeType,
                        onClick    = { onIncomeTypeChange(incomeType) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }

    // ── Campos fiscales dinámicos ─────────────────────────────────────────
    val selectedType = selectedIncomeType
    AnimatedVisibility(
        visible = selectedType != null,
        enter   = expandVertically(),
        exit    = shrinkVertically()
    ) {
        if (selectedType != null) {
            Column(
                modifier            = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)

                if (selectedType == IncomeType.EXEMPT_INCOME) {
                    // Solo importe
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Importe", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        FiscalTextField(
                            value         = grossAmount,
                            onValueChange = onGrossAmountChange,
                            placeholder   = "0,00",
                            suffix        = "€"
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IncomeGreen.copy(alpha = 0.07f)
                    ) {
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📋", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Este ingreso está exento de retenciones",
                                fontSize = 12.sp,
                                color    = TextSecondary
                            )
                        }
                    }
                } else {
                    // Importe bruto
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Importe bruto", fontSize = 12.sp, color = TextSecondary)
                        Spacer(Modifier.height(4.dp))
                        FiscalTextField(
                            value         = grossAmount,
                            onValueChange = onGrossAmountChange,
                            placeholder   = "0,00",
                            suffix        = "€"
                        )
                    }

                    // Cotizaciones SS (solo salario)
                    if (selectedType.hasSocialSecurity) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Cotizaciones Seg. Social", fontSize = 12.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            FiscalTextField(
                                value         = socialSecurityAmount,
                                onValueChange = onSocialSecurityChange,
                                placeholder   = "0,00",
                                suffix        = "€"
                            )
                        }
                    }

                    // IRPF
                    if (selectedType.hasIrpf) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Retención IRPF", fontSize = 12.sp, color = TextSecondary)
                            Spacer(Modifier.height(6.dp))

                            // ── Selector de modo: % o € ──────────────────────────
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

                            if (irpfInputMode == IrpfInputMode.PERCENT) {
                                FiscalTextField(
                                    value         = irpfPercent,
                                    onValueChange = onIrpfPercentChange,
                                    placeholder   = "0",
                                    suffix        = "%"
                                )
                            } else {
                                FiscalTextField(
                                    value         = irpfFixedAmount,
                                    onValueChange = onIrpfFixedAmountChange,
                                    placeholder   = "0,00",
                                    suffix        = "€"
                                )
                            }
                        }
                    }

                    // Comisiones (solo bonos/depósitos)
                    if (selectedType.hasCommission) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Comisiones", fontSize = 12.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            FiscalTextField(
                                value         = commissionAmount,
                                onValueChange = onCommissionChange,
                                placeholder   = "0,00",
                                suffix        = "€"
                            )
                        }
                    }

                    // Resumen calculado
                    calculatedNet?.let { netValue ->
                        val gross = grossAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val ss   = if (selectedType.hasSocialSecurity) socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
                        val comm = if (selectedType.hasCommission) commissionAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
                        val irpf = resolveIrpf(irpfInputMode, irpfPercent, irpfFixedAmount, gross, ssDeduction = if (selectedType == IncomeType.SALARY) ss else 0.0)

                        Surface(
                            shape  = RoundedCornerShape(10.dp),
                            color  = IncomeGreen.copy(alpha = 0.07f),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    FiscalSummaryItem("Bruto", gross, "€", TextPrimary)
                                    if (ss > 0) FiscalSummaryItem("Seg. Social", ss, "€", Color(0xFFFF9800))
                                    if (irpf > 0) FiscalSummaryItem("IRPF", irpf, "€", ExpenseRed)
                                    if (comm > 0) FiscalSummaryItem("Comisión", comm, "€", Color(0xFFFF9800))
                                    FiscalSummaryItem("Neto", netValue, "€", IncomeGreen)
                                }
                            }
                        }
                    }
                }

                // ── Emisor ────────────────────────────────────────────────────
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                IssuerSection(
                    issuerTypeLabel = selectedType.issuerType.label,
                    issuers         = issuers,
                    selectedId      = selectedIssuerId,
                    onSelect        = onIssuerSelected
                )
            }
        }
    }
}

// ─── Sección de emisor ───────────────────────────────────────────────────────

@Composable
private fun IssuerSection(
    issuerTypeLabel: String,
    issuers: List<Issuer>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(issuerTypeLabel, fontSize = 13.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))

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
                shape = RoundedCornerShape(8.dp),
                color = SurfaceElevated
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\u2139\uFE0F", fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Sin emisores. A\u00F1\u00E1delos en Ajustes.",
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                }
            }
        }
    }
}

// ─── Componentes reutilizables ───────────────────────────────────────────────

@Composable
private fun AmountInput(value: String, onValueChange: (String) -> Unit, prefix: String, color: Color) {
    BasicTextField(
        value           = value,
        onValueChange   = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle       = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.Transparent, textAlign = TextAlign.Center),
        decorationBox  = {
            Text(
                text      = if (value.isEmpty()) "${prefix}0,00 €" else "$prefix$value €",
                fontSize  = 48.sp,
                fontWeight= FontWeight.Bold,
                color     = if (value.isEmpty()) color.copy(alpha = 0.35f) else color,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FiscalTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, suffix: String) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(placeholder, color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
        suffix        = { Text(suffix, color = TextSecondary, fontSize = 14.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(8.dp),
        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
        textStyle     = TextStyle(fontSize = 14.sp, color = TextPrimary)
    )
}

@Composable
private fun IncomeTypeChip(incomeType: IncomeType, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) PrimaryDark.copy(alpha = 0.1f) else Color.Transparent)
            .border(if (selected) 1.5.dp else 0.5.dp, if (selected) PrimaryDark else BorderGray, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(incomeType.emoji, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text       = incomeType.label,
                fontSize   = 10.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected) PrimaryDark else TextSecondary,
                textAlign  = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun IssuerChip(name: String, icon: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) SurfaceWhite else Color.Transparent)
            .border(if (selected) 1.5.dp else 0.5.dp, if (selected) PrimaryDark else BorderGray, RoundedCornerShape(50.dp))
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

@Composable
private fun FiscalSummaryItem(label: String, value: Double, currency: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(
            text       = "${fmtAmt(value)} $currency",
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = color
        )
    }
}

@Composable
private fun TypeChip(label: String, selected: Boolean, selectedColor: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .border(1.dp, if (selected) selectedColor else BorderGray, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) Color.White else TextSecondary)
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SurfaceElevated else Color.Transparent)
            .border(
                width = if (selected) 1.5.dp else 0.5.dp,
                color = if (selected) PrimaryDark else BorderGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = label.firstOrNull()?.uppercase() ?: "?",
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color      = accentColor
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text       = label,
                fontSize   = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (selected) TextPrimary else TextSecondary
            )
        }
    }
}

private fun fmtAmt(v: Double): String {
    val sign   = if (v < 0) "-" else ""
    val absVal = kotlin.math.abs(v)
    val int_   = absVal.toLong()
    val frac   = ((absVal - int_) * 100 + 0.5).toLong().coerceIn(0, 99)
    val intStr = int_.toString().reversed().chunked(3).joinToString(".").reversed()
    return "$sign$intStr,${frac.toString().padStart(2, '0')}"
}

@Composable
private fun IrpfModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
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

private fun resolveIrpf(
    irpfInputMode: IrpfInputMode,
    irpfPercent: String,
    irpfFixedAmount: String,
    gross: Double,
    ssDeduction: Double = 0.0
): Double {
    return when (irpfInputMode) {
        IrpfInputMode.PERCENT -> {
            val pct = irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
            val base = gross - ssDeduction
            base * pct / 100.0
        }
        IrpfInputMode.AMOUNT -> {
            irpfFixedAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
        }
    }
}

// ─── Selector de fecha ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelector(
    dateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val instant  = Instant.fromEpochMilliseconds(dateMillis)
    val ld       = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    val months   = listOf(
        "enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre"
    )
    val dateText = "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Fecha", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value         = dateText,
            onValueChange = {},
            readOnly      = true,
            modifier      = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true },
            shape         = RoundedCornerShape(8.dp),
            trailingIcon  = {
                IconButton(onClick = { showPicker = true }) {
                    Icon(
                        Icons.Outlined.CalendarMonth,
                        contentDescription = "Seleccionar fecha",
                        tint = PrimaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            enabled       = false,
            colors        = OutlinedTextFieldDefaults.colors(
                disabledTextColor         = TextPrimary,
                disabledBorderColor       = BorderGray,
                disabledTrailingIconColor = PrimaryDark,
                disabledContainerColor    = Color.Transparent
            )
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton    = {
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
            colors = DatePickerDefaults.colors(
                containerColor = SurfaceWhite
            )
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

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun AddTransactionBottomSheetContentPreview() {
    TrackfolioTheme {
        AddTransactionBottomSheetContent(
            uiState               = AddTransactionUiState.Idle,
            isEditing             = false,
            type                  = TransactionType.EXPENSE,
            onTypeChange          = {},
            amount                = "45,50",
            onAmountChange        = {},
            categories            = listOf(
                Category("1", "Supermercado", TransactionType.EXPENSE, false),
                Category("2", "Restaurante", TransactionType.EXPENSE, false),
                Category("3", "Transporte", TransactionType.EXPENSE, false),
                Category("4", "Ocio", TransactionType.EXPENSE, false)
            ),
            selectedCategoryId    = "1",
            onCategoryChange      = {},
            calculatedNet         = null,
            selectedIncomeType    = null,
            onIncomeTypeChange    = {},
            grossAmount           = "",
            onGrossAmountChange   = {},
            socialSecurityAmount  = "",
            onSocialSecurityChange = {},
            irpfInputMode         = IrpfInputMode.PERCENT,
            onIrpfInputModeChange = {},
            irpfPercent           = "",
            onIrpfPercentChange   = {},
            irpfFixedAmount       = "",
            onIrpfFixedAmountChange = {},
            commissionAmount      = "",
            onCommissionChange    = {},
            issuers               = emptyList(),
            selectedIssuerId      = null,
            onIssuerSelected      = {},
            notes                 = "",
            onNotesChange         = {},
            dateMillis            = Clock.System.now().toEpochMilliseconds(),
            onDateChange          = {},
            isValid               = false,
            onSave                = {}
        )
    }
}
