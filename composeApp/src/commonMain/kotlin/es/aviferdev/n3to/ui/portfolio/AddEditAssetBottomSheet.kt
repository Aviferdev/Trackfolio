package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.domain.model.PriceQuote

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import es.aviferdev.n3to.ui.portfolio.components.*
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.portfolio_add_asset_cancel
import n3to.composeapp.generated.resources.portfolio_add_asset_category_label
import n3to.composeapp.generated.resources.portfolio_add_asset_composition_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_composition_label
import n3to.composeapp.generated.resources.portfolio_add_asset_create
import n3to.composeapp.generated.resources.portfolio_add_asset_desc
import n3to.composeapp.generated.resources.portfolio_add_asset_fi_price_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_market_price_label
import n3to.composeapp.generated.resources.portfolio_add_asset_maturity_date_label
import n3to.composeapp.generated.resources.portfolio_add_asset_maturity_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_name_label
import n3to.composeapp.generated.resources.portfolio_add_asset_name_placeholder
import n3to.composeapp.generated.resources.portfolio_add_asset_no_platforms_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_no_portfolio
import n3to.composeapp.generated.resources.portfolio_add_asset_notes_optional
import n3to.composeapp.generated.resources.portfolio_add_asset_platforms_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_platforms_label
import n3to.composeapp.generated.resources.portfolio_add_asset_portfolio_label
import n3to.composeapp.generated.resources.portfolio_add_asset_price_desc
import n3to.composeapp.generated.resources.portfolio_add_asset_price_optional
import n3to.composeapp.generated.resources.portfolio_add_asset_price_placeholder_val
import n3to.composeapp.generated.resources.portfolio_add_asset_regions_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_regions_label
import n3to.composeapp.generated.resources.portfolio_add_asset_regions_total
import n3to.composeapp.generated.resources.portfolio_add_asset_save_changes
import n3to.composeapp.generated.resources.portfolio_add_asset_sectors_hint
import n3to.composeapp.generated.resources.portfolio_add_asset_sectors_label
import n3to.composeapp.generated.resources.portfolio_add_asset_select_category
import n3to.composeapp.generated.resources.portfolio_add_asset_ticker_label
import n3to.composeapp.generated.resources.portfolio_add_asset_ticker_placeholder
import n3to.composeapp.generated.resources.portfolio_add_asset_ticker_required
import n3to.composeapp.generated.resources.portfolio_add_asset_title_create
import n3to.composeapp.generated.resources.portfolio_add_asset_title_edit
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

/**
 * Estado de la validación del ISIN contra la API de cotizaciones.
 */
