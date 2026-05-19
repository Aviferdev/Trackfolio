package es.aviferdev.n3to.ui.portfolio

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
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
import es.aviferdev.n3to.domain.model.Asset
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.ui.theme.*

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import es.aviferdev.n3to.ui.portfolio.components.*
import n3to.composeapp.generated.resources.portfolio_add_tx_accept
import n3to.composeapp.generated.resources.portfolio_add_tx_available
import n3to.composeapp.generated.resources.portfolio_add_tx_cancel
import n3to.composeapp.generated.resources.portfolio_add_tx_category_all
import n3to.composeapp.generated.resources.portfolio_add_tx_category_filter
import n3to.composeapp.generated.resources.portfolio_add_tx_date_label
import n3to.composeapp.generated.resources.portfolio_add_tx_fee_hint
import n3to.composeapp.generated.resources.portfolio_add_tx_fee_label
import n3to.composeapp.generated.resources.portfolio_add_tx_fee_placeholder
import n3to.composeapp.generated.resources.portfolio_add_tx_new
import n3to.composeapp.generated.resources.portfolio_add_tx_no_assets_category
import n3to.composeapp.generated.resources.portfolio_add_tx_notes_label
import n3to.composeapp.generated.resources.portfolio_add_tx_only_available
import n3to.composeapp.generated.resources.portfolio_add_tx_platform_label
import n3to.composeapp.generated.resources.portfolio_add_tx_platform_label_inline
import n3to.composeapp.generated.resources.portfolio_add_tx_platform_not_assigned
import n3to.composeapp.generated.resources.portfolio_add_tx_platform_select_hint
import n3to.composeapp.generated.resources.portfolio_add_tx_price_label
import n3to.composeapp.generated.resources.portfolio_add_tx_qty_label
import n3to.composeapp.generated.resources.portfolio_add_tx_save_changes
import n3to.composeapp.generated.resources.portfolio_add_tx_select_asset
import n3to.composeapp.generated.resources.portfolio_add_tx_title_edit
import n3to.composeapp.generated.resources.portfolio_add_tx_title_new
import n3to.composeapp.generated.resources.portfolio_add_tx_type_buy
import n3to.composeapp.generated.resources.portfolio_add_tx_type_sell
import n3to.composeapp.generated.resources.portfolio_add_tx_units_label
import org.jetbrains.compose.resources.stringResource

