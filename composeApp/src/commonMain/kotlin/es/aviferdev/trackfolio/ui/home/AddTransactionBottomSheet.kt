package es.aviferdev.trackfolio.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import es.aviferdev.trackfolio.domain.model.IncomeType
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.theme.*
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
                text       = if (viewModel.isEditing) "Editar movimiento" else "Nuevo movimiento",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary
            )

            Spacer(Modifier.height(16.dp))

            // ── Selector de tipo (Ingreso / Gasto) ───────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TypeChip(
                    label         = "Ingreso",
                    selected      = viewModel.type == TransactionType.INCOME,
                    selectedColor = IncomeGreen,
                    onClick       = { viewModel.onTypeChange(TransactionType.INCOME) }
                )
                TypeChip(
                    label         = "Gasto",
                    selected      = viewModel.type == TransactionType.EXPENSE,
                    selectedColor = ExpenseRed,
                    onClick       = { viewModel.onTypeChange(TransactionType.EXPENSE) }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (viewModel.type == TransactionType.EXPENSE) {
                ExpenseForm(viewModel)
            } else {
                IncomeForm(viewModel)
            }

            // ── Nota ──────────────────────────────────────────────────────────
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Nota (opcional)", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = viewModel.notes,
                    onValueChange = { viewModel.onNotesChange(it) },
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
                onClick  = { viewModel.save() },
                enabled  = viewModel.isValid && uiState !is AddTransactionUiState.Loading,
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
                        if (viewModel.isEditing) "Guardar cambios" else "Guardar",
                        fontSize = 16.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ─── Formulario de GASTO ─────────────────────────────────────────────────────

@Composable
private fun ExpenseForm(viewModel: AddTransactionViewModel) {
    AmountInput(
        value         = viewModel.amount,
        onValueChange = { viewModel.onAmountChange(it) },
        prefix        = "− ",
        color         = ExpenseRed
    )

    Spacer(Modifier.height(16.dp))
    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
    Spacer(Modifier.height(16.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Categoría", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.categories) { category ->
                CategoryChip(
                    label    = category.name,
                    selected = category.id == viewModel.selectedCategoryId,
                    onClick  = { viewModel.onCategoryChange(category.id) }
                )
            }
        }
    }
}

// ─── Formulario de INGRESO ───────────────────────────────────────────────────

@Composable
private fun IncomeForm(viewModel: AddTransactionViewModel) {
    // Neto calculado o placeholder
    val net = viewModel.calculatedNet
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        IncomeType.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { incomeType ->
                    IncomeTypeChip(
                        incomeType = incomeType,
                        selected   = viewModel.selectedIncomeType == incomeType,
                        onClick    = { viewModel.onIncomeTypeChange(incomeType) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }

    // ── Campos fiscales dinámicos ─────────────────────────────────────────
    val selectedType = viewModel.selectedIncomeType
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
                            value         = viewModel.grossAmount,
                            onValueChange = { viewModel.onGrossAmountChange(it) },
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
                            value         = viewModel.grossAmount,
                            onValueChange = { viewModel.onGrossAmountChange(it) },
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
                                value         = viewModel.socialSecurityAmount,
                                onValueChange = { viewModel.onSocialSecurityChange(it) },
                                placeholder   = "0,00",
                                suffix        = "€"
                            )
                        }
                    }

                    // IRPF
                    if (selectedType.hasIrpf) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Retención IRPF", fontSize = 12.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            FiscalTextField(
                                value         = viewModel.irpfPercent,
                                onValueChange = { viewModel.onIrpfPercentChange(it) },
                                placeholder   = "0",
                                suffix        = "%"
                            )
                        }
                    }

                    // Comisiones (solo bonos/depósitos)
                    if (selectedType.hasCommission) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Comisiones", fontSize = 12.sp, color = TextSecondary)
                            Spacer(Modifier.height(4.dp))
                            FiscalTextField(
                                value         = viewModel.commissionAmount,
                                onValueChange = { viewModel.onCommissionChange(it) },
                                placeholder   = "0,00",
                                suffix        = "€"
                            )
                        }
                    }

                    // Resumen calculado
                    viewModel.calculatedNet?.let { netValue ->
                        val gross = viewModel.grossAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val irpfPct = viewModel.irpfPercent.replace(',', '.').toDoubleOrNull() ?: 0.0
                        val ss   = if (selectedType.hasSocialSecurity) viewModel.socialSecurityAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
                        val comm = if (selectedType.hasCommission) viewModel.commissionAmount.replace(',', '.').toDoubleOrNull() ?: 0.0 else 0.0
                        val irpfBase = if (selectedType == IncomeType.SALARY) gross - ss else gross
                        val irpf = irpfBase * irpfPct / 100.0

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
                if (selectedType.issuerType != null) {
                    HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                    IssuerSection(
                        issuerTypeLabel = selectedType.issuerType.label,
                        issuers         = viewModel.issuers,
                        selectedId      = viewModel.selectedIssuerId,
                        onSelect        = { viewModel.onIssuerSelected(it) },
                        showNewField    = viewModel.showNewIssuerField,
                        onToggleNew     = { viewModel.onNewIssuerToggle() },
                        newName         = viewModel.newIssuerName,
                        onNewNameChange = { viewModel.onNewIssuerNameChange(it) }
                    )
                }
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
    onSelect: (String) -> Unit,
    showNewField: Boolean,
    onToggleNew: () -> Unit,
    newName: String,
    onNewNameChange: (String) -> Unit
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
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (showNewField) PrimaryDark.copy(alpha = 0.1f) else Color.Transparent)
                            .border(0.5.dp, if (showNewField) PrimaryDark else BorderGray, RoundedCornerShape(50.dp))
                            .clickable { onToggleNew() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Add, "Nuevo", modifier = Modifier.size(16.dp), tint = if (showNewField) PrimaryDark else TextSecondary)
                            Spacer(Modifier.width(4.dp))
                            Text("Nuevo", fontSize = 13.sp, color = if (showNewField) PrimaryDark else TextSecondary)
                        }
                    }
                }
            }
        } else {
            // Sin emisores existentes → mostrar campo directamente
            if (!showNewField) {
                OutlinedButton(
                    onClick = onToggleNew,
                    shape   = RoundedCornerShape(8.dp),
                    border  = ButtonDefaults.outlinedButtonBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, "Nuevo", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Añadir $issuerTypeLabel")
                }
            }
        }

        AnimatedVisibility(
            visible = showNewField || issuers.isEmpty(),
            enter   = expandVertically(),
            exit    = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                OutlinedTextField(
                    value         = newName,
                    onValueChange = onNewNameChange,
                    placeholder   = { Text("Nombre de $issuerTypeLabel", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(8.dp),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
                    singleLine    = true
                )
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
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) SurfaceWhite else Color.Transparent)
            .border(if (selected) 1.5.dp else 0.5.dp, if (selected) PrimaryDark else BorderGray, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) TextPrimary else TextSecondary)
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