private sealed class IsinValidationState {
    data object Idle : IsinValidationState()
    data object Validating : IsinValidationState()
    data class Valid(
        val name: String?,
        val price: Double,
        val currency: String,
        val exchange: String?
    ) : IsinValidationState()
    data class NotFound(val message: String) : IsinValidationState()
    data class Error(val message: String) : IsinValidationState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetBottomSheet(
    asset: Asset?,
    categories: List<AssetCategory>,
    preselectedCategoryId: String? = null,
    allPlatforms: List<Platform> = emptyList(),
    linkedPlatformIds: Set<String> = emptySet(),
    allSectors: List<es.aviferdev.n3to.domain.model.AssetSector> = emptyList(),
    linkedSectorIds: Set<String> = emptySet(),
    allRegions: List<es.aviferdev.n3to.domain.model.AssetRegion> = emptyList(),
    linkedRegionPercents: Map<String, Int> = emptyMap(),
    linkedFixedIncomePercent: Int = 0,
    portfolios: List<Portfolio> = emptyList(),
    selectedPortfolioId: String? = null,
    onValidateIsin: (suspend (String, String?) -> Result<PriceQuote>)? = null,
    onSave: (
        ticker: String,
        name: String,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?,
        isin: String?,
        platformIds: Set<String>,
        maturityDate: Long?,
        fixedIncomePercent: Int,
        sectorIds: Set<String>,
        regionPercents: Map<String, Int>,
        portfolioId: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = asset != null

    var ticker        by remember { mutableStateOf(asset?.ticker ?: "") }
    var name          by remember { mutableStateOf(asset?.name ?: "") }
    var currentPrice  by remember { mutableStateOf(asset?.currentPrice?.toString() ?: "") }
    var notes         by remember { mutableStateOf(asset?.notes ?: "") }
    var isin          by remember { mutableStateOf(asset?.isin ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(asset?.assetCategoryId ?: preselectedCategoryId)
    }
    var selectedPlatformIds by remember(linkedPlatformIds) { mutableStateOf(linkedPlatformIds) }
    var fixedIncomePercent by remember(linkedFixedIncomePercent) { mutableStateOf(linkedFixedIncomePercent) }
    var currentPortfolioId by remember { mutableStateOf(selectedPortfolioId ?: asset?.portfolioId) }
    var showPortfolioMenu by remember { mutableStateOf(false) }
    var selectedSectorIds by remember(linkedSectorIds) { mutableStateOf(linkedSectorIds) }
    var regionPercents by remember(allRegions, linkedRegionPercents) {
        mutableStateOf(
            if (linkedRegionPercents.isNotEmpty()) linkedRegionPercents.toMap()
            else allRegions.associate { it.id to 0 }
        )
    }
    // Estado de la validación del ISIN contra la API
    var isinValidationState by remember { mutableStateOf<IsinValidationState>(IsinValidationState.Idle) }
    val coroutineScope = rememberCoroutineScope()

    // Estado para fecha de vencimiento (solo para Renta Fija)
    var maturityDateMillis by remember {
        mutableStateOf(
            asset?.maturityDate ?: (nowMillis() + 365L * 24 * 60 * 60 * 1000)
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
    var showCategoryHelp by remember { mutableStateOf(false) }

    val isValid = ticker.isNotBlank() && name.isNotBlank()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.appColors.surface,
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
                text       = if (isEditing) stringResource(Res.string.portfolio_add_asset_title_edit) else stringResource(Res.string.portfolio_add_asset_title_create),
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.appColors.textPrimary,
                modifier   = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text     = stringResource(Res.string.portfolio_add_asset_desc),
                fontSize = 11.sp,
                color    = MaterialTheme.appColors.textSecondary,
                modifier = Modifier.padding(bottom = 18.dp)
            )

            // ── Selector de categoría (solo si no viene prefijada) ──────────
            if (preselectedCategoryId == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = stringResource(Res.string.portfolio_add_asset_category_label),
                        fontSize   = 12.sp,
                        color      = MaterialTheme.appColors.textSecondary,
                        fontWeight = FontWeight.Medium,
                        modifier   = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = "Ayuda sobre tipos de activo",
                        tint = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { showCategoryHelp = true }
                    )
                }
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
                    Text(stringResource(Res.string.portfolio_add_asset_select_category), fontSize = 11.sp, color = MaterialTheme.appColors.expense)
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Selector de cartera ─────────────────────────────────────────
            if (portfolios.isNotEmpty()) {
                Text(
                    text       = stringResource(Res.string.portfolio_add_asset_portfolio_label),
                    fontSize   = 12.sp,
                    color      = MaterialTheme.appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PortfolioChipSimple(
                        label      = stringResource(Res.string.portfolio_add_asset_no_portfolio),
                        isSelected = currentPortfolioId == null,
                        onClick    = { currentPortfolioId = null }
                    )
                    portfolios.forEach { portfolio ->
                        PortfolioChipSimple(
                            label      = portfolio.name,
                            isSelected = currentPortfolioId == portfolio.id,
                            onClick    = { currentPortfolioId = portfolio.id }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Ticker
            OutlinedTextField(
                value         = ticker,
                onValueChange = { ticker = it.uppercase(); tickerError = false },
                label         = { Text(stringResource(Res.string.portfolio_add_asset_ticker_label)) },
                placeholder   = {
                    Text(stringResource(Res.string.portfolio_add_asset_ticker_placeholder))
                },
                isError       = tickerError,
                supportingText = if (tickerError) {{ Text(stringResource(Res.string.portfolio_add_asset_ticker_required)) }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                )
            )
            Spacer(Modifier.height(12.dp))

            // Nombre
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text(stringResource(Res.string.portfolio_add_asset_name_label)) },
                placeholder   = {
                    Text(stringResource(Res.string.portfolio_add_asset_name_placeholder))
                },
                isError       = nameError,
                supportingText = if (nameError) {{ Text(stringResource(Res.string.portfolio_add_asset_ticker_required)) }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                )
            )
Spacer(Modifier.height(12.dp))

            // ── ISIN (Código internacional del activo) ───────────────────────
            if (!isFixedIncome) {
                OutlinedTextField(
                    value         = isin,
                    onValueChange = { isin = it.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' || c == '.' }; isinValidationState = IsinValidationState.Idle },
                    label         = { Text("ISIN (opcional)") },
                    placeholder   = { Text("ES0173516115") },
                    supportingText = if (AssetCategoryType.isQuotable(selectedCategoryId)) {
                        { Text("Código ISIN para obtener precio automático", fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary) }
                    } else {
                        null
                    },
                    trailingIcon = if (onValidateIsin != null && AssetCategoryType.isQuotable(selectedCategoryId) && isin.isNotBlank()) {
                        {
                            TextButton(
                                onClick = {
                                    val isinTrimmed = isin.trim().uppercase().replace("-", "").replace(".", "")
                                    if (isinTrimmed.isNotBlank()) {
                                        isinValidationState = IsinValidationState.Validating
                                        coroutineScope.launch {
                                            onValidateIsin?.let { validate ->
                                                validate(isinTrimmed, selectedCategoryId)
                                                    .onSuccess { quote ->
                                                        isinValidationState = IsinValidationState.Valid(
                                                            name = quote.name,
                                                            price = quote.price,
                                                            currency = quote.currency,
                                                            exchange = quote.exchange
                                                        )
                                                    }
                                                    .onFailure { error ->
                                                        val msg = error.message ?: ""
                                                        if (msg.contains("Not Found", ignoreCase = true) || msg.contains("404", ignoreCase = true)) {
                                                            isinValidationState = IsinValidationState.NotFound("ISIN no encontrado en el mercado")
                                                        } else {
                                                            isinValidationState = IsinValidationState.Error(msg)
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                },
                                enabled = isinValidationState !is IsinValidationState.Validating
                            ) {
                                Text(
                                    text = when (isinValidationState) {
                                        is IsinValidationState.Validating -> "Validando..."
                                        is IsinValidationState.Valid -> "✓ Validado"
                                        else -> "Validar"
                                    },
                                    fontSize = 12.sp,
                                    color = when (isinValidationState) {
                                        is IsinValidationState.Valid -> MaterialTheme.appColors.income
                                        is IsinValidationState.NotFound -> MaterialTheme.appColors.expense
                                        is IsinValidationState.Error -> MaterialTheme.appColors.expense
                                        else -> MaterialTheme.appColors.primary
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        null
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    )
                )

                // ── Tarjeta de resultado de validación ────────────────────────
                when (val state = isinValidationState) {
                    is IsinValidationState.Valid -> {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.appColors.income.copy(alpha = 0.08f))
                                .border(0.5.dp, MaterialTheme.appColors.income.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("✅ ISIN validado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.income)
                                Spacer(Modifier.height(4.dp))
                                Row {
                                    Text("📊 ", fontSize = 12.sp)
                                    Text(state.name ?: "", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.appColors.textPrimary)
                                }
                                Row {
                                    Text("💰 ", fontSize = 12.sp)
                                    Text("${state.price} ${state.currency}", fontSize = 12.sp, color = MaterialTheme.appColors.textPrimary)
                                    if (state.exchange != null) {
                                        Text(" · ${state.exchange}", fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                    is IsinValidationState.NotFound -> {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.appColors.expense.copy(alpha = 0.08f))
                                .border(0.5.dp, MaterialTheme.appColors.expense.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("⚠️ ISIN no encontrado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.expense)
                                Text("El identificador no se ha localizado en el mercado. Puedes guardarlo sin validar.", fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary)
                            }
                        }
                    }
                    is IsinValidationState.Error -> {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.appColors.expense.copy(alpha = 0.08f))
                                .border(0.5.dp, MaterialTheme.appColors.expense.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("❌ Error de validación", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.expense)
                                Text(state.message, fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary)
                            }
                        }
                    }
                    else -> {} // Idle o Validating
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Composición RF / RV (solo para Acciones, ETFs, Fondos) ───────
            if (isAnalyzable) {
                Text(
                    text       = stringResource(Res.string.portfolio_add_asset_composition_label),
                    fontSize   = 12.sp,
                    color      = MaterialTheme.appColors.textSecondary,
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
                            thumbColor   = MaterialTheme.appColors.primary,
                            activeTrackColor = MaterialTheme.appColors.primary
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text       = "${fixedIncomePercent}%",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.appColors.primary,
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
                            color    = if (pct == fixedIncomePercent) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary
                        )
                    }
                }
                Text(
                    text     = stringResource(Res.string.portfolio_add_asset_composition_hint, 100 - fixedIncomePercent),
                    fontSize = 10.sp,
                    color    = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Precio actual (no disponible para Renta Fija) ───────────────
            if (isFixedIncome) {
                Text(
                    text     = stringResource(Res.string.portfolio_add_asset_market_price_label),
                    fontSize = 12.sp,
                    color    = MaterialTheme.appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = stringResource(Res.string.portfolio_add_asset_fi_price_hint),
                    fontSize = 11.sp,
                    color    = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.appColors.surfaceElevated)
                        .padding(12.dp)
                )
                Spacer(Modifier.height(12.dp))
            } else {
                OutlinedTextField(
                    value         = currentPrice,
                    onValueChange = { currentPrice = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text(stringResource(Res.string.portfolio_add_asset_price_optional)) },
                    placeholder   = { Text(stringResource(Res.string.portfolio_add_asset_price_placeholder_val)) },
                    trailingIcon  = { Text("€", color = MaterialTheme.appColors.textSecondary, modifier = Modifier.padding(end = 12.dp)) },
                    supportingText = {
                        Text(
                            text     = stringResource(Res.string.portfolio_add_asset_price_desc),
                            fontSize = 11.sp,
                            color    = MaterialTheme.appColors.textSecondary
                        )
                    },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    )
                )
                Spacer(Modifier.height(12.dp))
            }

            // ── Plataformas vinculadas (multi-select) ────────────────────────
            if (allPlatforms.isNotEmpty()) {
                Text(
                    text       = stringResource(Res.string.portfolio_add_asset_platforms_label),
                    fontSize   = 12.sp,
                    color      = MaterialTheme.appColors.textSecondary,
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
                    stringResource(Res.string.portfolio_add_asset_platforms_hint),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Text(
                    text     = stringResource(Res.string.portfolio_add_asset_no_platforms_hint),
                    fontSize = 11.sp,
                    color    = MaterialTheme.appColors.textSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // ── Sectores (solo para Acciones, ETFs, Fondos) ────────────────────
            if (isAnalyzable && allSectors.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = stringResource(Res.string.portfolio_add_asset_sectors_label),
                    fontSize   = 12.sp,
                    color      = MaterialTheme.appColors.textSecondary,
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
                    stringResource(Res.string.portfolio_add_asset_sectors_hint),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }

            // ── Distribución regional (solo para Acciones, ETFs, Fondos) ────────
            if (isAnalyzable && allRegions.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = stringResource(Res.string.portfolio_add_asset_regions_label),
                    fontSize   = 12.sp,
                    color      = MaterialTheme.appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.portfolio_add_asset_regions_hint),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textSecondary
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
                            color = MaterialTheme.appColors.textPrimary,
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
                                thumbColor = MaterialTheme.appColors.primary,
                                activeTrackColor = MaterialTheme.appColors.primary
                            )
                        )
                        Text(
                            text = "${currentValue}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.appColors.textPrimary,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                }
                val totalPercent = regionPercents.values.sum()
                val totalColor = if (totalPercent > 100) MaterialTheme.appColors.expense else MaterialTheme.appColors.textSecondary
                Text(
                    text = stringResource(Res.string.portfolio_add_asset_regions_total, totalPercent),
                    fontSize = 10.sp,
                    color = totalColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // ── Fecha de vencimiento (solo para Renta Fija) ─────────────────────
            if (isFixedIncome) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = stringResource(Res.string.portfolio_add_asset_maturity_date_label),
                    fontSize   = 12.sp,
                    color      = MaterialTheme.appColors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text     = formatFullDate(maturityDateMillis),
                    fontSize = 14.sp,
                    color    = MaterialTheme.appColors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(0.5.dp, MaterialTheme.appColors.border, RoundedCornerShape(10.dp))
                        .clickable { showMaturityDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.portfolio_add_asset_maturity_hint),
                    fontSize = 10.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }

            // Nota
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text(stringResource(Res.string.portfolio_add_asset_notes_optional)) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
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
                    val isinTrimmed = isin.trim().let { v ->
                        if (v.isBlank()) null else v.uppercase().replace("-", "").replace(".", "")
                    }
                    onSave(
                        ticker.trim(),
                        name.trim(),
                        notes.ifBlank { null },
                        selectedCategoryId,
                        curr,
                        isinTrimmed,
                        selectedPlatformIds,
                        maturityToSave,
                        compositionToSave,
                        sectorsToSave,
                        regionsToSave,
                        currentPortfolioId
                    )
                },
                enabled  = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    text       = if (isEditing) stringResource(Res.string.portfolio_add_asset_save_changes) else stringResource(Res.string.portfolio_add_asset_create),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // ── Ayuda: taxonomía de tipos de activo ──────────────────────────────
    if (showCategoryHelp) {
        AlertDialog(
            onDismissRequest = { showCategoryHelp = false },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text("Tipos de activo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.textPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AssetTypeHelpRow("📈", "Acciones / ETFs / Fondos", "Cotizados con precio de mercado. Admiten análisis por sectores y regiones geográficas.")
                    AssetTypeHelpRow("💶", "Renta fija", "Bonos y depósitos con rendimiento acordado. Requieren fecha de vencimiento; sin precio de mercado automático.")
                    AssetTypeHelpRow("₿", "Cripto / Materias primas / Crowdlending", "Activos alternativos con precio de mercado pero sin análisis sectorial.")
                    AssetTypeHelpRow("🏠", "Inmuebles", "Propiedades físicas valoradas manualmente; sin precio de mercado automático.")
                    AssetTypeHelpRow("💎", "Valiosos", "Arte, coleccionables u otros activos tangibles no financieros.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryHelp = false }) {
                    Text("Entendido", color = MaterialTheme.appColors.primary, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
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
                    if (selected != null && selected > nowMillis()) {
                        maturityDateMillis = selected
                    }
                    showMaturityDatePicker = false
                }) { Text(stringResource(Res.string.common_accept), color = MaterialTheme.appColors.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showMaturityDatePicker = false }) {
                    Text(stringResource(Res.string.portfolio_add_asset_cancel), color = MaterialTheme.appColors.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.appColors.surface)
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = MaterialTheme.appColors.primary,
                    todayDateBorderColor = MaterialTheme.appColors.primary
                )
            )
        }
    }
}

@Composable
private fun AssetTypeHelpRow(icon: String, title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(28.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.appColors.textPrimary)
            Text(description, fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary)
        }
    }
}

@Preview
@Composable
private fun AddEditAssetBottomSheetPreview() {
    N3toTheme {
        val now = nowMillis()
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
            preselectedCategoryId = null,
            allPlatforms = samplePlatforms,
            linkedPlatformIds = emptySet(),
            allSectors = emptyList(),
            linkedSectorIds = emptySet(),
            allRegions = emptyList(),
            linkedRegionPercents = emptyMap(),
            linkedFixedIncomePercent = 0,
            onSave = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
            onValidateIsin = null,
            onDismiss = {}
        )
    }
}

