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
import es.aviferdev.trackfolio.domain.model.IncomeTaxType
import es.aviferdev.trackfolio.domain.model.TransactionType
import es.aviferdev.trackfolio.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.abs

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

            // ── Selector de tipo ──────────────────────────────────────────────
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

            // ── Importe ───────────────────────────────────────────────────────
            val isIncomeWithFiscal = viewModel.type == TransactionType.INCOME && viewModel.showFiscalFields
            val amountColor = if (viewModel.type == TransactionType.INCOME) IncomeGreen else ExpenseRed

            if (isIncomeWithFiscal) {
                // Modo fiscal: el usuario introduce bruto + %; el neto se calcula
                val net = viewModel.calculatedNet
                Text(
                    text       = if (net != null) "Neto: ${fmtAmt(net)} €" else "Introduce bruto e IRPF%",
                    fontSize   = 14.sp,
                    color      = if (net != null) IncomeGreen else TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                val prefix = if (viewModel.type == TransactionType.INCOME) "+ " else "− "
                AmountInput(
                    value         = viewModel.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    prefix        = prefix,
                    color         = amountColor
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // ── Categoría ─────────────────────────────────────────────────────
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

            // ── Sección IRPF (solo ingresos) ──────────────────────────────────
            if (viewModel.type == TransactionType.INCOME) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                // Toggle para mostrar/ocultar campos fiscales
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Información fiscal (IRPF)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Bruto, retención y tipo de rendimiento", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked         = viewModel.showFiscalFields,
                        onCheckedChange = { viewModel.onToggleFiscalFields(it) },
                        colors          = SwitchDefaults.colors(checkedThumbColor = SurfaceWhite, checkedTrackColor = PrimaryDark)
                    )
                }

                AnimatedVisibility(
                    visible = viewModel.showFiscalFields,
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth().padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Tipo de rendimiento
                        Text("Tipo de rendimiento", fontSize = 12.sp, color = TextSecondary)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            IncomeTaxType.entries.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { taxType ->
                                        TaxTypeChip(
                                            taxType  = taxType,
                                            selected = viewModel.selectedTaxType == taxType,
                                            onClick  = { viewModel.onTaxTypeChange(taxType) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    // Rellenar si la fila está incompleta
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }

                        // Importe bruto + % IRPF en la misma fila
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(modifier = Modifier.weight(2f)) {
                                Text("Importe bruto", fontSize = 12.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                FiscalTextField(
                                    value         = viewModel.grossAmount,
                                    onValueChange = { viewModel.onGrossAmountChange(it) },
                                    placeholder   = "0,00",
                                    suffix        = "€"
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("IRPF %", fontSize = 12.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                FiscalTextField(
                                    value         = viewModel.irpfPercent,
                                    onValueChange = { viewModel.onIrpfPercentChange(it) },
                                    placeholder   = "0",
                                    suffix        = "%"
                                )
                            }
                        }

                        // Resumen calculado
                        viewModel.calculatedNet?.let { net ->
                            val gross = viewModel.grossAmount.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val irpf  = gross - net
                            Surface(
                                shape  = RoundedCornerShape(10.dp),
                                color  = IncomeGreen.copy(alpha = 0.07f),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    FiscalSummaryItem("Bruto",  gross, "€", TextPrimary)
                                    FiscalSummaryItem("IRPF",   irpf,  "€", ExpenseRed)
                                    FiscalSummaryItem("Neto",   net,   "€", IncomeGreen)
                                }
                            }
                        }
                    }
                }
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

// ─── Componentes ─────────────────────────────────────────────────────────────

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
private fun TaxTypeChip(taxType: IncomeTaxType, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            Text(taxType.emoji, fontSize = 16.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text       = taxType.label,
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
private fun FiscalSummaryItem(label: String, value: Double, currency: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(
            text       = "${fmtAmt(value)} $currency",
            fontSize   = 13.sp,
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
