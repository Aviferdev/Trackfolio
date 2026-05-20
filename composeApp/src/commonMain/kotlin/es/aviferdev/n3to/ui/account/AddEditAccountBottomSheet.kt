package es.aviferdev.n3to.ui.account

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Account
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.account_add_title
import n3to.composeapp.generated.resources.account_edit_title
import n3to.composeapp.generated.resources.account_initial_balance_desc
import n3to.composeapp.generated.resources.account_initial_balance_label
import n3to.composeapp.generated.resources.account_initial_balance_valid_hint
import n3to.composeapp.generated.resources.account_name_placeholder
import n3to.composeapp.generated.resources.account_name_supporting_text
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.portfolio_add_asset_save
import n3to.composeapp.generated.resources.portfolio_name_required
import n3to.composeapp.generated.resources.portfolio_platform_name
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountBottomSheet(
    account: Account?,
    onSave: (name: String, initialBalance: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = account != null

    var name         by remember { mutableStateOf(account?.name ?: "") }
    var nameError    by remember { mutableStateOf(false) }
    var balanceText  by remember { mutableStateOf("") }
    var balanceError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.appColors.navySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.navyBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text       = if (isEditing) stringResource(Res.string.account_edit_title)
                             else stringResource(Res.string.account_add_title),
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = if (isEditing)
                    "Cambia el nombre de la cuenta."
                else
                    "Crea un bolsillo para agrupar tus movimientos: banco, efectivo, inversión...",
                fontSize = 11.sp,
                color    = MaterialTheme.appColors.textSecondary
            )

            Spacer(Modifier.height(20.dp))

            // ── Nombre ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text(stringResource(Res.string.portfolio_platform_name)) },
                placeholder   = { Text(stringResource(Res.string.account_name_placeholder)) },
                isError       = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(Res.string.portfolio_name_required)) }
                } else {
                    { Text(stringResource(Res.string.account_name_supporting_text)) }
                },
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                shape           = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = if (isEditing) ImeAction.Done else ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = MaterialTheme.appColors.cyanAccent,
                    unfocusedBorderColor    = MaterialTheme.appColors.navyBorder,
                    cursorColor             = MaterialTheme.appColors.cyanAccent,
                    focusedLabelColor       = MaterialTheme.appColors.cyanAccent,
                    unfocusedLabelColor     = MaterialTheme.appColors.textSecondary,
                    focusedTextColor        = MaterialTheme.appColors.textPrimary,
                    unfocusedTextColor      = MaterialTheme.appColors.textPrimary,
                    focusedContainerColor   = MaterialTheme.appColors.navySurfaceLight,
                    unfocusedContainerColor = MaterialTheme.appColors.navySurfaceLight
                )
            )

            if (!isEditing) {
                Spacer(Modifier.height(12.dp))

                // ── Saldo inicial ────────────────────────────────────────────
                OutlinedTextField(
                    value         = balanceText,
                    onValueChange = {
                        balanceText  = it.filter { c -> c.isDigit() || c == ',' || c == '.' }
                        balanceError = false
                    },
                    label         = { Text(stringResource(Res.string.account_initial_balance_label)) },
                    placeholder   = { Text("0,00") },
                    trailingIcon  = {
                        Text(
                            text     = "€",
                            fontSize = 16.sp,
                            color    = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    },
                    isError        = balanceError,
                    supportingText = if (balanceError) {
                        { Text(stringResource(Res.string.account_initial_balance_valid_hint)) }
                    } else {
                        { Text(stringResource(Res.string.account_initial_balance_desc)) }
                    },
                    modifier        = Modifier.fillMaxWidth(),
                    singleLine      = true,
                    shape           = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction    = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor      = MaterialTheme.appColors.cyanAccent,
                        unfocusedBorderColor    = MaterialTheme.appColors.navyBorder,
                        cursorColor             = MaterialTheme.appColors.cyanAccent,
                        focusedLabelColor       = MaterialTheme.appColors.cyanAccent,
                        unfocusedLabelColor     = MaterialTheme.appColors.textSecondary,
                        focusedTextColor        = MaterialTheme.appColors.textPrimary,
                        unfocusedTextColor      = MaterialTheme.appColors.textPrimary,
                        focusedContainerColor   = MaterialTheme.appColors.navySurfaceLight,
                        unfocusedContainerColor = MaterialTheme.appColors.navySurfaceLight
                    )
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    var valid = true
                    if (name.isBlank()) { nameError = true; valid = false }
                    val balance: Double
                    if (!isEditing) {
                        val parsed = balanceText.replace(',', '.').toDoubleOrNull()
                        if (balanceText.isNotBlank() && parsed == null) {
                            balanceError = true; valid = false
                        }
                        balance = if (balanceText.isBlank()) 0.0 else (parsed ?: run { valid = false; 0.0 })
                    } else {
                        balance = 0.0
                    }
                    if (!valid) return@Button
                    onSave(name.trim(), balance)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(11.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.cyanAccent,
                    contentColor   = MaterialTheme.appColors.navyDeep
                )
            ) {
                Text(
                    text       = if (isEditing) stringResource(Res.string.portfolio_add_asset_save)
                                 else "Crear cuenta",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.common_cancel), fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary)
            }
        }
    }
}

@Preview
@Composable
private fun AddEditAccountBottomSheetCreatePreview() {
    N3toTheme {
        AddEditAccountBottomSheet(
            account   = null,
            onSave    = { _, _ -> },
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
                id              = "1",
                name            = "Cuenta Principal",
                initialBalance  = 5000.0,
                computedBalance = 5200.0,
                createdAt       = nowMillis()
            ),
            onSave    = { _, _ -> },
            onDismiss = {}
        )
    }
}
