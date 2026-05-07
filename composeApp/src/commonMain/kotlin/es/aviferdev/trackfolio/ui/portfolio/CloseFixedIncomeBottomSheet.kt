package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetTransaction
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Tipos de cierre para renta fija.
 */
enum class FixedIncomeCloseType(val label: String, val description: String) {
    MATURITY("Vencimiento", "El producto ha llegado a su fecha de vencimiento"),
    SECONDARY_SALE("Venta en secundario", "Venta del bono en el mercado secundario antes del vencimiento"),
    EARLY_CANCELLATION("Cancelación anticipada", "Cancelación anticipada del depósito antes del vencimiento")
}

/**
 * Sheet para liquidar, vender en secundario o cancelar anticipadamente
 * un bono o depósito bancario.
 *
 * - **Vencimiento (bono/depósito):** Se devuelve el nominal + intereses
 *   netos. Se registran bruto, IRPF y comisiones.
 * - **Venta en secundario (solo bonos):** Se vende a un precio de mercado
 *   que puede diferir del nominal. Genera P&L.
 * - **Cancelación anticipada (solo depósitos):** Se recupera el capital
 *   con posible penalización. Se indican intereses devengados y penalización.
 *
 * El resultado es un SELL en AssetTransaction + una Transaction de liquidez
 * en la cuenta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseFixedIncomeBottomSheet(
    asset: Asset,
    platforms: List<Platform>,
    assetTransactions: List<AssetTransaction>,
    currencyCode: String,
    onSave: (
        closeType: FixedIncomeCloseType,
        quantity: Double,
        salePrice: Double,
        grossInterest: Double,
        irpfPercent: Double,
        commissionAmount: Double,
        date: Long,
        platformId: String,
        notes: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val symbol = currencySymbol(currencyCode)
    val isBond = asset.isBond
    val isDeposit = asset.isDeposit

    // Tipos de cierre disponibles según el producto
    val closeTypes = remember(isBond, isDeposit) {
        when {
            isBond    -> listOf(FixedIncomeCloseType.MATURITY, FixedIncomeCloseType.SECONDARY_SALE)
            isDeposit -> listOf(FixedIncomeCloseType.MATURITY, FixedIncomeCloseType.EARLY_CANCELLATION)
            else      -> listOf(FixedIncomeCloseType.MATURITY)
        }
    }

    var closeType by remember { mutableStateOf(FixedIncomeCloseType.MATURITY) }
    var quantityText by remember { mutableStateOf("") }
    var salePriceText by remember { mutableStateOf("") }
    var grossInterestText by remember { mutableStateOf("") }
    var irpfPercentText by remember { mutableStateOf("19") }
    var commissionText by remember { mutableStateOf("") }
    var dateMillis by remember {
        mutableStateOf(asset.maturityDate ?: Clock.System.now().toEpochMilliseconds())
    }
    var platformId by remember { mutableStateOf(platforms.firstOrNull()?.id) }
    var notesText by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedQty = quantityText.replace(',', '.').toDoubleOrNull()
    val parsedSalePrice = salePriceText.replace(',', '.').toDoubleOrNull()
    val parsedGrossInterest = grossInterestText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val parsedIrpf = irpfPercentText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val parsedCommission = commissionText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val now = Clock.System.now().toEpochMilliseconds()

    // Disponible para vender
    val availableQty = remember(assetTransactions, dateMillis) {
        PortfolioCalculator.availableQuantityAt(
            transactions = assetTransactions,
            asOfDate = dateMillis
        )
    }

    // Para depósitos, siempre se liquida todo (cantidad = lo que haya)
    LaunchedEffect(closeType, availableQty) {
        if (isDeposit || closeType == FixedIncomeCloseType.MATURITY) {
            quantityText = if (availableQty > 0) formatQty(availableQty) else ""
        }
    }

    // Cálculos de resumen
    val irpfOnInterest = parsedGrossInterest * parsedIrpf / 100.0
    val netInterest = parsedGrossInterest - irpfOnInterest - parsedCommission
    val nominalReturned = if (parsedQty != null && parsedSalePrice != null)
        parsedQty * parsedSalePrice else null
    val totalReceived = if (nominalReturned != null) nominalReturned + netInterest else null

    val isSecondary = closeType == FixedIncomeCloseType.SECONDARY_SALE
    val isEarlyCancel = closeType == FixedIncomeCloseType.EARLY_CANCELLATION

    val sellExceeds = parsedQty != null && parsedQty > availableQty

    val isValid = parsedQty != null && parsedQty > 0 && !sellExceeds
        && parsedSalePrice != null && parsedSalePrice > 0
        && platformId != null

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
                    .background(BorderGray)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isBond) "Liquidar bono" else "Liquidar depósito",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(asset.name, fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            // Tipo de cierre
            if (closeTypes.size > 1) {
                Text("Tipo de operación", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    closeTypes.forEach { ct ->
                        val isSel = closeType == ct
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PrimaryDark.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = if (isSel) 1.dp else 0.dp,
                                    color = if (isSel) PrimaryDark else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { closeType = ct }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ct.label,
                                fontSize = 12.sp,
                                color = if (isSel) PrimaryDark else TextSecondary,
                                fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(closeType.description, fontSize = 11.sp, color = TextSecondary)
                Spacer(Modifier.height(16.dp))
            }

            // Cantidad (editable para venta secundario de bonos, fijo para el resto)
            if (isBond && isSecondary) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Títulos a vender") },
                    isError = sellExceeds,
                    supportingText = {
                        Text(
                            text = if (sellExceeds) "Solo tienes ${formatQty(availableQty)} títulos"
                                   else "Disponible: ${formatQty(availableQty)} títulos",
                            fontSize = 11.sp,
                            color = if (sellExceeds) ExpenseRed else TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (sellExceeds) ExpenseRed else PrimaryDark,
                        unfocusedBorderColor = if (sellExceeds) ExpenseRed else BorderGray
                    )
                )
                Spacer(Modifier.height(12.dp))
            } else {
                // Vencimiento o cancelación anticipada: cantidad fija
                if (availableQty > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceElevated
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (isDeposit) "Depósito" else "Títulos en cartera",
                                fontSize = 13.sp, color = TextSecondary
                            )
                            Text(
                                if (isDeposit) "1 depósito" else "${formatQty(availableQty)} títulos",
                                fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Precio de venta / nominal devuelto por unidad
            OutlinedTextField(
                value = salePriceText,
                onValueChange = { salePriceText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label = {
                    Text(
                        when {
                            isSecondary -> "Precio de venta por título"
                            isEarlyCancel -> "Capital devuelto"
                            isBond -> "Nominal devuelto por título"
                            else -> "Capital devuelto"
                        }
                    )
                },
                placeholder = { Text("0,00") },
                trailingIcon = { Text(symbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // Intereses brutos devengados
            if (!isSecondary) {
                OutlinedTextField(
                    value = grossInterestText,
                    onValueChange = { grossInterestText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = {
                        Text(
                            if (isEarlyCancel) "Intereses devengados (bruto)"
                            else "Intereses / cupón final (bruto)"
                        )
                    },
                    placeholder = { Text("0,00") },
                    trailingIcon = { Text(symbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                    supportingText = {
                        Text(
                            if (isEarlyCancel) "Intereses acumulados hasta la fecha de cancelación"
                            else "Intereses generados al vencimiento",
                            fontSize = 11.sp, color = TextSecondary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    )
                )
                Spacer(Modifier.height(12.dp))
            }

            // IRPF
            OutlinedTextField(
                value = irpfPercentText,
                onValueChange = { irpfPercentText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label = { Text("Retención IRPF") },
                placeholder = { Text("19") },
                trailingIcon = { Text("%", color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                supportingText = {
                    Text(
                        if (isSecondary) "Sobre la plusvalía de la venta"
                        else "Sobre los intereses brutos",
                        fontSize = 11.sp, color = TextSecondary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // Comisiones
            OutlinedTextField(
                value = commissionText,
                onValueChange = { commissionText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label = {
                    Text(
                        if (isEarlyCancel) "Penalización / comisiones"
                        else "Comisiones"
                    )
                },
                placeholder = { Text("0,00") },
                trailingIcon = { Text(symbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // Fecha
            Text(
                when (closeType) {
                    FixedIncomeCloseType.MATURITY -> "Fecha de vencimiento"
                    FixedIncomeCloseType.SECONDARY_SALE -> "Fecha de venta"
                    FixedIncomeCloseType.EARLY_CANCELLATION -> "Fecha de cancelación"
                },
                fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.5.dp, BorderGray, RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(formatFullDateLocal2(dateMillis), fontSize = 14.sp, color = TextPrimary)
            }
            Spacer(Modifier.height(12.dp))

            // Plataforma
            Text("Entidad / Plataforma", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                platforms.forEach { p ->
                    PlatformChipLocal2(
                        icon = p.icon,
                        label = p.name,
                        isSelected = platformId == p.id,
                        onClick = { platformId = p.id }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // Notas
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            // Resumen
            if (totalReceived != null && totalReceived > 0) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = IncomeGreen.copy(alpha = 0.07f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (isSecondary) "Venta títulos" else "Capital devuelto",
                                fontSize = 11.sp, color = TextSecondary
                            )
                            Text(
                                "${formatAmount(nominalReturned ?: 0.0)} $symbol",
                                fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium
                            )
                        }
                        if (!isSecondary && parsedGrossInterest > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Intereses brutos", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "${formatAmount(parsedGrossInterest)} $symbol",
                                    fontSize = 12.sp, color = IncomeGreen, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (irpfOnInterest > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("IRPF (${irpfPercentText}%)", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    "- ${formatAmount(irpfOnInterest)} $symbol",
                                    fontSize = 12.sp, color = ExpenseRed, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (parsedCommission > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (isEarlyCancel) "Penalización" else "Comisiones",
                                    fontSize = 11.sp, color = TextSecondary
                                )
                                Text(
                                    "- ${formatAmount(parsedCommission)} $symbol",
                                    fontSize = 12.sp, color = ExpenseRed, fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 6.dp),
                            color = BorderGray, thickness = 0.5.dp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total recibido", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${formatAmount(totalReceived)} $symbol",
                                fontSize = 14.sp, color = IncomeGreen, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val qty = parsedQty ?: return@Button
                    val price = parsedSalePrice ?: return@Button
                    val platId = platformId ?: return@Button
                    onSave(
                        closeType, qty, price, parsedGrossInterest, parsedIrpf,
                        parsedCommission, dateMillis, platId,
                        notesText.ifBlank { null }
                    )
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text = when (closeType) {
                        FixedIncomeCloseType.MATURITY -> "Registrar vencimiento"
                        FixedIncomeCloseType.SECONDARY_SALE -> "Registrar venta"
                        FixedIncomeCloseType.EARLY_CANCELLATION -> "Registrar cancelación"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Date picker
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("Aceptar", color = PrimaryDark) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextSecondary) }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceWhite)
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryDark,
                    todayDateBorderColor = PrimaryDark
                )
            )
        }
    }
}

// ── Helpers locales ──────────────────────────────────────────────────────────

private fun formatFullDateLocal2(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}

@Composable
private fun PlatformChipLocal2(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark else SurfaceElevated
    val border = if (isSelected) PrimaryDark else BorderGray
    val text   = if (isSelected) Color.White else TextPrimary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, color = text, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
