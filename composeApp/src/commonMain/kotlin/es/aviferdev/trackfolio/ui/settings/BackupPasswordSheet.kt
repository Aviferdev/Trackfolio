package es.aviferdev.trackfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
    val isLoading   = state.backupState is BackupUiState.Loading
    val isSuccess   = state.backupState is BackupUiState.Success
    val errorState  = state.backupState as? BackupUiState.Error

    // ── Estado LOCAL de los TextFields ────────────────────────────────────────
    // Mantenemos el texto en local state para evitar que cada pulsación dispare
    // una recomposición del sheet completo a través del StateFlow del ViewModel.
    // Solo notificamos al VM cuando el valor cambia respecto al que ya tenía.
    var pwd        by remember(state.action) { mutableStateOf(state.password) }
    var confirmPwd by remember(state.action) { mutableStateOf(state.confirmPassword) }
    var showPwd     by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    // Si el VM resetea el state (p.ej. tras éxito o dismiss), reflejarlo.
    LaunchedEffect(state.password)        { if (state.password != pwd) pwd = state.password }
    LaunchedEffect(state.confirmPassword) { if (state.confirmPassword != confirmPwd) confirmPwd = state.confirmPassword }

    ModalBottomSheet(
        onDismissRequest = { if (!isLoading) onDismiss() },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
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

            // ── Campos de contraseña (ocultos tras éxito en export) ───────────
            if (!isSuccess) {
                OutlinedTextField(
                    value         = pwd,
                    onValueChange = { newValue ->
                        pwd = newValue
                        onPasswordChange(newValue)
                    },
                    label         = { Text(if (isExport) "Contraseña de cifrado" else "Contraseña del backup") },
                    visualTransformation = if (showPwd) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(
                            onClick        = { showPwd = !showPwd },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                if (showPwd) "Ocultar" else "Mostrar",
                                fontSize = 11.sp,
                                color    = TextSecondary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError    = state.passwordError != null,
                    enabled    = !isLoading,
                    modifier   = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape      = RoundedCornerShape(10.dp),
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PrimaryDark,
                        unfocusedBorderColor = BorderGray
                    )
                )

                if (isExport) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value         = confirmPwd,
                        onValueChange = { newValue ->
                            confirmPwd = newValue
                            onConfirmPasswordChange(newValue)
                        },
                        label         = { Text("Confirmar contraseña") },
                        visualTransformation = if (showConfirm) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        trailingIcon = {
                            TextButton(
                                onClick        = { showConfirm = !showConfirm },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(
                                    if (showConfirm) "Ocultar" else "Mostrar",
                                    fontSize = 11.sp,
                                    color    = TextSecondary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError    = state.passwordError != null,
                        enabled    = !isLoading,
                        modifier   = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape      = RoundedCornerShape(10.dp),
                        colors     = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = PrimaryDark,
                            unfocusedBorderColor = BorderGray
                        )
                    )
                }

                // Error de validación de contraseña
                state.passwordError?.let { err ->
                    Spacer(Modifier.height(6.dp))
                    Text(err, fontSize = 12.sp, color = ExpenseRed)
                }

                // Error devuelto por la operación de backup (no validación)
                errorState?.let { err ->
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ExpenseRed.copy(alpha = 0.18f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text     = "⚠️  ${err.message}",
                            fontSize = 13.sp,
                            color    = ExpenseRed
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Acción principal ──────────────────────────────────────────────
            when {
                isSuccess && isExport -> {
                    // Export completado: mostrar confirmación y cerrar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(IncomeGreen.copy(alpha = 0.18f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "✅  Backup exportado correctamente",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color      = IncomeGreen
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick  = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
                    ) {
                        Text("Cerrar", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                else -> {
                    val canSubmit = !isLoading && pwd.isNotBlank() &&
                            (!isExport || confirmPwd.isNotBlank())

                    Button(
                        onClick  = onConfirm,
                        enabled  = canSubmit,
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
                                color       = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text       = if (isExport) "Exportar y compartir" else "Restaurar backup",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color      = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onDismiss, enabled = !isLoading) {
                        Text("Cancelar", fontSize = 14.sp, color = TextSecondary)
                    }
                }
            }
        }
    }

    // Auto-cerrar tras éxito solo en importación
    if (isSuccess && !isExport) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(600)
            onDismiss()
        }
    }
}
