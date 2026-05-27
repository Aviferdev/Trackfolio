package es.aviferdev.n3to.ui.home.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.account_set_balance_btn
import n3to.composeapp.generated.resources.account_set_balance_desc
import n3to.composeapp.generated.resources.account_set_balance_hint
import n3to.composeapp.generated.resources.account_set_balance_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Sheet obligatorio para configurar el saldo inicial de una cuenta.
 * No tiene botón de cancelar/omitir: la cuenta no es usable sin saldo inicial.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetInitialBalanceBottomSheet(
    accountName: String = "",
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    val currency = LocalCurrencySymbol.current
    val isValid = amount.isNotBlank() &&
            amount.replace(',', '.').toDoubleOrNull()?.let { it >= 0 } == true

    ModalBottomSheet(
        onDismissRequest = { /* Bloqueado — solo se puede cerrar confirmando */ },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.dragHandle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.account_set_balance_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )

            if (accountName.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = accountName,
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.account_set_balance_desc),
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
                placeholder = {
                    Text(
                        "0,00",
                        color = MaterialTheme.appColors.textSecondary.copy(alpha = 0.6f),
                        fontSize = 32.sp
                    )
                },
                textStyle = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary,
                    textAlign = TextAlign.Center
                ),
                trailingIcon = {
                    Text(
                        currency,
                        fontSize = 20.sp,
                        color = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.account_set_balance_hint),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val value = amount.replace(',', '.').toDoubleOrNull() ?: return@Button
                    onConfirm(value)
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.38f)
                )
            ) {
                Text(
                    stringResource(Res.string.account_set_balance_btn),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
private fun SetInitialBalanceBottomSheetPreview() {
    N3toTheme {
        SetInitialBalanceBottomSheet(
            accountName = "Cuenta Principal",
            onConfirm = {}
        )
    }
}
