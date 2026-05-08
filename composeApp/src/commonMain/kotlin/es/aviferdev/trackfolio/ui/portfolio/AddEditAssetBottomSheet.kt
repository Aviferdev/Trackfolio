package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetBottomSheet(
    asset: Asset?,
    categories: List<AssetCategory>,
    currencyCode: String = "EUR",
    preselectedCategoryId: String? = null,
    allPlatforms: List<Platform> = emptyList(),
    linkedPlatformIds: Set<String> = emptySet(),
    onSave: (
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        platformIds: Set<String>,
        maturityDate: Long?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = asset != null
    val symbol    = currencySymbol(currencyCode)

    var ticker        by remember { mutableStateOf(asset?.ticker ?: "") }
    var name          by remember { mutableStateOf(asset?.name ?: "") }
    var currentPrice  by remember { mutableStateOf(asset?.currentPrice?.toString() ?: "") }
    var notes         by remember { mutableStateOf(asset?.notes ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(asset?.assetCategoryId ?: preselectedCategoryId)
    }
    var selectedPlatformIds by remember { mutableStateOf(linkedPlatformIds) }

    var tickerError by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }

    val isValid = ticker.isNotBlank() && name.isNotBlank()
        && selectedCategoryId != null
        && (currentPrice.isBlank() || currentPrice.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
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
                text       = if (isEditing) "Editar activo" else "Nuevo activo",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text     = "Define la ficha del activo y las plataformas donde operas.",
                fontSize = 11.sp,
                color    = TextSecondary,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // ── Selector de categoría (solo si no viene prefijada) ──────────
            if (preselectedCategoryId == null) {
                Text(
                    text       = "Categoría",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        CategoryChip(
                            icon       = cat.icon,
                            label      = cat.name,
                            isSelected = selectedCategoryId == cat.id,
                            onClick    = { selectedCategoryId = cat.id }
                        )
                    }
                }
                if (selectedCategoryId == null) {
                    Spacer(Modifier.height(4.dp))
                    Text("Selecciona una categoría", fontSize = 11.sp, color = ExpenseRed)
                }
                Spacer(Modifier.height(16.dp))
            }

            // Ticker
            OutlinedTextField(
                value         = ticker,
                onValueChange = { ticker = it.uppercase(); tickerError = false },
                label         = { Text("Ticker / Símbolo") },
                placeholder   = {
                    Text("Ej. AAPL, BTC, IAG.MC")
                },
                isError       = tickerError,
                supportingText = if (tickerError) {{ Text("Obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // Nombre
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Nombre del activo") },
                placeholder   = {
                    Text("Ej. Apple Inc., Bitcoin")
                },
                isError       = nameError,
                supportingText = if (nameError) {{ Text("Obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // ── Precio actual ──────
            OutlinedTextField(
                value         = currentPrice,
                onValueChange = { currentPrice = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label         = { Text("Precio actual (opcional)") },
                placeholder   = { Text("0,00") },
                trailingIcon  = { Text(symbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                supportingText = {
                    Text(
                        text     = "Sirve para calcular el valor actual y la revalorización.",
                        fontSize = 11.sp,
                        color    = TextSecondary
                    )
                },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )
            Spacer(Modifier.height(12.dp))

            // ── Plataformas vinculadas (multi-select) ────────────────────────
            if (allPlatforms.isNotEmpty()) {
                Text(
                    text       = "Plataformas donde operas este activo",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allPlatforms.forEach { platform ->
                        val isSelected = platform.id in selectedPlatformIds
                        PlatformToggleChip(
                            icon       = platform.icon,
                            label      = platform.name,
                            isSelected = isSelected,
                            onClick    = {
                                selectedPlatformIds = if (isSelected)
                                    selectedPlatformIds - platform.id
                                else
                                    selectedPlatformIds + platform.id
                            }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Selecciona todas las plataformas donde compras o vendes este activo.",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Text(
                    text     = "Crea plataformas primero desde la configuración del activo para vincularlas.",
                    fontSize = 11.sp,
                    color    = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Nota
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
                    if (ticker.isBlank()) { tickerError = true; return@Button }
                    if (name.isBlank())   { nameError = true; return@Button }
                    val curr = currentPrice.replace(',', '.').toDoubleOrNull()
                    onSave(
                        ticker.trim(),
                        name.trim(),
                        notes.ifBlank { null },
                        selectedCategoryId,
                        curr,
                        selectedPlatformIds,
                        asset?.maturityDate
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
                    text       = if (isEditing) "Guardar cambios" else "Crear activo",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatFullDate(epochMillis: Long): String {
    val months = listOf("enero","febrero","marzo","abril","mayo","junio",
        "julio","agosto","septiembre","octubre","noviembre","diciembre")
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}

@Composable
private fun CategoryChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark              else SurfaceElevated
    val border = if (isSelected) PrimaryDark              else BorderGray
    val text   = if (isSelected) MaterialTheme.colorScheme.onPrimary else TextPrimary

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
private fun PlatformToggleChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg     = if (isSelected) PrimaryDark.copy(alpha = 0.12f) else SurfaceElevated
    val border = if (isSelected) PrimaryDark                      else BorderGray
    val text   = if (isSelected) PrimaryDark                      else TextPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 0.5.dp, border, RoundedCornerShape(20.dp))
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
        if (isSelected) {
            Spacer(Modifier.width(4.dp))
            Text("✓", fontSize = 12.sp, color = PrimaryDark, fontWeight = FontWeight.Bold)
        }
    }
}
