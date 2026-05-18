package es.aviferdev.n3to.ui.account

import es.aviferdev.n3to.platform.nowMillis
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
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.account_add_title
import n3to.composeapp.generated.resources.account_edit_title
import n3to.composeapp.generated.resources.account_name_placeholder
import n3to.composeapp.generated.resources.portfolio_add_asset_save
import n3to.composeapp.generated.resources.portfolio_name_required
import n3to.composeapp.generated.resources.portfolio_platform_name
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bottom sheet para crear o editar una cuenta.
 * En creación solo pide nombre — el saldo inicial se configura
 * en un paso posterior obligatorio (SetInitialBalanceBottomSheet).
 * Todas las cuentas usan euros (€).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountBottomSheet(
    account: Account?,  // null = crear
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = account != null

    var name      by remember { mutableStateOf(account?.name ?: "") }
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
                text       = if (isEditing) stringResource(Res.string.account_edit_title) else stringResource(Res.string.account_add_title),
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary,
                modifier   = Modifier.padding(bottom = 20.dp)
            )

            // Nombre
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text(stringResource(Res.string.portfolio_platform_name)) },
                placeholder   = { Text(stringResource(Res.string.account_name_placeholder)) },
                supportingText = if (nameError) {{ Text(stringResource(Res.string.portfolio_name_required)) }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

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
                    onSave(name.trim())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(
                    text       = if (isEditing) stringResource(Res.string.portfolio_add_asset_save) else "Continuar",
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
            onSave = {},
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
                createdAt = nowMillis()
            ),
            onSave = {},
            onDismiss = {}
        )
    }
}
