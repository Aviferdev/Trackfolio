package es.aviferdev.n3to.ui.settings

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.common.input.AmountInputField
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.category_remove_limit
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_close
import n3to.composeapp.generated.resources.common_save

import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Bottom sheet ligero para configurar o quitar el límite anual de una categoría.
 * No modifica el nombre de la categoría — solo el límite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetCategoryLimitSheet(
    categoryName: String,
    currentLimit: Double = 0.0,
    currentLimitType: LimitType = LimitType.FIXED,
    onSave: (annualLimit: Double, limitType: LimitType) -> Unit,
    onDismiss: () -> Unit
) {
    var limitText by remember {
        mutableStateOf(
            if (currentLimit > 0.0) if (currentLimit % 1.0 == 0.0) currentLimit.toLong().toString() else currentLimit.toString()
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
                Column {
                    Text(
                        "Límite anual",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.appColors.textPrimary
                    )
                    Text(
                        categoryName,
                        fontSize = 13.sp,
                        color = MaterialTheme.appColors.textSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, stringResource(Res.string.common_close), tint = MaterialTheme.appColors.textSecondary)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Selector de tipo (Fijo / Porcentaje) ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableChip(
                    label = LimitType.FIXED.label,
                    selected = limitType == LimitType.FIXED,
                    onClick = { limitType = LimitType.FIXED },
                    accentColor = MaterialTheme.appColors.primary,
                    selectedBgColor = PrimaryAlpha,
                    borderColorUnselected = MaterialTheme.appColors.border2,
                    textColorUnselected = MaterialTheme.appColors.textTertiary,
                    modifier = Modifier.weight(1f)
                )
                SelectableChip(
                    label = LimitType.PERCENTAGE.label,
                    selected = limitType == LimitType.PERCENTAGE,
                    onClick = { limitType = LimitType.PERCENTAGE },
                    accentColor = MaterialTheme.appColors.primary,
                    selectedBgColor = PrimaryAlpha,
                    borderColorUnselected = MaterialTheme.appColors.border2,
                    textColorUnselected = MaterialTheme.appColors.textTertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Campo de importe (AmountInputField) ────────────────────────────
            AmountInputField(
                label = if (limitType == LimitType.FIXED) "Importe del límite" else "Porcentaje de ingresos",
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
                    val limit = limitText
                        .replace(",", ".")
                        .toDoubleOrNull()
                        ?.coerceAtLeast(0.0) ?: 0.0
                    onSave(limit, limitType)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.primary,
                    disabledContainerColor = MaterialTheme.appColors.primary.copy(alpha = 0.3f)
                )
            ) {
                Text(stringResource(Res.string.common_save), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            // ── Botón Quitar límite (solo si hay límite actual) ────────────────
            if (currentLimit > 0.0) {
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { onSave(0.0, LimitType.FIXED) }) {
                    Text(stringResource(Res.string.category_remove_limit), color = MaterialTheme.appColors.expense, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(4.dp))

            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.common_cancel), color = MaterialTheme.appColors.textSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Preview
@Composable
private fun SetCategoryLimitSheetPreview() {
    N3toTheme {
        SetCategoryLimitSheet(
            categoryName = "Alimentación",
            currentLimit = 6000.0,
            currentLimitType = LimitType.FIXED,
            onSave = { _, _ -> },
            onDismiss = {}
        )
    }
}
