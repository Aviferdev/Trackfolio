package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.domain.model.AssetCategoryType
import es.aviferdev.trackfolio.domain.model.Platform
import es.aviferdev.trackfolio.ui.theme.BorderGray
import es.aviferdev.trackfolio.ui.theme.ExpenseRed
import es.aviferdev.trackfolio.ui.theme.PrimaryDark
import es.aviferdev.trackfolio.ui.theme.SurfaceWhite
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.TextPrimary
import es.aviferdev.trackfolio.ui.theme.TextSecondary
import es.aviferdev.trackfolio.ui.theme.TrackfolioTheme
import es.aviferdev.trackfolio.ui.theme.currencySymbol
import org.jetbrains.compose.ui.tooling.preview.Preview
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
    allSectors: List<es.aviferdev.trackfolio.domain.model.AssetSector> = emptyList(),
    linkedSectorIds: Set<String> = emptySet(),
    allRegions: List<es.aviferdev.trackfolio.domain.model.AssetRegion> = emptyList(),
    linkedRegionPercents: Map<String, Int> = emptyMap(),
    linkedFixedIncomePercent: Int = 0,
    onSave: (
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        platformIds: Set<String>,
        maturityDate: Long?,
        fixedIncomePercent: Int,
        sectorIds: Set<String>,
        regionPercents: Map<String, Int>
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

    var selectedPlatformIds by remember(linkedPlatformIds) { mutableStateOf(linkedPlatformIds) }
    var fixedIncomePercent by remember(linkedFixedIncomePercent) { mutableStateOf(linkedFixedIncomePercent) }
    var selectedSectorIds by remember(linkedSectorIds) { mutableStateOf(linkedSectorIds) }
    var regionPercents by remember(allRegions, linkedRegionPercents) {
        mutableStateOf(
            if (linkedRegionPercents.isNotEmpty()) linkedRegionPercents.toMap()
            else allRegions.associate { it.id to 0 }
        )
    }

    // Estado para fecha de vencimiento (solo para Renta Fija)
    var maturityDateMillis by remember {
        mutableStateOf(
            asset?.maturityDate
                ?: (Clock.System.now().toEpochMilliseconds() + 365L * 24 * 60 * 60 * 1000)
        )
    }
    var showMaturityDatePicker by remember { mutableStateOf(false) }

    // Computed: ¿La categoría actual permite análisis (sectores, regiones, composición)?
    val isAnalyzable = AssetCategoryType.isAnalyzable(selectedCategoryId)
    // Computed: ¿La categoría actual es de renta fija?
    val isFixedIncome = AssetCategoryType.isFixedIncome(selectedCategoryId)

    // Ensure all regions are in the map
    LaunchedEffect(allRegions) {
        val updated = allRegions.associate { it.id to (regionPercents[it.id] ?: 0) }
        regionPercents = updated
    }

    var tickerError by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }

    val isValid = ticker.isNotBlank() && name.isNotBlank()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() }
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

            // ── Composición RF / RV (solo para Acciones, ETFs, Fondos) ───────
            if (isAnalyzable) {
                Text(
                    text       = "Composición RF / RV",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value     = fixedIncomePercent.toFloat(),
                        onValueChange = { fixedIncomePercent = it.toInt() },
                        valueRange = 0f..100f,
                        steps     = 3,
                        modifier  = Modifier.weight(1f),
                        colors    = SliderDefaults.colors(
                            thumbColor   = PrimaryDark,
                            activeTrackColor = PrimaryDark
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = "${fixedIncomePercent}%",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = PrimaryDark,
                        modifier   = Modifier.width(50.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0, 25, 50, 75, 100).forEach { pct ->
                        Text(
                            text     = "$pct%",
                            fontSize = 9.sp,
                            color    = if (pct == fixedIncomePercent) PrimaryDark else TextSecondary
                        )
                    }
                }
                Text(
                    text     = "RF: Renta Fija (${100 - fixedIncomePercent}% RV: Renta Variable)",
                    fontSize = 10.sp,
                    color    = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Precio actual (no disponible para Renta Fija) ───────────────
            if (isFixedIncome) {
                Text(
                    text     = "Precio de mercado",
                    fontSize = 12.sp,
                    color    = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = "Los activos de renta fija no utilizan precio de mercado. Use el sistema de posiciones de renta fija para registrar estos activos.",
                    fontSize = 11.sp,
                    color    = TextSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(12.dp)
                )
                Spacer(Modifier.height(12.dp))
            } else {
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
            }

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

            // ── Sectores (solo para Acciones, ETFs, Fondos) ────────────────────
            if (isAnalyzable && allSectors.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = "Sectores",
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
                    allSectors.forEach { sector ->
                        val isSelected = sector.id in selectedSectorIds
                        SectorToggleChip(
                            icon       = sector.icon,
                            label      = sector.name,
                            isSelected = isSelected,
                            onClick    = {
                                selectedSectorIds = if (isSelected)
                                    selectedSectorIds - sector.id
                                else
                                    selectedSectorIds + sector.id
                            }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Clasifica el activo en uno o varios sectores.",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            // ── Distribución regional (solo para Acciones, ETFs, Fondos) ────────
            if (isAnalyzable && allRegions.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = "Distribución regional",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Asigna el porcentaje de inversión por región (opcional)",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(8.dp))
                allRegions.forEach { region ->
                    val currentValue = regionPercents[region.id] ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = region.name,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.width(100.dp)
                        )
                        Slider(
                            value = currentValue.toFloat(),
                            onValueChange = { newValue ->
                                regionPercents = regionPercents + (region.id to newValue.toInt())
                            },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryDark,
                                activeTrackColor = PrimaryDark
                            )
                        )
                        Text(
                            text = "${currentValue}%",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                }
                val totalPercent = regionPercents.values.sum()
                val totalColor = if (totalPercent > 100) ExpenseRed else TextSecondary
                Text(
                    text = "Total: $totalPercent% (debe ser ≤ 100%)",
                    fontSize = 10.sp,
                    color = totalColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Fecha de vencimiento (solo para Renta Fija) ─────────────────────
            if (isFixedIncome) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = "Fecha de vencimiento",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = formatFullDate(maturityDateMillis),
                    fontSize = 14.sp,
                    color    = TextPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.5.dp, BorderGray, RoundedCornerShape(10.dp))
                        .clickable { showMaturityDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Toca para seleccionar la fecha de vencimiento del activo.",
                    fontSize = 10.sp,
                    color = TextSecondary
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
                    val curr = if (isFixedIncome) null else currentPrice.replace(',', '.').toDoubleOrNull()
                    val regionsToSave = if (isAnalyzable) regionPercents.filter { it.value > 0 } else emptyMap()
                    val sectorsToSave = if (isAnalyzable) selectedSectorIds else emptySet()
                    val compositionToSave = if (isAnalyzable) fixedIncomePercent else 0
                    val maturityToSave = if (isFixedIncome) maturityDateMillis else null
                    onSave(
                        ticker.trim(),
                        name.trim(),
                        notes.ifBlank { null },
                        selectedCategoryId,
                        curr,
                        selectedPlatformIds,
                        maturityToSave,
                        compositionToSave,
                        sectorsToSave,
                        regionsToSave
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

    // ── DatePicker para fecha de vencimiento ─────────────────────────────
    if (showMaturityDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = maturityDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showMaturityDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = pickerState.selectedDateMillis
                    if (selected != null && selected > Clock.System.now().toEpochMilliseconds()) {
                        maturityDateMillis = selected
                    }
                    showMaturityDatePicker = false
                }) { Text("Aceptar", color = PrimaryDark) }
            },
            dismissButton = {
                TextButton(onClick = { showMaturityDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
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


// ── Helpers ──────────────────────────────────────────────────────────────────

fun formatFullDate(epochMillis: Long): String {
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

@Composable
private fun SectorToggleChip(
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

@Preview
@Composable
private fun AddEditAssetBottomSheetPreview() {
    TrackfolioTheme {
        val now = Clock.System.now().toEpochMilliseconds()
        val sampleCategories = listOf(
            AssetCategory("fixed_cat_funds", "Fondos de Inversión", "📊", 0, false, now),
            AssetCategory("fixed_cat_stocks", "Acciones", "📈", 1, false, now),
            AssetCategory("fixed_cat_etfs", "ETFs", "📉", 2, false, now),
            AssetCategory("fixed_cat_pensions", "Planes de Pensiones", "🏦", 3, false, now)
        )
        val samplePlatforms = listOf(
            Platform("platform-1", "Banco Santander", "🏦", 0, false, now),
            Platform("platform-2", "BBVA", "🏛️", 1, false, now),
            Platform("platform-3", "ING", "🏠", 2, false, now)
        )

        AddEditAssetBottomSheet(
            asset = null,
            categories = sampleCategories,
            currencyCode = "EUR",
            preselectedCategoryId = null,
            allPlatforms = samplePlatforms,
            linkedPlatformIds = emptySet(),
            allSectors = emptyList(),
            linkedSectorIds = emptySet(),
            allRegions = emptyList(),
            linkedRegionPercents = emptyMap(),
            linkedFixedIncomePercent = 0,
            onSave = { _, _, _, _, _, _, _, _, _, _ -> },
            onDismiss = {}
        )
    }
}
