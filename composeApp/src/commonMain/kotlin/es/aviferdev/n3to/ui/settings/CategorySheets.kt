package es.aviferdev.n3to.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.LimitType
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.common.input.AmountInputField
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.DragHandleColor
import androidx.compose.ui.tooling.preview.Preview

// ─── CONSTANTES ──────────────────────────────────────────────────────────────────
private val chipBorderColor = BorderGray2
private val chipTextColor   = TextTertiary
private val chipSelectedBg  = PrimaryAlpha

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
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DragHandleColor)
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
                    "Nueva categoría",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre de la categoría", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryDark
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
                    containerColor = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.3f)
                )
            ) {
                Text("Guardar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary, fontSize = 14.sp)
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
    var name by remember { mutableStateOf(currentName) }
    var limitText by remember {
        mutableStateOf(
            if (currentLimit > 0.0) currentLimit.toBigDecimal().stripTrailingZeros().toPlainString()
            else ""
        )
    }
    var limitType by remember { mutableStateOf(currentLimitType) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DragHandleColor)
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
                    "Editar categoría",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Nombre ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre de la categoría", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryDark
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
                    "Límite anual",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Selector de tipo (Fijo / Porcentaje) ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SelectableChip(
                    label = LimitType.FIXED.label,
                    selected = limitType == LimitType.FIXED,
                    onClick = { limitType = LimitType.FIXED },
                    accentColor = PrimaryDark,
                    selectedBgColor = chipSelectedBg,
                    borderColorUnselected = chipBorderColor,
                    textColorUnselected = chipTextColor,
                    modifier = Modifier.weight(1f)
                )
                SelectableChip(
                    label = LimitType.PERCENTAGE.label,
                    selected = limitType == LimitType.PERCENTAGE,
                    onClick = { limitType = LimitType.PERCENTAGE },
                    accentColor = PrimaryDark,
                    selectedBgColor = chipSelectedBg,
                    borderColorUnselected = chipBorderColor,
                    textColorUnselected = chipTextColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── AmountInputField (componente de importe nativo) ─────────────────
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
                    containerColor = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.3f)
                )
            ) {
                Text("Guardar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            // ── Botón Quitar límite (solo si hay límite actual) ────────────────
            if (currentLimit > 0.0) {
                TextButton(onClick = {
                    onSave(name.trim(), 0.0, LimitType.FIXED)
                }) {
                    Text("Quitar límite", color = ExpenseRed, fontSize = 14.sp)
                }
            }

            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary, fontSize = 14.sp)
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
