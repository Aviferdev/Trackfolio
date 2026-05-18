package es.aviferdev.n3to.ui.valuable

import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.model.ValuableExpense
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import es.aviferdev.n3to.domain.model.ValuableExpenseCategories
import es.aviferdev.n3to.ui.common.input.DatePickerRow
import es.aviferdev.n3to.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditValuableBottomSheet(
    existingValuable: Valuable? = null,
    existingPurchaseExpenses: List<ValuableExpense> = emptyList(),
    existingHoldingExpenses: List<ValuableExpense> = emptyList(),
    accountId: String,
    onDismiss: () -> Unit,
    onSave: (Valuable, List<ValuableExpense>, List<ValuableExpense>) -> Unit
) {
    val isEditing = existingValuable != null

    var name by remember { mutableStateOf(existingValuable?.name ?: "") }
    var description by remember { mutableStateOf(existingValuable?.description ?: "") }
    var purchasePriceText by remember { mutableStateOf(existingValuable?.purchasePrice?.toString() ?: "") }
    var purchaseDateMillis by remember { mutableStateOf(existingValuable?.purchaseDate ?: nowMillis()) }
    var estimatedValueText by remember { mutableStateOf(existingValuable?.estimatedValue?.toString() ?: "") }
    var purchaseExpenses by remember { mutableStateOf(existingPurchaseExpenses) }
    var holdingExpenses by remember { mutableStateOf(existingHoldingExpenses) }
    var notes by remember { mutableStateOf(existingValuable?.notes ?: "") }

    val expenseCategories = remember {
        ValuableExpenseCategories.allIds.toList()
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = BackgroundGray) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (isEditing) "Editar bien" else "Nuevo bien",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
            Spacer(Modifier.height(20.dp))

            // Nombre
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre") },
                placeholder = { Text("Ej: Torno CNC") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Descripción
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                placeholder = { Text("Marca, modelo, año...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Precio compra
            OutlinedTextField(
                value = purchasePriceText, onValueChange = { purchasePriceText = it },
                label = { Text("Precio de compra") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Fecha compra
            DatePickerRow(
                label = "Fecha de compra",
                dateMillis = purchaseDateMillis,
                onDateSelected = { purchaseDateMillis = it }
            )
            Spacer(Modifier.height(12.dp))

            // Valor estimado
            OutlinedTextField(
                value = estimatedValueText, onValueChange = { estimatedValueText = it },
                label = { Text("Valor estimado (opcional)") },
                placeholder = { Text("Si no se indica, se usa el precio de compra") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(12.dp))

            // Notas
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notas (opcional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )
            Spacer(Modifier.height(16.dp))

            // ── Gastos de compra ──────────────────────────────────────────
            Text("Gastos de compra", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            purchaseExpenses.forEachIndexed { index, expense ->
                ExpenseRow(
                    expense = expense,
                    categories = expenseCategories,
                    onExpenseChange = { newExpense ->
                        purchaseExpenses = purchaseExpenses.toMutableList().apply {
                            set(index, newExpense)
                        }
                    },
                    onRemove = {
                        purchaseExpenses = purchaseExpenses.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
            TextButton(onClick = {
                purchaseExpenses = purchaseExpenses + ValuableExpense(
                    categoryId = expenseCategories.first(),
                    amount = 0.0
                )
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir gasto de compra")
            }
            Spacer(Modifier.height(12.dp))

            // ── Gastos de tenencia ────────────────────────────────────────
            Text("Gastos de tenencia", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            holdingExpenses.forEachIndexed { index, expense ->
                ExpenseRow(
                    expense = expense,
                    categories = expenseCategories,
                    onExpenseChange = { newExpense ->
                        holdingExpenses = holdingExpenses.toMutableList().apply {
                            set(index, newExpense)
                        }
                    },
                    onRemove = {
                        holdingExpenses = holdingExpenses.toMutableList().apply {
                            removeAt(index)
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
            }
            TextButton(onClick = {
                holdingExpenses = holdingExpenses + ValuableExpense(
                    categoryId = expenseCategories.first(),
                    amount = 0.0
                )
            }) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir gasto de tenencia")
            }

            Spacer(Modifier.height(24.dp))

            // Botón guardar
            Button(
                onClick = {
                    val price = purchasePriceText.toDoubleOrNull() ?: return@Button
                    val estValue = estimatedValueText.toDoubleOrNull()
                    val valuable = Valuable(
                        id = existingValuable?.id ?: uuid4().toString(),
                        accountId = accountId,
                        name = name,
                        description = description,
                        purchasePrice = price,
                        purchaseDate = purchaseDateMillis,
                        estimatedValue = estValue,
                        salePrice = existingValuable?.salePrice,
                        saleDate = existingValuable?.saleDate,
                        linkedLoanId = existingValuable?.linkedLoanId,
                        notes = notes.ifBlank { null },
                        createdAt = existingValuable?.createdAt ?: nowMillis()
                    )
                    onSave(valuable, purchaseExpenses.filter { it.amount > 0 }, holdingExpenses.filter { it.amount > 0 })
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) {
                Text("Guardar", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ExpenseRow(
    expense: ValuableExpense,
    categories: List<String>,
    onExpenseChange: (ValuableExpense) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category selector
        var showCategoryMenu by remember { mutableStateOf(false) }
        Box(modifier = Modifier.weight(1f)) {
            val catName = expense.categoryId.removePrefix("cat_exp_val_")
            OutlinedButton(
                onClick = { showCategoryMenu = true },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(40.dp).fillMaxWidth()
            ) {
                Text(text = catName, fontSize = 12.sp, maxLines = 1)
            }
            DropdownMenu(
                expanded = showCategoryMenu,
                onDismissRequest = { showCategoryMenu = false }
            ) {
                categories.forEach { catId ->
                    DropdownMenuItem(
                        text = { Text(catId.removePrefix("cat_exp_val_"), fontSize = 13.sp) },
                        onClick = {
                            onExpenseChange(expense.copy(categoryId = catId))
                            showCategoryMenu = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = if (expense.amount > 0) expense.amount.toString() else "",
            onValueChange = { text ->
                val amt = text.toDoubleOrNull() ?: 0.0
                onExpenseChange(expense.copy(amount = amt))
            },
            label = { Text("Importe") },
            singleLine = true,
            modifier = Modifier.weight(0.7f),
            colors = textFieldColors()
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Outlined.Close, contentDescription = "Eliminar", tint = ExpenseRed)
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandGreen,
    unfocusedBorderColor = BorderGray,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = BrandGreen,
    focusedLabelColor = BrandGreen,
    unfocusedLabelColor = TextSecondary
)
