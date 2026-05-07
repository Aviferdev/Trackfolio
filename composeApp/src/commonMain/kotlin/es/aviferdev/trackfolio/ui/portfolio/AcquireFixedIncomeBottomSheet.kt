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
import es.aviferdev.trackfolio.domain.model.FixedIncomeCategories
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Sheet para contratar (adquirir) un bono o depósito bancario.
 *
 * - Bonos: permiten cantidad > 1 (ej. "3 bonos de 1.000€ nominal cada uno").
 * - Depósitos: siempre 1 unidad con un capital nominal.
 *
 * El movimiento resultante es un BUY en AssetTransaction donde:
 * - quantity = número de unidades (1 para depósitos, N para bonos)
 * - pricePerUnit = nominal por unidad
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcquireFixedIncomeBottomSheet(
    asset: Asset,
    platforms: List<Platform>,
    currencyCode: String,
    onSave: (
        quantity: Double,
        nominalPerUnit: Double,
        date: Long,
        platformId: String,
        feeNote: String?,
        notes: String?
    ) -> Unit,
    onCreatePlatform: () -> Unit,
    onDismiss: () -> Unit
) {
    val symbol = currencySymbol(currencyCode)
    val isBond = asset.isBond
    val isDeposit = asset.isDeposit

    var quantity by remember { mutableStateOf(if (isDeposit) "1" else "") }
    var nominalText by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var platformId by remember { mutableStateOf(platforms.firstOrNull()?.id) }
    var feeNote by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedQty = quantity.replace(',', '.').toDoubleOrNull()
    val parsedNominal = nominalText.replace(',', '.').toDoubleOrNull()
    val now = Clock.System.now().toEpochMilliseconds()

    val totalInvested = if (parsedQty != null && parsedNominal != null && parsedQty > 0 && parsedNominal > 0)
        parsedQty * parsedNominal else null

    val isValid = parsedQty != null && parsedQty > 0
        && parsedNominal != null && parsedNominal > 0
        && platformId != null
        && dateMillis <= now

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
                text = if (isBond) "Adquirir bono" else "Contratar depósito",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))

            // Cabecera del activo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBond) "📜" else "🏦",
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(asset.name, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text(asset.ticker, fontSize = 11.sp, color = TextSecondary)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Cantidad (solo para bonos; depósitos siempre 1)
            if (isBond) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label = { Text("Número de títulos") },
                    placeholder = { Text("Ej. 3") },
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

            // Nominal por unidad
            OutlinedTextField(
                value = nominalText,
                onValueChange = { nominalText = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label = {
                    Text(
                        if (isBond) "Nominal por título"
                        else "Capital invertido"
                    )
                },
                placeholder = { Text("Ej. 1.000,00") },
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

            // Total invertido (solo bonos con cantidad > 1)
            if (isBond && totalInvested != null && (parsedQty ?: 0.0) > 1.0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Total: ${formatAmount(totalInvested)} $symbol",
                    fontSize = 12.sp,
                    color = PrimaryDark,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(12.dp))

            // Fecha de contratación
            Text("Fecha de contratación", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
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
                    text = formatFullDateLocal(dateMillis),
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(12.dp))

            // Plataforma
            Text("Entidad / Plataforma", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            if (platforms.isEmpty()) {
                EmptyPlatformsHintLocal(onCreate = onCreatePlatform)
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.forEach { p ->
                        PlatformChipLocal(
                            icon = p.icon,
                            label = p.name,
                            isSelected = platformId == p.id,
                            onClick = { platformId = p.id }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Comisión informativa
            OutlinedTextField(
                value = feeNote,
                onValueChange = { feeNote = it },
                label = { Text("Comisión de compra (opcional)") },
                placeholder = { Text("Ej. 0,10%, 5 €") },
                supportingText = {
                    Text("Texto informativo, no se incluye en los cálculos.", fontSize = 11.sp, color = TextSecondary)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // Notas
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Nota (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val qty = parsedQty ?: return@Button
                    val nominal = parsedNominal ?: return@Button
                    val platId = platformId ?: return@Button
                    onSave(qty, nominal, dateMillis, platId, feeNote.ifBlank { null }, notes.ifBlank { null })
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
                    text = if (isBond) "Registrar adquisición" else "Registrar contratación",
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
                    val selected = pickerState.selectedDateMillis
                    if (selected != null && selected <= Clock.System.now().toEpochMilliseconds()) {
                        dateMillis = selected
                    }
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

private fun formatFullDateLocal(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}

@Composable
private fun PlatformChipLocal(
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

@Composable
private fun EmptyPlatformsHintLocal(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .padding(14.dp)
    ) {
        Text(
            "Crea una plataforma para asociar esta operación (banco, bróker…).",
            fontSize = 12.sp,
            color = TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onCreate,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("+ Crear plataforma", fontSize = 13.sp, color = PrimaryDark, fontWeight = FontWeight.Medium)
        }
    }
}
