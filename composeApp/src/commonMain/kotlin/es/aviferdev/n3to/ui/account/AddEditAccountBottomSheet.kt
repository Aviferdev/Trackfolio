package es.aviferdev.n3to.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.domain.model.AccountType
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bottom sheet para crear o editar una cuenta.
 * En creación solo pide nombre y tipo — el saldo inicial se configura
 * en un paso posterior obligatorio (SetInitialBalanceBottomSheet).
 * Todas las cuentas usan euros (€).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountBottomSheet(
    account: Account?,  // null = crear
    onSave: (name: String, accountType: AccountType) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = account != null

    var name      by remember { mutableStateOf(account?.name ?: "") }
    var isCash    by remember { mutableStateOf(account?.accountType == AccountType.CASH) }
    var nameError by remember { mutableStateOf(false) }

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

            // Tipo de cuenta: efectivo
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "\uD83D\uDCB5 Cuenta de efectivo",
                        fontSize   = 15.sp,
                        color      = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text     = "Permite reconciliar el saldo con el efectivo real",
                        fontSize = 12.sp,
                        color    = TextSecondary
                    )
                }
                Switch(
                    checked         = isCash,
                    onCheckedChange = { isCash = it },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor   = SurfaceWhite,
                        checkedTrackColor   = PrimaryDark,
                        uncheckedThumbColor = SurfaceWhite,
                        uncheckedTrackColor = BorderGray
                    )
                )
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
                    val type = if (isCash) AccountType.CASH else AccountType.GENERAL
                    onSave(name.trim(), type)
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

@Preview
@Composable
private fun AddEditAccountBottomSheetCreatePreview() {
    N3toTheme {
        AddEditAccountBottomSheet(
            account = null,
            onSave = { _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddEditAccountBottomSheetEditPreview() {
    N3toTheme {
        AddEditAccountBottomSheet(
            account = Account(
                id = "1",
                name = "Cuenta Principal",
                initialBalance = 5000.0,
                computedBalance = 5200.0,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                accountType = AccountType.GENERAL
            ),
            onSave = { _, _ -> },
            onDismiss = {}
        )
    }
}