/**
 * Sheet para registrar (o editar) un movimiento de compra/venta sobre un
 * activo. Validaciones:
 *
 * - Plataforma obligatoria (decisión 3.A). Las plataformas deben crearse
 *   previamente desde Ajustes › Portfolio › Plataformas.
 * - Para SELL: solo se muestran habilitadas las plataformas donde el activo
 *   tiene unidades disponibles. El resto aparece deshabilitado. No se
 *   preselecciona ninguna — el usuario debe elegir explícitamente.
 * - Bloqueo de sobreventa (decisión 7.A): para una venta, la cantidad no
 *   puede exceder las unidades disponibles en la plataforma seleccionada
 *   a la fecha indicada.
 * - Fee como texto libre informativo (decisión 5.C): no entra en cálculos.
 *
 * @param assetTransactions movimientos del activo seleccionado, para
 *        validar la sobreventa con FIFO.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetTransactionBottomSheet(
    transaction: AssetTransaction?,             // null = crear
    fixedAsset: Asset?,                          // si != null, no se permite cambiar de activo
    allAssets: List<Asset>,                      // catálogo (para el selector)
    platforms: List<Platform>,                   // plataformas activas (todas)
    platformsByAsset: Map<String, List<Platform>>, // plataformas vinculadas por activo
    categories: List<AssetCategory>,             // categorías para filtrar
    assetTransactions: List<AssetTransaction>,   // movimientos del activo seleccionado actual
    buyOnly: Boolean = false,                    // si true, no se muestra el toggle y siempre es BUY
    selectedPortfolioId: String? = null,         // cartera activa para asignar al asset
    onSave: (
        assetId: String,
        type: AssetTransactionType,
        quantity: Double,
        pricePerUnit: Double,
        date: Long,
        platformId: String,
        feeNote: String?,
        notes: String?,
        portfolioId: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = transaction != null

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
        mutableStateOf(transaction?.date ?: nowMillis())
    }
    var platformId by remember(transaction) {
        mutableStateOf(transaction?.platformId)
    }
    var feeNote by remember(transaction) {
        mutableStateOf(transaction?.feeNote ?: "")
    }
    var notes by remember(transaction) {
        mutableStateOf(transaction?.notes ?: "")
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }

    // Filtrar activos por categoría seleccionada (excluir Renta Fija - no admiten compra/venta)
    val filteredAssets = remember(selectedCategoryId, allAssets) {
        val baseAssets = if (selectedCategoryId == null) allAssets
        else allAssets.filter { it.assetCategoryId == selectedCategoryId }
        // Excluir activos de Renta Fija de la lista de compra/venta
        baseAssets.filter { !AssetCategoryType.isFixedIncome(it.assetCategoryId) }
    }

    // Resetear selectedAssetId si el activo actual no pertenece a la nueva categoría
    LaunchedEffect(selectedCategoryId, allAssets) {
        if (selectedAssetId != null) {
            val currentAsset = allAssets.firstOrNull { it.id == selectedAssetId }
            if (currentAsset != null && selectedCategoryId != null && currentAsset.assetCategoryId != selectedCategoryId) {
                selectedAssetId = filteredAssets.firstOrNull()?.id
            }
        }
    }

    // ── Disponibilidad por plataforma (para ventas) ─────────────────────────
    val relevantTransactions = remember(selectedAssetId, assetTransactions) {
        assetTransactions.filter { it.assetId == selectedAssetId }
    }

    // Plataformas vinculadas al activo seleccionado o a los activos de la categoría seleccionada
    val visiblePlatforms: List<Platform> = remember(selectedAssetId, selectedCategoryId, platformsByAsset, platforms, filteredAssets) {
        // Primero obtener las plataformas de los activos filtrados por categoría
        val categoryAssetIds = if (selectedCategoryId != null) {
            filteredAssets.map { it.id }.toSet()
        } else {
            emptySet()
        }

        val platformsForFilteredAssets = if (categoryAssetIds.isNotEmpty()) {
            categoryAssetIds.mapNotNull { platformsByAsset[it] }.flatten().distinctBy { it.id }
        } else {
            emptyList()
        }

        // Si hay un activo seleccionado específicamente, usar sus plataformas
        val linked = selectedAssetId?.let { platformsByAsset[it] }

        when {
            // Si hay activo seleccionado con plataformas vinculadas, usar esas
            linked != null && linked.isNotEmpty() -> linked
            // Si hay activos filtrados con plataformas vinculadas, usar esas
            platformsForFilteredAssets.isNotEmpty() -> platformsForFilteredAssets
            // Si no hay categoría seleccionada ni activo con plataformas, mostrar todas
            else -> platforms
        }
    }
    val hasLinkedPlatforms: Boolean = remember(selectedAssetId, selectedCategoryId, platformsByAsset, filteredAssets) {
        when {
            selectedAssetId != null -> platformsByAsset[selectedAssetId]?.isNotEmpty() ?: false
            selectedCategoryId != null -> filteredAssets.any { platformsByAsset[it.id]?.isNotEmpty() == true }
            else -> false
        }
    }

    val availableByPlatform: Map<String, Double> = remember(relevantTransactions, dateMillis, transaction, visiblePlatforms) {
        visiblePlatforms.associate { p ->
            p.id to PortfolioCalculator.availableQuantityAt(
                transactions           = relevantTransactions,
                asOfDate               = dateMillis,
                platformId             = p.id,
                excludingTransactionId = transaction?.id
            )
        }
    }

    val platformsWithStock: Set<String> = remember(availableByPlatform) {
        availableByPlatform.filterValues { it > 0.0 }.keys
    }

    // Al cambiar a SELL: limpiar plataforma si la actual no tiene stock
    LaunchedEffect(type) {
        if (type == AssetTransactionType.SELL) {
            if (platformId != null && platformId !in platformsWithStock) {
                platformId = null
            }
        } else if (platformId == null && transaction == null) {
            // Para BUY sin edición, preseleccionar la primera de las visibles
            platformId = visiblePlatforms.firstOrNull()?.id
        }
    }

    // Al cambiar de categoría, limpiar plataforma si ya no está en las visibles
    LaunchedEffect(visiblePlatforms) {
        if (platformId != null && visiblePlatforms.none { it.id == platformId }) {
            platformId = visiblePlatforms.firstOrNull()?.id
        }
    }

    // ── Validación ───────────────────────────────────────────────────────────
    val parsedQty   = quantity.replace(',', '.').toDoubleOrNull()
    val parsedPrice = pricePerUnit.replace(',', '.').toDoubleOrNull()
    val now         = nowMillis()

    val isSell = type == AssetTransactionType.SELL

    val availableForSale: Double = if (isSell && platformId != null) {
        availableByPlatform[platformId] ?: 0.0
    } else if (isSell) {
        // Sin plataforma seleccionada: mostrar total global como referencia
        PortfolioCalculator.availableQuantityAt(
            transactions           = relevantTransactions,
            asOfDate               = dateMillis,
            excludingTransactionId = transaction?.id
        )
    } else 0.0

    val sellExceeds = isSell
        && parsedQty != null && platformId != null
        && parsedQty > (availableByPlatform[platformId] ?: 0.0)

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
        containerColor   = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.border)
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
                text       = if (isEditing) stringResource(Res.string.portfolio_add_tx_title_edit) else stringResource(Res.string.portfolio_add_tx_title_new),
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.appColors.textPrimary,
                modifier   = Modifier.padding(bottom = 16.dp)
            )

            // ── Tipo BUY/SELL (oculto en modo buyOnly) ────────────────────────
            if (!buyOnly) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.appColors.surfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TypeToggle(
                        label    = stringResource(Res.string.portfolio_add_tx_type_buy),
                        isSel    = type == AssetTransactionType.BUY,
                        selColor = IncomeGreen,
                        modifier = Modifier.weight(1f),
                        onClick  = { type = AssetTransactionType.BUY }
                    )
                    TypeToggle(
                        label    = stringResource(Res.string.portfolio_add_tx_type_sell),
                        isSel    = type == AssetTransactionType.SELL,
                        selColor = ExpenseRed,
                        modifier = Modifier.weight(1f),
                        onClick  = { type = AssetTransactionType.SELL }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Selector de activo (oculto si fixedAsset != null) ────────────
            if (fixedAsset == null) {
                // Filtro de categorías (excluir Renta Fija - no admiten compra/venta)
                val investmentCategories = remember(categories) {
                    categories.filter { !AssetCategoryType.isFixedIncome(it.id) }
                }
                if (investmentCategories.isNotEmpty()) {
                    Text(stringResource(Res.string.portfolio_add_tx_category_filter), fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryFilterChip(
                            label    = stringResource(Res.string.portfolio_add_tx_category_all),
                            isSelected = selectedCategoryId == null,
                            onClick  = { selectedCategoryId = null }
                        )
                        investmentCategories.forEach { category ->
                            CategoryFilterChip(
                                label    = category.name,
                                icon     = category.icon,
                                isSelected = selectedCategoryId == category.id,
                                onClick  = { selectedCategoryId = category.id }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                Text(stringResource(Res.string.portfolio_add_tx_select_asset), fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                if (filteredAssets.isEmpty()) {
                    if (selectedCategoryId != null && allAssets.isNotEmpty()) {
                        Text(
                            text     = stringResource(Res.string.portfolio_add_tx_no_assets_category),
                            fontSize = 12.sp,
                            color    = MaterialTheme.appColors.textSecondary
                        )
                    } else {
                        EmptyAssetsHint()
                    }
                } else {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredAssets.forEach { asset ->
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
                        .background(MaterialTheme.appColors.surfaceElevated)
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
                        Text(fixedAsset.name, fontSize = 14.sp, color = MaterialTheme.appColors.textPrimary, fontWeight = FontWeight.Medium)
                        Text(fixedAsset.ticker, fontSize = 11.sp, color = MaterialTheme.appColors.textSecondary)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Plataforma (obligatoria — antes de cantidad para ventas) ─────
            Text(stringResource(Res.string.portfolio_add_tx_platform_label), fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary, fontWeight = FontWeight.Medium)
            if (isSell && platformId == null && platformsWithStock.isNotEmpty()) {
                Text(
                    text     = stringResource(Res.string.portfolio_add_tx_platform_select_hint),
                    fontSize = 11.sp,
                    color    = ExpenseRed.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(8.dp))
            if (visiblePlatforms.isEmpty()) {
                EmptyPlatformsInlineHint()
            } else {
                // Hint cuando el activo no tiene plataformas vinculadas
                if (!hasLinkedPlatforms && selectedAssetId != null) {
                    Text(
                        text     = stringResource(Res.string.portfolio_add_tx_platform_not_assigned),
                        fontSize = 11.sp,
                        color    = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    visiblePlatforms.forEach { p ->
                        val hasStock = platformsWithStock.contains(p.id)
                        val enabled = !isSell || hasStock
                        val available = availableByPlatform[p.id] ?: 0.0

                        PlatformChip(
                            icon       = p.icon,
                            label      = p.name,
                            isSelected = platformId == p.id,
                            enabled    = enabled,
                            badge      = if (isSell && hasStock) "${formatQty(available)} ${stringResource(Res.string.portfolio_add_tx_units_label)}" else null,
                            onClick    = { if (enabled) platformId = p.id }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Cantidad + precio ────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text(stringResource(Res.string.portfolio_add_tx_qty_label)) },
                    placeholder   = { Text("0") },
                    isError       = sellExceeds,
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = if (sellExceeds) ExpenseRed else PrimaryDark,
                        unfocusedBorderColor = if (sellExceeds) ExpenseRed else MaterialTheme.appColors.border
                    )
                )
                OutlinedTextField(
                    value         = pricePerUnit,
                    onValueChange = { pricePerUnit = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text(stringResource(Res.string.portfolio_add_tx_price_label)) },
                    placeholder   = { Text("0,00") },
                    trailingIcon  = { Text("€", color = MaterialTheme.appColors.textSecondary, modifier = Modifier.padding(end = 12.dp)) },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryDark,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    )
                )
            }
            if (isSell && selectedAssetId != null) {
                Spacer(Modifier.height(6.dp))
                val displayAvailable = if (platformId != null)
                    availableByPlatform[platformId] ?: 0.0
                else
                    availableForSale
                val platformName = platforms.firstOrNull { it.id == platformId }?.name ?: ""
                val platformLabel = if (platformId != null)
                    stringResource(Res.string.portfolio_add_tx_platform_label_inline, platformName)
                else ""
                Text(
                    text     = if (sellExceeds)
                        stringResource(Res.string.portfolio_add_tx_only_available, formatQty(displayAvailable), platformLabel)
                    else
                        stringResource(Res.string.portfolio_add_tx_available, formatQty(displayAvailable), platformLabel),
                    fontSize = 11.sp,
                    color    = if (sellExceeds) ExpenseRed else MaterialTheme.appColors.textSecondary
                )
            }
            Spacer(Modifier.height(12.dp))

            // ── Fecha ────────────────────────────────────────────────────────
            Text(stringResource(Res.string.portfolio_add_tx_date_label), fontSize = 12.sp, color = MaterialTheme.appColors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(0.5.dp, MaterialTheme.appColors.border, RoundedCornerShape(10.dp))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text     = formatFullDate(dateMillis),
                    fontSize = 14.sp,
                    color    = MaterialTheme.appColors.textPrimary
                )
            }
            Spacer(Modifier.height(12.dp))

            // ── Comisión informativa ────────────────────────────────────────
            OutlinedTextField(
                value         = feeNote,
                onValueChange = { feeNote = it },
                label         = { Text(stringResource(Res.string.portfolio_add_tx_fee_label)) },
                placeholder   = { Text(stringResource(Res.string.portfolio_add_tx_fee_placeholder)) },
                supportingText = {
                    Text(
                        text     = stringResource(Res.string.portfolio_add_tx_fee_hint),
                        fontSize = 11.sp,
                        color    = MaterialTheme.appColors.textSecondary
                    )
                },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                )
            )
            Spacer(Modifier.height(12.dp))

            // ── Notas ───────────────────────────────────────────────────────
            OutlinedTextField(
                value         = notes,
                onValueChange = { notes = it },
                label         = { Text(stringResource(Res.string.portfolio_add_tx_notes_label)) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = MaterialTheme.appColors.border
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
                        notes.ifBlank { null },
                        selectedPortfolioId
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
                    text       = if (isEditing) stringResource(Res.string.portfolio_add_tx_save_changes) else stringResource(Res.string.portfolio_add_tx_new),
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
                    if (selected != null && selected <= nowMillis()) {
                        dateMillis = selected
                    }
                    showDatePicker = false
                }) { Text(stringResource(Res.string.portfolio_add_tx_accept), color = PrimaryDark) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.portfolio_add_tx_cancel), color = MaterialTheme.appColors.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = MaterialTheme.appColors.surface)
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

