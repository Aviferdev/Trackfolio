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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.domain.model.AssetCategory
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetBottomSheet(
    asset: Asset?,                          // null = crear
    categories: List<AssetCategory>,        // categorías activas disponibles
    currencyCode: String = "EUR",           // moneda de la cuenta seleccionada
    onSave: (
        ticker: String,
        name: String,
        quantity: Double,
        purchasePrice: Double,
        purchaseDate: Long,
        notes: String?,
        assetCategoryId: String?,
        currentPrice: Double?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = asset != null
    val currencySymbol = currencySymbol(currencyCode)

    var ticker        by remember { mutableStateOf(asset?.ticker ?: "") }
    var name          by remember { mutableStateOf(asset?.name ?: "") }
    var quantity      by remember { mutableStateOf(asset?.quantity?.toString() ?: "") }
    var purchasePrice by remember { mutableStateOf(asset?.purchasePrice?.toString() ?: "") }
    var currentPrice  by remember { mutableStateOf(asset?.currentPrice?.toString() ?: "") }
    var notes         by remember { mutableStateOf(asset?.notes ?: "") }
    var selectedCategoryId by remember { mutableStateOf(asset?.assetCategoryId) }

    var tickerError by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }

    val isValid = ticker.isNotBlank() && name.isNotBlank()
        && quantity.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
        && purchasePrice.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
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
                text       = if (isEditing) "Editar posición" else "Nueva posición",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 20.dp)
            )

            // ── Selector de categoría ────────────────────────────────────────
            if (categories.isNotEmpty()) {
                Text(
                    text       = "Categoría",
                    fontSize   = 12.sp,
                    color      = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chip "Sin categoría"
                    CategoryChip(
                        icon       = "❔",
                        label      = "Sin categoría",
                        isSelected = selectedCategoryId == null,
                        onClick    = { selectedCategoryId = null }
                    )
                    categories.forEach { cat ->
                        CategoryChip(
                            icon       = cat.icon,
                            label      = cat.name,
                            isSelected = selectedCategoryId == cat.id,
                            onClick    = { selectedCategoryId = cat.id }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                // Sin categorías creadas: mostrar hint para crearlas en Ajustes
                Text(
                    text     = "💡  Puedes crear categorías de activos desde Ajustes para agruparlas (ej. Cryptos, ETFs).",
                    fontSize = 11.sp,
                    color    = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Ticker
            OutlinedTextField(
                value         = ticker,
                onValueChange = { ticker = it.uppercase(); tickerError = false },
                label         = { Text("Ticker / Símbolo") },
                placeholder   = { Text("Ej. AAPL, BTC, IAG.MC") },
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
                placeholder   = { Text("Ej. Apple Inc., Bitcoin") },
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

            // Cantidad + Precio compra en fila
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value         = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text("Cantidad") },
                    placeholder   = { Text("0") },
                    modifier      = Modifier.weight(1f),
                    singleLine    = true,
                    shape         = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    )
                )
                OutlinedTextField(
                    value         = purchasePrice,
                    onValueChange = { purchasePrice = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                    label         = { Text("Precio compra") },
                    placeholder   = { Text("0,00") },
                    trailingIcon  = { Text(currencySymbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
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
            Spacer(Modifier.height(12.dp))

            // Precio actual (opcional) — habilita el cálculo de revalorización
            OutlinedTextField(
                value         = currentPrice,
                onValueChange = { currentPrice = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                label         = { Text("Precio actual (opcional)") },
                placeholder   = { Text("0,00") },
                trailingIcon  = { Text(currencySymbol, color = TextSecondary, modifier = Modifier.padding(end = 12.dp)) },
                supportingText = {
                    Text(
                        text     = "Déjalo vacío si todavía no lo conoces. Lo puedes actualizar después.",
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
                    val qty   = quantity.replace(',', '.').toDoubleOrNull() ?: return@Button
                    val price = purchasePrice.replace(',', '.').toDoubleOrNull() ?: return@Button
                    val curr  = currentPrice.replace(',', '.').toDoubleOrNull()
                    val date  = asset?.purchaseDate ?: Clock.System.now().toEpochMilliseconds()
                    onSave(
                        ticker.trim(),
                        name.trim(),
                        qty,
                        price,
                        date,
                        notes.ifBlank { null },
                        selectedCategoryId,
                        curr
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
                    text       = if (isEditing) "Guardar cambios" else "Añadir posición",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
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
