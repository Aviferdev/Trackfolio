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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.formatAmount
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconcileBalanceBottomSheet(
    viewModel: ReconciliationViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.closeBottomSheet()
            onDismiss()
        },
        sheetState     = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceElevated,
        shape          = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        ReconcileBalanceBottomSheetContent(
            state               = state,
            keyboardController  = keyboardController,
            onRealBalanceChange = { viewModel.updateRealBalance(it) },
            onReconcile         = { viewModel.reconcile() }
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
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector        = Icons.Outlined.Balance,
            contentDescription = null,
            tint               = PrimaryDark,
            modifier           = Modifier.height(32.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text       = "Reconciliar saldo",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = "Saldo actual calculado",
            fontSize  = 13.sp,
            color     = TextSecondary
        )

        Text(
            text       = "${formatAmount(state.computedBalance)} €",
            fontSize   = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value           = state.realBalanceInput,
            onValueChange   = onRealBalanceChange,
            label           = { Text("Saldo real (€)") },
            placeholder     = { Text("Ej: 1250.00") },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction    = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onReconcile()
                }
            ),
            colors   = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrimaryDark,
                focusedLabelColor    = PrimaryDark,
                cursorColor          = PrimaryDark
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        val realBalance = state.realBalanceInput.replace(',', '.').toDoubleOrNull()
        if (realBalance != null) {
            val diff = realBalance - state.computedBalance
            val sign = if (diff > 0) "+" else ""
            val color = when {
                diff > 0  -> MaterialTheme.colorScheme.primary
                diff < 0  -> MaterialTheme.colorScheme.error
                else      -> TextSecondary
            }
            Text(
                text       = "Diferencia: ${sign}${formatAmount(diff)} €",
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = color,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        state.resultMessage?.let { msg ->
            val color = if (state.isSuccess) PrimaryDark else MaterialTheme.colorScheme.error
            Text(
                text      = msg,
                fontSize  = 13.sp,
                color     = color,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick  = onReconcile,
            enabled  = !state.isProcessing && state.realBalanceInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
        ) {
            if (state.isProcessing) {
                CircularProgressIndicator(
                    color       = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier    = Modifier.height(20.dp)
                )
            } else {
                Text(
                    text       = if (state.isSuccess) "Hecho" else "Ajustar saldo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 15.sp
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
            state               = ReconciliationUiState(computedBalance = 1234.56, realBalanceInput = "1250,00"),
            keyboardController  = null,
            onRealBalanceChange = {},
            onReconcile         = {}
        )
    }
}
