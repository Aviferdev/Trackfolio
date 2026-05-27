package es.aviferdev.n3to.ui.reconciliation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryDark

import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.account_computed_balance
import n3to.composeapp.generated.resources.reconciliation_adjust_label
import n3to.composeapp.generated.resources.reconciliation_amount_placeholder
import n3to.composeapp.generated.resources.reconciliation_done
import n3to.composeapp.generated.resources.reconciliation_error_balanced
import n3to.composeapp.generated.resources.reconciliation_error_generic
import n3to.composeapp.generated.resources.reconciliation_error_invalid_amount
import n3to.composeapp.generated.resources.reconciliation_real_balance
import n3to.composeapp.generated.resources.reconciliation_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconcileBalanceBottomSheet(
    state: ReconciliationUiState,
    onRealBalanceChange: (String) -> Unit,
    onReconcile: () -> Unit,
    onDismiss: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surfaceElevated,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        ReconcileBalanceBottomSheetContent(
            state = state,
            keyboardController = keyboardController,
            onRealBalanceChange = onRealBalanceChange,
            onReconcile = onReconcile
        )
    }
}

@Composable
fun ReconcileBalanceBottomSheetContent(
    state: ReconciliationUiState,
    keyboardController: SoftwareKeyboardController?,
    onRealBalanceChange: (String) -> Unit,
    onReconcile: () -> Unit
) {
    val currency = LocalCurrencySymbol.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Balance,
            contentDescription = null,
            tint = MaterialTheme.appColors.primary,
            modifier = Modifier.height(32.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(Res.string.reconciliation_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.appColors.textPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.account_computed_balance),
            fontSize = 13.sp,
            color = MaterialTheme.appColors.textSecondary
        )

        Text(
            text = "${formatAmount(state.computedBalance)} $currency",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textPrimary
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = state.realBalanceInput,
            onValueChange = onRealBalanceChange,
            label = { Text(stringResource(Res.string.reconciliation_real_balance, currency)) },
            placeholder = { Text(stringResource(Res.string.reconciliation_amount_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onReconcile()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.appColors.primary,
                focusedLabelColor = MaterialTheme.appColors.primary,
                cursorColor = MaterialTheme.appColors.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        val realBalance = state.realBalanceInput.replace(',', '.').toDoubleOrNull()
        if (realBalance != null) {
            val diff = realBalance - state.computedBalance
            val sign = if (diff > 0) "+" else ""
            val color = when {
                diff > 0 -> MaterialTheme.appColors.primary
                diff < 0 -> MaterialTheme.appColors.expense
                else -> MaterialTheme.appColors.textSecondary
            }
            Text(
                text = "Diferencia: ${sign}${formatAmount(diff)} $currency",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        state.resultMessage?.let { msg ->
            val color =
                if (state.isSuccess) MaterialTheme.appColors.primary else MaterialTheme.appColors.expense
            Text(
                text = when (msg) {
                    is ReconciliationError.InvalidAmount -> stringResource(Res.string.reconciliation_error_invalid_amount)
                    is ReconciliationError.AlreadyBalanced -> stringResource(Res.string.reconciliation_error_balanced)
                    is ReconciliationError.Generic -> msg.message ?: stringResource(Res.string.reconciliation_error_generic)
                    is ReconciliationError.Success -> msg.formattedMessage
                },
                fontSize = 13.sp,
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = onReconcile,
            enabled = !state.isProcessing && state.realBalanceInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(
                    color = MaterialTheme.appColors.navyDeep,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(20.dp)
                )
            } else {
                Text(
                    text = if (state.isSuccess) stringResource(Res.string.reconciliation_done) else stringResource(
                        Res.string.reconciliation_adjust_label
                    ),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Preview
@Composable
private fun ReconcileBalanceBottomSheetContentPreview() {
    es.aviferdev.n3to.ui.theme.N3toTheme {
        ReconcileBalanceBottomSheetContent(
            state = ReconciliationUiState(computedBalance = 1234.56, realBalanceInput = "1250,00"),
            keyboardController = null,
            onRealBalanceChange = {},
            onReconcile = {}
        )
    }
}
