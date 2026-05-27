package es.aviferdev.n3to.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.common.input.AmountInputField
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.appColors
import es.aviferdev.n3to.ui.theme.LocalCurrencySymbol
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.category_annual_limit
import n3to.composeapp.generated.resources.category_create_new
import n3to.composeapp.generated.resources.category_edit_title
import n3to.composeapp.generated.resources.category_income_percentage
import n3to.composeapp.generated.resources.category_limit_amount
import n3to.composeapp.generated.resources.category_name_placeholder
import n3to.composeapp.generated.resources.category_remove_limit
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_close
import n3to.composeapp.generated.resources.common_save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// ─── CONSTANTES ──────────────────────────────────────────────────────────────────
private val chipBorderColor: androidx.compose.ui.graphics.Color
    @Composable get() = MaterialTheme.appColors.border2
private val chipTextColor: androidx.compose.ui.graphics.Color
    @Composable get() = MaterialTheme.appColors.textTertiary
private val chipSelectedBg = PrimaryAlpha

// ─── AÑADIR CATEGORÍA ────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategorySheet(
    type: TransactionType,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.dragHandle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.category_create_new),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        stringResource(Res.string.common_close),
                        tint = MaterialTheme.appColors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(Res.string.category_name_placeholder),
                        color = MaterialTheme.appColors.textTertiary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border,
                    focusedTextColor = MaterialTheme.appColors.textPrimary,
                    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
                    cursorColor = MaterialTheme.appColors.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim())
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    stringResource(Res.string.common_save),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = MaterialTheme.appColors.textSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── EDITAR CATEGORÍA (con límite anual) ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategorySheet(
    currentName: String,
    currentLimit: Double = 0.0,
    currentLimitType: LimitType = LimitType.FIXED,
    type: TransactionType,
    onSave: (name: String, annualLimit: Double, limitType: LimitType) -> Unit,
    onDismiss: () -> Unit
) {
    val currency = LocalCurrencySymbol.current
    var name by remember { mutableStateOf(currentName) }
    var limitText by remember {
        mutableStateOf(
            if (currentLimit > 0.0) if (currentLimit % 1.0 == 0.0) currentLimit.toLong()
                .toString() else currentLimit.toString()
            else ""
        )
    }
    var limitType by remember { mutableStateOf(currentLimitType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.dragHandle)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Título ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(Res.string.category_edit_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        stringResource(Res.string.common_close),
                        tint = MaterialTheme.appColors.textSecondary
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Nombre ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(Res.string.category_name_placeholder),
                        color = MaterialTheme.appColors.textTertiary
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border,
                    focusedTextColor = MaterialTheme.appColors.textPrimary,
                    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
                    cursorColor = MaterialTheme.appColors.primary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Límite anual (siempre visible) ──────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.category_annual_limit),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.appColors.textPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Selector de tipo (Fijo / Porcentaje) ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableChip(
                    label = "${LimitType.FIXED.label} ($currency)",
                    selected = limitType == LimitType.FIXED,
                    onClick = { limitType = LimitType.FIXED },
                    accentColor = MaterialTheme.appColors.primary,
                    selectedBgColor = chipSelectedBg,
                    borderColorUnselected = chipBorderColor,
                    textColorUnselected = chipTextColor,
                    modifier = Modifier.weight(1f)
                )
                SelectableChip(
                    label = LimitType.PERCENTAGE.label,
                    selected = limitType == LimitType.PERCENTAGE,
                    onClick = { limitType = LimitType.PERCENTAGE },
                    accentColor = MaterialTheme.appColors.primary,
                    selectedBgColor = chipSelectedBg,
                    borderColorUnselected = chipBorderColor,
                    textColorUnselected = chipTextColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── AmountInputField (componente de importe nativo) ─────────────────
            AmountInputField(
                label = if (limitType == LimitType.FIXED) stringResource(Res.string.category_limit_amount) else stringResource(Res.string.category_income_percentage),
                value = limitText,
                onChange = { newValue ->
                    val filtered = newValue.filter { it.isDigit() || it == ',' || it == '.' }
                    limitText = filtered
                },
                placeholder = if (limitType == LimitType.FIXED) "0,00" else "0"
            )

            Spacer(Modifier.height(24.dp))

            // ── Botón Guardar ──────────────────────────────────────────────────
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val limit = limitText
                            .replace(",", ".")
                            .toDoubleOrNull()
                            ?.coerceAtLeast(0.0) ?: 0.0
                        onSave(name.trim(), limit, limitType)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    stringResource(Res.string.common_save),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Botón Quitar límite (solo si hay límite actual) ────────────────
            if (currentLimit > 0.0) {
                TextButton(onClick = {
                    onSave(name.trim(), 0.0, LimitType.FIXED)
                }) {
                    Text(
                        stringResource(Res.string.category_remove_limit),
                        color = MaterialTheme.appColors.expense,
                        fontSize = 14.sp
                    )
                }
            }

            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = MaterialTheme.appColors.textSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ─── PREVIEWS ────────────────────────────────────────────────────────────────────
@Preview
@Composable
private fun AddCategorySheetPreview() {
    N3toTheme {
        AddCategorySheet(type = TransactionType.EXPENSE, onSave = {}, onDismiss = {})
    }
}

@Preview
@Composable
private fun EditCategorySheetPreview() {
    N3toTheme {
        EditCategorySheet(
            currentName = "Alimentación",
            currentLimit = 6000.0,
            currentLimitType = LimitType.FIXED,
            type = TransactionType.EXPENSE,
            onSave = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
