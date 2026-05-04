package es.aviferdev.trackfolio.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.*

/**
 * Bottom sheet para crear o editar una cuenta.
 * En creación solo pide nombre y moneda — el saldo inicial se configura
 * en un paso posterior obligatorio (SetInitialBalanceBottomSheet).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountBottomSheet(
    account: es.aviferdev.trackfolio.domain.model.Account?,  // null = crear
    onSave: (name: String, currency: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = account != null

    var name             by remember { mutableStateOf(account?.name ?: "") }
    var currency         by remember { mutableStateOf(account?.currency ?: "EUR") }
    var nameError        by remember { mutableStateOf(false) }
    var expandedCurrency by remember { mutableStateOf(false) }

    val currencies = listOf("EUR", "USD", "GBP", "CHF", "JPY", "MXN", "ARS", "CLP")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .imePadding()
        ) {
            Text(
                text       = if (isEditing) "Editar cuenta" else "Nueva cuenta",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 20.dp)
            )

            // Nombre
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Nombre") },
                placeholder   = { Text("Ej. Cuenta corriente BBVA") },
                isError       = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(16.dp))

            // Moneda
            ExposedDropdownMenuBox(
                expanded         = expandedCurrency,
                onExpandedChange = { expandedCurrency = !expandedCurrency }
            ) {
                OutlinedTextField(
                    value         = currency,
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Moneda") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCurrency) },
                    modifier      = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape  = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    )
                )
                ExposedDropdownMenu(
                    expanded         = expandedCurrency,
                    onDismissRequest = { expandedCurrency = false }
                ) {
                    currencies.forEach { c ->
                        DropdownMenuItem(
                            text    = { Text(c) },
                            onClick = { currency = c; expandedCurrency = false }
                        )
                    }
                }
            }

            if (!isEditing) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text     = "A continuación deberás establecer el saldo inicial de la cuenta.",
                    fontSize = 12.sp,
                    color    = TextSecondary
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    onSave(name.trim(), currency)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(
                    text       = if (isEditing) "Guardar cambios" else "Continuar",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
