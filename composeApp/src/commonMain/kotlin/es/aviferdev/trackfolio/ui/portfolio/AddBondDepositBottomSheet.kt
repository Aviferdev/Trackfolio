package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock

/**
 * Sheet para registrar un rendimiento de bono o depósito vinculado a un activo.
 * Igual que dividendo pero con campo de comisiones adicional.
 * Neto = bruto - IRPF - comisiones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBondDepositBottomSheet(
    fixedAssetName: String? = null,
    allAssets: List<Asset> = emptyList(),
    currencyCode: String,
    onSave: (assetId: String?, grossAmount: Double, irpfPercent: Double, commissionAmount: Double, date: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val symbol = currencySymbol(currencyCode)
    val showAssetSelector = fixedAssetName == null && allAssets.isNotEmpty()

    var selectedAssetId   by remember { mutableStateOf(allAssets.firstOrNull()?.id) }
    var grossAmountText   by remember { mutableStateOf("") }
    var irpfPercentText   by remember { mutableStateOf("19") }
    var commissionText    by remember { mutableStateOf("") }
    var error             by remember { mutableStateOf<String?>(null) }

    val grossAmount      = grossAmountText.replace(',', '.').toDoubleOrNull()
    val irpfPercent      = irpfPercentText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val commissionAmount = commissionText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val netAmount = if (grossAmount != null && grossAmount > 0) {
        grossAmount - (grossAmount * irpfPercent / 100.0) - commissionAmount
    } else null

    val displayName = fixedAssetName ?: allAssets.find { it.id == selectedAssetId }?.name ?: ""

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
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text       = "\uD83D\uDCDC Rendimiento bono/dep\u00f3sito",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Medium,
                color      = TextPrimary
            )
            if (displayName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(text = displayName, fontSize = 13.sp, color = TextSecondary)
            }

            Spacer(Modifier.height(16.dp))

            // ── Selector de activo (solo desde PortfolioScreen) ─────────────
            if (showAssetSelector) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Activo", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allAssets) { asset ->
                            val isSel = asset.id == selectedAssetId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) PrimaryDark.copy(alpha = 0.1f) else Color.Transparent)
                                    .border(
                                        if (isSel) 1.5.dp else 0.5.dp,
                                        if (isSel) PrimaryDark else BorderGray,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedAssetId = asset.id }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = asset.ticker,
                                    fontSize   = 12.sp,
                                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isSel) PrimaryDark else TextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Neto calculado ──────────────────────────────────────────────
            if (netAmount != null) {
                Text(
                    text       = "+ ${formatAmount(netAmount)} $symbol",
                    fontSize   = 28.sp,
                    color      = IncomeGreen,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text     = "Introduce el importe bruto",
                    fontSize = 14.sp,
                    color    = TextSecondary
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BorderGray, thickness = 0.5.dp)
            Spacer(Modifier.height(16.dp))

            // ── Importe bruto ───────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Importe bruto", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value           = grossAmountText,
                    onValueChange   = { grossAmountText = it.filter { c -> c.isDigit() || c == ',' || c == '.' }; error = null },
                    placeholder     = { Text("0,00", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                    suffix          = { Text(symbol, color = TextSecondary, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(8.dp),
                    colors          = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
                    textStyle       = TextStyle(fontSize = 14.sp, color = TextPrimary)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Retención IRPF ──────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Retenci\u00f3n IRPF", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value           = irpfPercentText,
                    onValueChange   = { irpfPercentText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    placeholder     = { Text("19", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                    suffix          = { Text("%", color = TextSecondary, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(8.dp),
                    colors          = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
                    textStyle       = TextStyle(fontSize = 14.sp, color = TextPrimary)
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Comisiones ──────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Comisiones", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value           = commissionText,
                    onValueChange   = { commissionText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    placeholder     = { Text("0,00", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                    suffix          = { Text(symbol, color = TextSecondary, fontSize = 14.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine      = true,
                    modifier        = Modifier.fillMaxWidth(),
                    shape           = RoundedCornerShape(8.dp),
                    colors          = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray),
                    textStyle       = TextStyle(fontSize = 14.sp, color = TextPrimary)
                )
            }

            // ── Resumen ─────────────────────────────────────────────────────
            if (grossAmount != null && grossAmount > 0) {
                Spacer(Modifier.height(14.dp))
                val irpfAmount = grossAmount * irpfPercent / 100.0
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = IncomeGreen.copy(alpha = 0.07f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BondSummaryItem("Bruto", grossAmount, symbol, TextPrimary)
                        if (irpfAmount > 0) BondSummaryItem("IRPF", irpfAmount, symbol, ExpenseRed)
                        if (commissionAmount > 0) BondSummaryItem("Comisi\u00f3n", commissionAmount, symbol, Color(0xFFFF9800))
                        BondSummaryItem("Neto", netAmount ?: 0.0, symbol, IncomeGreen)
                    }
                }
            }

            // ── Error ───────────────────────────────────────────────────────
            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = ExpenseRed, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            // ── Botón guardar ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (showAssetSelector && selectedAssetId == null) {
                        error = "Selecciona un activo"
                        return@Button
                    }
                    val ga = grossAmountText.replace(',', '.').toDoubleOrNull()
                    if (ga == null || ga <= 0) {
                        error = "Introduce un importe bruto v\u00e1lido"
                        return@Button
                    }
                    val pct = irpfPercentText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val comm = commissionText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    val now = Clock.System.now().toEpochMilliseconds()
                    onSave(selectedAssetId, ga, pct, comm, now)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                Text("Registrar rendimiento", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun BondSummaryItem(label: String, value: Double, currency: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = TextSecondary)
        Text(
            text       = "${formatAmount(value)} $currency",
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = color
        )
    }
}
