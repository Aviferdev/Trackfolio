package es.aviferdev.n3to.ui.realestate

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_note_placeholder
import n3to.composeapp.generated.resources.realestate_expense_delete_cd
import org.jetbrains.compose.resources.stringResource

/**
 * Fila reusable para añadir/editar un gasto de propiedad.
 * Incluye selector de categoría, campo de importe y botón de eliminar.
 */
@Composable
fun PropertyExpenseRow(
    categories: List<Category>,
    expense: PropertyExpense,
    onExpenseChange: (PropertyExpense) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var amountText by remember(expense.amount) {
        mutableStateOf(
            if (expense.amount > 0 && expense.amount == expense.amount.toLong().toDouble())
                expense.amount.toLong().toString()
            else if (expense.amount > 0) expense.amount.toString()
            else ""
        )
    }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var notesText by remember(expense.notes) { mutableStateOf(expense.notes ?: "") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.appColors.background),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: selector de categoría + botón eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown de categorías
                Box {
                    val selectedCategory = categories.find { it.id == expense.categoryId }
                    OutlinedButton(
                        onClick = { showCategoryMenu = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selectedCategory != null) MaterialTheme.appColors.primary else MaterialTheme.appColors.textTertiary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = selectedCategory?.name ?: "Categoría",
                            fontSize = 12.sp,
                            fontWeight = if (selectedCategory != null) FontWeight.Medium else FontWeight.Normal
                        )
                    }

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false },
                        containerColor = MaterialTheme.appColors.surface
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = category.name,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.appColors.textPrimary,
                                        fontWeight = if (category.id == expense.categoryId) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onExpenseChange(expense.copy(categoryId = category.id))
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                // Botón eliminar
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(Res.string.realestate_expense_delete_cd),
                        tint = MaterialTheme.appColors.expense,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Importe + Notas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        amountText = newValue.filter { c -> c.isDigit() || c == ',' || c == '.' }
                        val amount = newValue.replace(',', '.').toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            onExpenseChange(expense.copy(amount = amount))
                        }
                    },
                    placeholder = { Text("0,00", fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    trailingIcon = { Text("€", fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary, modifier = Modifier.padding(end = 8.dp)) },
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = MaterialTheme.appColors.textPrimary),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    )
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = {
                        notesText = it
                        onExpenseChange(expense.copy(notes = it.ifBlank { null }))
                    },
                    placeholder = { Text(stringResource(Res.string.common_note_placeholder), fontSize = 12.sp, color = MaterialTheme.appColors.textTertiary) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = MaterialTheme.appColors.textPrimary),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.appColors.primary,
                        unfocusedBorderColor = MaterialTheme.appColors.border
                    )
                )
            }
        }
    }
}
