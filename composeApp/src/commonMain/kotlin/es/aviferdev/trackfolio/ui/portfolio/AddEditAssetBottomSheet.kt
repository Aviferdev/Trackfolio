package es.aviferdev.trackfolio.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Asset
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAssetBottomSheet(
    asset: Asset?,          // null = crear
    onSave: (ticker: String, name: String, quantity: Double, purchasePrice: Double, purchaseDate: Long, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = asset != null

    var ticker        by remember { mutableStateOf(asset?.ticker ?: "") }
    var name          by remember { mutableStateOf(asset?.name ?: "") }
    var quantity      by remember { mutableStateOf(asset?.quantity?.toString() ?: "") }
    var purchasePrice by remember { mutableStateOf(asset?.purchasePrice?.toString() ?: "") }
    var notes         by remember { mutableStateOf(asset?.notes ?: "") }

    var tickerError by remember { mutableStateOf(false) }
    var nameError   by remember { mutableStateOf(false) }

    val isValid = ticker.isNotBlank() && name.isNotBlank()
        && quantity.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
        && purchasePrice.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true

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
                    .background(Color(0xFFBDBDBD))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text       = if (isEditing) "Editar posición" else "Nueva posición",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 20.dp)
            )

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

            // Cantidad + Precio en fila
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
                    val date  = asset?.purchaseDate ?: Clock.System.now().toEpochMilliseconds()
                    onSave(ticker.trim(), name.trim(), qty, price, date, notes.ifBlank { null })
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
