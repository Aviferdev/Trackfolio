package es.aviferdev.n3to.ui.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Debt
import es.aviferdev.n3to.domain.model.DebtDirection
import es.aviferdev.n3to.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtBottomSheet(
    editingDebt: Debt? = null,
    onDismiss: () -> Unit,
    viewModel: DebtViewModel
) {
    val isEditing = editingDebt != null

    var personName by remember { mutableStateOf(editingDebt?.personName ?: "") }
    var amount     by remember { mutableStateOf(
        editingDebt?.amount?.let {
            if (it == it.toLong().toDouble()) it.toLong().toString()
            else it.toString()
        } ?: ""
    )}
    var direction  by remember { mutableStateOf(editingDebt?.direction ?: DebtDirection.THEY_OWE) }
    var notes      by remember { mutableStateOf(editingDebt?.notes ?: "") }
    var isLoading  by remember { mutableStateOf(false) }

    val isValid = personName.isNotBlank() &&
        amount.isNotBlank() &&
        amount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
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
        AddDebtBottomSheetContent(
            isEditing         = isEditing,
            personName        = personName,
            onPersonNameChange = { personName = it },
            amount            = amount,
            onAmountChange    = { amount = it.filter { c -> c.isDigit() || c == ',' || c == '.' } },
            direction         = direction,
            onDirectionChange = { direction = it },
            notes             = notes,
            onNotesChange     = { notes = it },
            isValid           = isValid,
            isLoading         = isLoading,
            onSave            = {
                val amountValue = amount.replace(',', '.').toDoubleOrNull()
                if (amountValue != null) {
                    isLoading = true
                    if (isEditing && editingDebt != null) {
                        viewModel.editDebt(
                            original   = editingDebt,
                            personName = personName.trim(),
                            amount     = amountValue,
                            direction  = direction,
                            notes      = notes.ifBlank { null }
                        )
                    } else {
                        viewModel.saveDebt(
                            personName = personName.trim(),
                            amount     = amountValue,
                            direction  = direction,
                            notes      = notes.ifBlank { null }
                        )
                    }
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun AddDebtBottomSheetContent(
    isEditing: Boolean,
    personName: String,
    onPersonNameChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    direction: DebtDirection,
    onDirectionChange: (DebtDirection) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    isValid: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text       = if (isEditing) "Editar deuda" else "Nueva deuda",
            fontSize   = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextPrimary
        )

        Spacer(Modifier.height(20.dp))

        Text(text = "Tipo de deuda", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DirectionChip(
                label         = "Me deben",
                selected      = direction == DebtDirection.THEY_OWE,
                selectedColor = IncomeGreen,
                onClick       = { onDirectionChange(DebtDirection.THEY_OWE) }
            )
            DirectionChip(
                label         = "Debo yo",
                selected      = direction == DebtDirection.I_OWE,
                selectedColor = ExpenseRed,
                onClick       = { onDirectionChange(DebtDirection.I_OWE) }
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(text = "Persona", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = personName,
            onValueChange = onPersonNameChange,
            placeholder   = { Text("Nombre o apodo", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrimaryDark,
                unfocusedBorderColor = BorderGray
            ),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Text(text = "Importe", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = amount,
            onValueChange = onAmountChange,
            placeholder   = { Text("0,00", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            trailingIcon  = {
                Text("€", fontSize = 16.sp, color = TextSecondary, modifier = Modifier.padding(end = 12.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrimaryDark,
                unfocusedBorderColor = BorderGray
            ),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        Text(text = "Nota (opcional)", fontSize = 13.sp, color = TextSecondary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = notes,
            onValueChange = onNotesChange,
            placeholder   = { Text("Ej. Cena del viernes", color = TextSecondary.copy(alpha = 0.6f), fontSize = 14.sp) },
            modifier      = Modifier.fillMaxWidth(),
            shape         = RoundedCornerShape(8.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = PrimaryDark,
                unfocusedBorderColor = BorderGray
            ),
            singleLine = true
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick  = onSave,
            enabled  = isValid && !isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = PrimaryDark,
                disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
            )
        ) {
            Text(
                if (isEditing) "Guardar cambios" else "Guardar deuda",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DirectionChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) selectedColor else Color.Transparent)
            .border(1.dp, if (selected) selectedColor else BorderGray, RoundedCornerShape(50.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color      = if (selected) Color.White else TextSecondary
        )
    }
}

@Preview
@Composable
private fun AddDebtBottomSheetContentPreview() {
    N3toTheme {
        AddDebtBottomSheetContent(
            isEditing         = false,
            personName        = "Juan Pérez",
            onPersonNameChange = {},
            amount            = "50,00",
            onAmountChange    = {},
            direction         = DebtDirection.THEY_OWE,
            onDirectionChange = {},
            notes             = "Cena del viernes",
            onNotesChange     = {},
            isValid           = true,
            isLoading         = false,
            onSave            = {}
        )
    }
}
