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
import es.aviferdev.trackfolio.domain.model.AssetTransactionType
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.domain.portfolio.PortfolioCalculator
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Sheet para registrar (o editar) un movimiento de compra/venta sobre un
 * activo. Validaciones:
 *
 * - Plataforma obligatoria (decisión 3.A). Si no hay plataformas creadas,
 *   se muestra un atajo a la pantalla de creación (decisión 8.C).
 * - Bloqueo de sobreventa (decisión 7.A): para una venta, la cantidad no
 *   puede exceder las unidades disponibles a la fecha indicada.
 * - Fee como texto libre informativo (decisión 5.C): no entra en cálculos.
 *
 * @param assetTransactions movimientos del activo seleccionado, para
 *        validar la sobreventa con FIFO.
 * @param onCreatePlatform invocado cuando el usuario pulsa "Crear plataforma"
 *        en el atajo inline. La sheet se cerrará y el caller debe abrir
 *        AddEditPlatformSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetTransactionBottomSheet(
    transaction: AssetTransaction?,             // null = crear
    fixedAsset: Asset?,                          // si != null, no se permite cambiar de activo
    allAssets: List<Asset>,                      // catálogo (para el selector)
    platforms: List<Platform>,                   // plataformas activas
    assetTransactions: List<AssetTransaction>,   // movimientos del activo seleccionado actual
    currencyCode: String,
    onSave: (
        assetId: String,
        type: AssetTransactionType,
        quantity: Double,
        pricePerUnit: Double,
        date: Long,
        platformId: String,
        feeNote: String?,
        notes: String?
    ) -> Unit,
    onCreatePlatform: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = transaction != null
    val symbol = currencySymbol(currencyCode)

    // ── Estado del formulario ────────────────────────────────────────────────
    var selectedAssetId by remember(transaction, fixedAsset) {
        mutableStateOf(fixedAsset?.id ?: transaction?.assetId ?: allAssets.firstOrNull()?.id)
    }
    var type by remember(transaction) {
        mutableStateOf(transaction?.type ?: AssetTransactionType.BUY)
    }
    var quantity by remember(transaction) {
        mutableStateOf(transaction?.quantity?.toString() ?: "")
    }
    var pricePerUnit by remember(transaction) {
        mutableStateOf(transaction?.pricePerUnit?.toString() ?: "")
    }
    var dateMillis by remember(transaction) {
        mutableStateOf(transaction?.date ?: Clock.System.now().toEpochMilliseconds())
    }
    var platformId by remember(transaction) {
        mutableStateOf(transaction?.platformId ?: platforms.firstOrNull()?.id)
    }
    var feeNote by remember(transaction) {
        mutableStateOf(transaction?.feeNote ?: "")
    }
    var notes by remember(transaction) {
        mutableStateOf(transaction?.notes ?: "")
    }
    var showDatePicker by remember { mutableStateOf(false) }

    // ── Validación ───────────────────────────────────────────────────────────
    val parsedQty   = quantity.replace(',', '.').toDoubleOrNull()
    val parsedPrice = pricePerUnit.replace(',', '.').toDoubleOrNull()
    val now         = Clock.System.now().toEpochMilliseconds()

    val availableForSale: Double = if (selectedAssetId != null) {
        PortfolioCalculator.availableQuantityAt(
            transactions           = assetTransactions.filter { it.assetId == selectedAssetId },
            asOfDate               = dateMillis,
            excludingTransactionId = transaction?.id
        )
    } else 0.0

    val sellExceeds = type == AssetTransactionType.SELL
        && parsedQty != null && parsedQty > availableForSale

    val isValid = selectedAssetId != null
        && parsedQty != null && parsedQty > 0.0
        && parsedPrice != null && parsedPrice > 0.0
        && platformId != null
        && dateMillis <= now
        && !sellExceeds

    val selectedAsset = remember(selectedAssetId, allAssets) {
        allAssets.firstOrNull { it.id == selectedAssetId }
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
                text       = if (isEditing) "Editar movimiento" else "Nuevo movimiento",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 16.dp)
            )

            // ── Tipo BUY/SELL ────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TypeToggle(
                    label    = "Compra",
                    isSel    = type == AssetTransactionType.BUY,
                    selColor = IncomeGreen,
                    modifier = Modifier.weight(1f),
                    onClick  = { type = AssetTransactionType.BUY }
                )
                TypeToggle(
                    label    = "Venta",
                    isSel    = type == AssetTransactionType.SELL,
                    selColor = ExpenseRed,
                    modifier = Modifier.weight(1f),
                    onClick  = { type = AssetTransactionType.SELL }
                )
            }
            Spacer(Modifier.height(16.dp))

            // ── Selector de activo (oculto si fixedAsset != null) ────────────
            if (fixedAsset == null) {
                Text("Activo", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                if (allAssets.isEmpty()) {
                    EmptyAssetsHint()
                } else {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allAssets.forEach { asset ->
                            AssetChip(
                                ticker     = asset.ticker,
                                name       = asset.name,
                                isSelected = selectedAssetId == asset.id,
                                onClick    = { selectedAssetId = asset.id }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                // Mostrar el activo fijado solo como cabecera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier         = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = fixedAsset.ticker.take(3),
                            fontSize   = if (fixedAsset.ticker.length > 3) 9.sp else 11.sp,
                            color      = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fixedAsset.name, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text(fixedAsset.ticker, fontSize = 11.sp, color = TextSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Cantidad + precio ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text("Cantidad") },
                    placeholder   = { Text("0") },
                    isError       = sellExceeds,
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = if (sellExceeds) ExpenseRed else PrimaryDark,
                        unfocusedBorderColor = if (sellExceeds) ExpenseRed else BorderGray
                    )
                )
                OutlinedTextField(
                    value         = pricePerUnit,
                    onValueChange = { pricePerUnit = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text("Precio unidad") },
                    placeholder   = { Text("0,00") },
                    trailingIcon  = { Text(symbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    )
                )
            }
            if (type == AssetTransactionType.SELL && selectedAssetId != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text     = if (sellExceeds)
                        "Solo tienes ${formatQty(availableForSale)} unidades disponibles a esa fecha"
                    else
                        "Disponible: ${formatQty(availableForSale)} unidades a esa fecha",
                    fontSize = 11.sp,
                    color    = if (sellExceeds) ExpenseRed else TextSecondary
                )
            }
            Spacer(Modifier.height(12.dp))

            // ── Fecha ────────────────────────────────────────────────────────
            Text("Fecha", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.5.dp, BorderGray, RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text     = formatFullDate(dateMillis),
                    fontSize = 14.sp,
                    color    = TextPrimary
                )
            }
            Spacer(Modifier.height(12.dp))

            // ── Plataforma (obligatoria) ────────────────────────────────────
            Text("Plataforma", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            if (platforms.isEmpty()) {
                EmptyPlatformsInlineHint(onCreate = onCreatePlatform)
            } else {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.forEach { p ->
                        PlatformChip(
                            icon       = p.icon,
                            label      = p.name,
                            isSelected = platformId == p.id,
                            onClick    = { platformId = p.id }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Comisión informativa ────────────────────────────────────────
            OutlinedTextField(
                value         = feeNote,
                onValueChange = { feeNote = it },
                label         = { Text("Comisión (opcional)") },
                placeholder   = { Text("Ej. 0,5%, 1,20 €") },
                supportingText = {
                    Text(
                        text     = "Texto informativo. No se incluye en el cálculo del P&L.",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // ── Notas ───────────────────────────────────────────────────────
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text("Nota (opcional)") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val assetIdNonNull    = selectedAssetId    ?: return@Button
                    val platformIdNonNull = platformId         ?: return@Button
                    val qty               = parsedQty          ?: return@Button
                    val price             = parsedPrice        ?: return@Button
                    onSave(
                        assetIdNonNull,
                        type,
                        qty,
                        price,
                        dateMillis,
                        platformIdNonNull,
                        feeNote.ifBlank { null },
                        notes.ifBlank { null }
                    )
                },
                enabled  = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text       = if (isEditing) "Guardar cambios" else "Registrar movimiento",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // ── Date picker ──────────────────────────────────────────────────────────
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = pickerState.selectedDateMillis
                    if (selected != null && selected <= Clock.System.now().toEpochMilliseconds()) {
                        dateMillis = selected
                    }
                    showDatePicker = false
                }) { Text("Aceptar", color = PrimaryDark) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceWhite)
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = PrimaryDark,
                    todayDateBorderColor      = PrimaryDark
                )
            )
        }
    }
}

