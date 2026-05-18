package es.aviferdev.n3to.ui.portfolio

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
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.portfolio.PortfolioCalculator
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Sheet para realizar un traspaso entre fondos de inversión o planes de
 * pensiones. El traspaso es fiscalmente neutro (sin hecho imponible en España):
 * el coste base FIFO del fondo origen se arrastra al fondo destino.
 *
 * Campos del formulario:
 * - Fondo destino (selector entre fondos traspasables de la misma cuenta)
 * - Cantidad de participaciones a traspasar del origen
 * - Plataforma origen (de dónde salen las participaciones)
 * - Plataforma destino (donde se suscriben las nuevas)
 * - VL (valor liquidativo) del fondo destino
 * - Fecha del traspaso
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferFundBottomSheet(
    sourceAsset: Asset,
    destinations: List<Asset>,
    platforms: List<Platform>,
    assetTransactions: List<AssetTransaction>,
    onExecuteTransfer: (
        destinationAssetId: String,
        quantity: Double,
        sourcePlatformId: String,
        destinationPlatformId: String,
        destinationPricePerUnit: Double,
        date: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {

    // ── Estado del formulario ────────────────────────────────────────────────
    var selectedDestinationId by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var sourcePlatformId by remember { mutableStateOf(platforms.firstOrNull()?.id) }
    var destinationPlatformId by remember { mutableStateOf(platforms.firstOrNull()?.id) }
    var destinationVL by remember { mutableStateOf("") }
    var dateMillis by remember { mutableStateOf(nowMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // ── Validación ───────────────────────────────────────────────────────────
    val parsedQty = quantity.replace(',', '.').toDoubleOrNull()
    val parsedVL  = destinationVL.replace(',', '.').toDoubleOrNull()
    val now       = nowMillis()

    val availableForTransfer: Double = if (sourcePlatformId != null) {
        PortfolioCalculator.availableQuantityAt(
            transactions = assetTransactions,
            asOfDate     = dateMillis,
            platformId   = sourcePlatformId!!
        )
    } else {
        PortfolioCalculator.availableQuantityAt(
            transactions = assetTransactions,
            asOfDate     = dateMillis
        )
    }

    val transferExceeds = parsedQty != null && parsedQty > availableForTransfer

    val isValid = selectedDestinationId != null
        && parsedQty != null && parsedQty > 0.0
        && parsedVL != null && parsedVL > 0.0
        && sourcePlatformId != null
        && destinationPlatformId != null
        && dateMillis <= now
        && !transferExceeds

    val selectedDestination = remember(selectedDestinationId, destinations) {
        destinations.firstOrNull { it.id == selectedDestinationId }
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
                text       = "Traspaso de fondo",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text     = "Traspaso fiscalmente neutro. El coste base se arrastra al fondo destino.",
                fontSize = 12.sp,
                color    = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ── Fondo origen (solo lectura) ──────────────────────────────────
            Text("Fondo origen", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
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
                        text       = sourceAsset.ticker.take(3),
                        fontSize   = if (sourceAsset.ticker.length > 3) 9.sp else 11.sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(sourceAsset.name, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    Text(sourceAsset.ticker, fontSize = 11.sp, color = TextSecondary)
                }
                Text("↗", fontSize = 20.sp, color = ExpenseRed)
            }
            Spacer(Modifier.height(16.dp))

            // ── Fondo destino (selector) ─────────────────────────────────────
            Text("Fondo destino", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            if (destinations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = "No hay otros fondos traspasables en esta cuenta. Crea primero el fondo destino desde Ajustes › Portfolio.",
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                }
            } else {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    destinations.forEach { asset ->
                        DestinationChip(
                            ticker     = asset.ticker,
                            name       = asset.name,
                            isSelected = selectedDestinationId == asset.id,
                            onClick    = { selectedDestinationId = asset.id }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Cantidad de participaciones a traspasar ──────────────────────
            OutlinedTextField(
                value         = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label         = { Text("Participaciones a traspasar") },
                placeholder   = { Text("0") },
                isError       = transferExceeds,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = if (transferExceeds) ExpenseRed else PrimaryDark,
                    unfocusedBorderColor = if (transferExceeds) ExpenseRed else BorderGray
                )
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text     = if (transferExceeds)
                    "Solo tienes ${formatQty(availableForTransfer)} participaciones disponibles a esa fecha"
                else
                    "Disponible: ${formatQty(availableForTransfer)} participaciones a esa fecha",
                fontSize = 11.sp,
                color    = if (transferExceeds) ExpenseRed else TextSecondary
            )
            Spacer(Modifier.height(12.dp))

            // ── VL del fondo destino ─────────────────────────────────────────
            OutlinedTextField(
                value         = destinationVL,
                onValueChange = { destinationVL = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label         = { Text("VL fondo destino (precio por participación)") },
                placeholder   = { Text("0,00") },
                trailingIcon  = { Text("€", color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
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

            // ── Fecha ────────────────────────────────────────────────────────
            Text("Fecha del traspaso", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
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

            // ── Plataforma origen ────────────────────────────────────────────
            Text("Plataforma origen", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            if (platforms.isEmpty()) {
                Text(
                    text     = "Sin plataformas disponibles.",
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            } else {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.forEach { p ->
                        TransferPlatformChip(
                            icon       = p.icon,
                            label      = p.name,
                            isSelected = sourcePlatformId == p.id,
                            onClick    = { sourcePlatformId = p.id }
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // ── Plataforma destino ───────────────────────────────────────────
            Text("Plataforma destino", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            if (platforms.isEmpty()) {
                Text(
                    text     = "Sin plataformas disponibles.",
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            } else {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    platforms.forEach { p ->
                        TransferPlatformChip(
                            icon       = p.icon,
                            label      = p.name,
                            isSelected = destinationPlatformId == p.id,
                            onClick    = { destinationPlatformId = p.id }
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Botón de confirmar ───────────────────────────────────────────
            Button(
                onClick = {
                    val destId     = selectedDestinationId    ?: return@Button
                    val srcPlat    = sourcePlatformId         ?: return@Button
                    val dstPlat    = destinationPlatformId    ?: return@Button
                    val qty        = parsedQty                ?: return@Button
                    val vl         = parsedVL                 ?: return@Button
                    onExecuteTransfer(destId, qty, srcPlat, dstPlat, vl, dateMillis)
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
                    text       = "Confirmar traspaso",
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
private fun DestinationChip(
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
private fun TransferPlatformChip(
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

@Preview
@Composable
private fun TransferFundBottomSheetPreview() {
    N3toTheme {
        val now = nowMillis()
        val sourceAsset = Asset(
            id = "asset-1",
            accountId = "acc-1",
            ticker = "SAN",
            name = "Fondo Santander España",
            notes = null,
            createdAt = now,
            assetCategoryId = "fixed_cat_funds"
        )
        val destinationAssets = listOf(
            Asset(id = "asset-2", accountId = "acc-1", ticker = "BBVA", name = "Fondo BBVA España", notes = null, createdAt = now, assetCategoryId = "fixed_cat_funds"),
            Asset(id = "asset-3", accountId = "acc-1", ticker = "ING", name = "Fondo ING España", notes = null, createdAt = now, assetCategoryId = "fixed_cat_funds")
        )
        val platforms = listOf(
            Platform("platform-1", "Banco Santander", "🏦", 0, false, now),
            Platform("platform-2", "BBVA", "🏛️", 1, false, now)
        )
        val transactions = emptyList<AssetTransaction>()

        TransferFundBottomSheet(
            sourceAsset = sourceAsset,
            destinations = destinationAssets,
            platforms = platforms,
            assetTransactions = transactions,
            onExecuteTransfer = { _, _, _, _, _, _ -> },
            onDismiss = {}
        )
    }
}
