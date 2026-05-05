package es.aviferdev.trackfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupPasswordSheet(
    state: BackupSheetState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isExport    = state.action == BackupAction.EXPORT
    var showPwd     by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

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
                    .background(Color(0xFFBDBDBD))
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
                text       = if (isExport) "☁️  Exportar backup" else "📥  Importar backup",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isExport)
                    "Elige una contraseña para cifrar tu backup.\nNecesitarás esta misma contraseña para restaurarlo."
                else
                    "Introduce la contraseña con la que se cifró el backup que quieres restaurar.",
                fontSize  = 13.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            // ── Contraseña ────────────────────────────────────────────────────
            OutlinedTextField(
                value         = state.password,
                onValueChange = onPasswordChange,
                label         = { Text(if (isExport) "Contraseña de cifrado" else "Contraseña del backup") },
                visualTransformation = if (showPwd) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon  = {
                    TextButton(
                        onClick            = { showPwd = !showPwd },
                        contentPadding     = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text     = if (showPwd) "Ocultar" else "Mostrar",
                            fontSize = 11.sp,
                            color    = TextSecondary
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError    = state.passwordError != null,
                modifier   = Modifier.fillMaxWidth(),
                singleLine = true,
                shape      = RoundedCornerShape(10.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            // ── Confirmar contraseña (solo exportar) ──────────────────────────
            if (isExport) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label         = { Text("Confirmar contraseña") },
                    visualTransformation = if (showConfirm) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon  = {
                        TextButton(
                            onClick        = { showConfirm = !showConfirm },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                text     = if (showConfirm) "Ocultar" else "Mostrar",
                                fontSize = 11.sp,
                                color    = TextSecondary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError    = state.passwordError != null,
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(10.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    )
                )
            }

            // ── Error ─────────────────────────────────────────────────────────
            state.passwordError?.let { err ->
                Spacer(Modifier.height(6.dp))
                Text(err, fontSize = 12.sp, color = ExpenseRed)
            }

            Spacer(Modifier.height(28.dp))

            // ── Botón ─────────────────────────────────────────────────────────
            val isLoading = state.backupState is BackupUiState.Loading

            Button(
                onClick  = onConfirm,
                enabled  = !isLoading && state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text       = if (isExport) "Exportar y compartir" else "Restaurar backup",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color      = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancelar", fontSize = 14.sp, color = TextSecondary)
            }
        }
    }

    // Cerrar automáticamente tras éxito
    if (state.backupState is BackupUiState.Success) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(300)
            onDismiss()
        }
    }
}