// ─── Componentes auxiliares ───────────────────────────────────────────────────

@Composable
private fun TypeToggle(
    label: String,
    isSel: Boolean,
    selColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSel) selColor.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (isSel) 1.dp else 0.dp,
                color = if (isSel) selColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 14.sp,
            color      = if (isSel) selColor else TextSecondary,
            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun AssetChip(
    ticker: String,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark    else SurfaceElevated
    val border = if (isSelected) PrimaryDark    else BorderGray
    val text   = if (isSelected) Color.White    else TextPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text       = ticker,
            fontSize   = 12.sp,
            color      = text,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text  = name,
            fontSize = 12.sp,
            color = text.copy(alpha = if (isSelected) 0.85f else 0.65f)
        )
    }
}

@Composable
private fun PlatformChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark    else SurfaceElevated
    val border = if (isSelected) PrimaryDark    else BorderGray
    val text   = if (isSelected) Color.White    else TextPrimary

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
        Text(
            text       = label,
            fontSize   = 13.sp,
            color      = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun EmptyAssetsHint() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text     = "Aún no tienes activos. Créalos primero desde Ajustes › Portfolio › Activos.",
            fontSize = 12.sp,
            color    = TextSecondary
        )
    }
}

@Composable
private fun EmptyPlatformsInlineHint(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(14.dp)
    ) {
        Text(
            text     = "Aún no tienes plataformas. Crea la primera para asociar este movimiento (broker, exchange, banco…).",
            fontSize = 12.sp,
            color    = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick        = onCreate,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("+ Crear plataforma", fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Helpers de formato locales ───────────────────────────────────────────────

private fun formatFullDate(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}

internal fun formatQty(value: Double): String {
    if (value == value.toLong().toDouble()) return value.toLong().toString()
    val rounded = (value * 10000).toLong()
    val intPart = rounded / 10000
    val decPart = rounded % 10000
    val decStr = decPart.toString().padStart(4, '0').trimEnd('0').ifEmpty { "0" }
    return "$intPart,$decStr"
}
