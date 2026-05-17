package es.aviferdev.n3to.ui.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.benasher44.uuid.uuid4
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.Loan
import es.aviferdev.n3to.domain.model.PropertyExpense
import es.aviferdev.n3to.domain.model.PropertyType
import es.aviferdev.n3to.domain.model.RealEstateProperty
import es.aviferdev.n3to.domain.model.RentalStatus
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.realestate.SavePropertyUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.common.component.SelectableChip
import es.aviferdev.n3to.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPropertyBottomSheet(
    existingProperty: RealEstateProperty? = null,
    accountId: String,
    availableLoans: List<Loan>,
    onDismiss: () -> Unit,
    onSave: (RealEstateProperty, List<PropertyExpense>) -> Unit,
    savePropertyUseCase: SavePropertyUseCase = koinInject(),
    getCategoriesByType: GetCategoriesByTypeUseCase = koinInject(),
    session: AccountSession = koinInject()
) {
    val isEditing = existingProperty != null
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(existingProperty?.name ?: "") }
    var address by remember { mutableStateOf(existingProperty?.address ?: "") }
    var selectedPropertyType by remember { mutableStateOf(existingProperty?.propertyType ?: PropertyType.PRIMARY_HOME) }
    var purchaseValueText by remember { mutableStateOf(existingProperty?.purchaseValue?.toString() ?: "") }
    var estimatedValueText by remember { mutableStateOf(existingProperty?.currentEstimatedValue?.toString() ?: "") }
    var acquisitionDateMillis by remember { mutableStateOf(existingProperty?.acquisitionDate ?: Clock.System.now().toEpochMilliseconds()) }
    var ownershipText by remember { mutableStateOf(existingProperty?.ownershipPercentage?.toString() ?: "100") }
    var selectedRentalStatus by remember { mutableStateOf(existingProperty?.rentalStatus ?: RentalStatus.OWN_USE) }
    var monthlyRentText by remember { mutableStateOf(existingProperty?.monthlyRent?.toString() ?: "") }
    var selectedLoanId by remember { mutableStateOf(existingProperty?.linkedLoanId) }
    var showLoanPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    // Gastos de compra
    var showPurchaseExpenses by remember { mutableStateOf(isEditing) }
    var purchaseExpenses by remember { mutableStateOf(listOf<PropertyExpense>()) }
    var expenseCategories by remember { mutableStateOf(listOf<Category>()) }
    var categoriesLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        if (!categoriesLoaded && accountId.isNotBlank()) {
            expenseCategories = getCategoriesByType(accountId, TransactionType.EXPENSE).firstOrNull() ?: emptyList()
            categoriesLoaded = true
        }
    }

    val validExpenseCategories = expenseCategories.filter { it.name != "Ajuste de saldo" }

    val purchaseValue = purchaseValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val estimatedValue = estimatedValueText.replace(',', '.').toDoubleOrNull() ?: 0.0
    val ownership = ownershipText.replace(',', '.').toDoubleOrNull() ?: 100.0
    val monthlyRent = monthlyRentText.replace(',', '.').toDoubleOrNull()

    val isValid = name.isNotBlank() && address.isNotBlank() &&
            purchaseValue > 0 && estimatedValue > 0 &&
            ownership in 0.0..100.0 &&
            (selectedRentalStatus != RentalStatus.RENTED || (monthlyRent != null && monthlyRent > 0))

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = acquisitionDateMillis)

    if (showLoanPicker) {
        LoanPickerSheet(
            loans = availableLoans, selectedLoanId = selectedLoanId,
            onLoanSelected = { loan -> selectedLoanId = loan?.id; showLoanPicker = false },
            onDismiss = { showLoanPicker = false }
        )
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { acquisitionDateMillis = it }; showDatePicker = false }) { Text("Aceptar", color = PrimaryDark) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = TextTertiary) } }
        ) { DatePicker(state = datePickerState, colors = DatePickerDefaults.colors(containerColor = SurfaceWhite)) }
    }

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                if (isEditing) "Editar propiedad" else "Nueva propiedad",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )

            Spacer(Modifier.height(20.dp))

            // ── Nombre ───────────────────────────────────────────────────────
            SectionLabel("Nombre")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                placeholder = { Text("Ej: Mi casa", color = TextTertiary.copy(alpha = 0.6f)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Dirección ────────────────────────────────────────────────────
            SectionLabel("Dirección")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = address, onValueChange = { address = it },
                placeholder = { Text("Ej: Calle Mayor 1, Madrid", color = TextTertiary.copy(alpha = 0.6f)) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Tipo de vivienda ─────────────────────────────────────────────
            SectionLabel("Tipo de vivienda")
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PropertyType.entries) { type ->
                    SelectableChip(
                        label = "${type.emoji} ${type.label}",
                        selected = selectedPropertyType == type,
                        onClick = { selectedPropertyType = type }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Valor de compra + Valor estimado ─────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = purchaseValueText, onValueChange = { purchaseValueText = it },
                    label = { Text("Compra (€)") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors()
                )
                OutlinedTextField(
                    value = estimatedValueText, onValueChange = { estimatedValueText = it },
                    label = { Text("Valor actual (€)") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors()
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Fecha de adquisición ─────────────────────────────────────────
            SectionLabel("Fecha de adquisición")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = formatDate(acquisitionDateMillis),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = fieldColors(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.CalendarMonth, "Seleccionar fecha", tint = TextTertiary)
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            // ── % de propiedad ───────────────────────────────────────────────
            SectionLabel("% de propiedad")
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = ownershipText, onValueChange = { ownershipText = it },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(10.dp), colors = fieldColors()
            )

            Spacer(Modifier.height(12.dp))

            // ── Estado de alquiler ───────────────────────────────────────────
            SectionLabel("Estado de alquiler")
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RentalStatus.entries) { status ->
                    SelectableChip(
                        label = "${status.emoji} ${status.label}",
                        selected = selectedRentalStatus == status,
                        onClick = { selectedRentalStatus = status }
                    )
                }
            }

            if (selectedRentalStatus == RentalStatus.RENTED) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = monthlyRentText, onValueChange = { monthlyRentText = it },
                    label = { Text("Renta mensual (€)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp), colors = fieldColors()
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Vincular hipoteca ────────────────────────────────────────────
            OutlinedButton(
                onClick = { showLoanPicker = true },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDark),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PrimaryDark)
                )
            ) {
                Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (selectedLoanId != null) "Cambiar hipoteca" else "Vincular hipoteca",
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Gastos de compra (expandible) ───────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).clickable { showPurchaseExpenses = !showPurchaseExpenses },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Gastos de compra",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        if (showPurchaseExpenses) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = TextTertiary
                    )
                }
                if (showPurchaseExpenses) {
                    TextButton(
                        onClick = {
                            val firstCat = validExpenseCategories.firstOrNull()?.id ?: ""
                            purchaseExpenses = purchaseExpenses + PropertyExpense(categoryId = firstCat, amount = 0.0)
                        }
                    ) {
                        Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp), tint = PrimaryDark)
                        Spacer(Modifier.width(4.dp))
                        Text("Añadir", fontSize = 12.sp, color = PrimaryDark)
                    }
                }
            }

            if (showPurchaseExpenses) {
                Spacer(Modifier.height(8.dp))
                if (purchaseExpenses.isEmpty()) {
                    Text(
                        "No hay gastos de compra. Pulsa \"Añadir\" para incluir notaría, ITP, tasación, etc.",
                        fontSize = 12.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    purchaseExpenses.forEachIndexed { index, expense ->
                        PropertyExpenseRow(
                            categories = validExpenseCategories,
                            expense = expense,
                            onExpenseChange = { updated ->
                                purchaseExpenses = purchaseExpenses.toMutableList().apply { set(index, updated) }
                            },
                            onRemove = {
                                purchaseExpenses = purchaseExpenses.toMutableList().apply { removeAt(index) }
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                if (purchaseExpenses.any { it.amount > 0 }) {
                    val totalPurchaseCosts = purchaseExpenses.sumOf { it.amount }
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text(
                            "Total gastos: ${formatAmountEuro(totalPurchaseCosts)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Botón guardar ───────────────────────────────────────────────
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val effectiveAccountId = if (accountId.isNotBlank()) accountId
                        else session.selectedAccountId.value ?: run {
                            isLoading = false
                            return@launch
                        }
                        val property = RealEstateProperty(
                            id = existingProperty?.id ?: uuid4().toString(),
                            accountId = effectiveAccountId,
                            name = name.trim(),
                            address = address.trim(),
                            propertyType = selectedPropertyType,
                            purchaseValue = purchaseValue,
                            currentEstimatedValue = estimatedValue,
                            acquisitionDate = acquisitionDateMillis,
                            ownershipPercentage = ownership,
                            linkedLoanId = selectedLoanId,
                            rentalStatus = selectedRentalStatus,
                            monthlyRent = if (selectedRentalStatus == RentalStatus.RENTED) monthlyRent else null,
                            mortgageReminderDismissed = existingProperty?.mortgageReminderDismissed ?: false,
                            archived = existingProperty?.archived ?: false
                        )
                        val validExpenses = purchaseExpenses.filter { it.amount > 0 }
                        savePropertyUseCase(property, validExpenses)
                            .onSuccess { onSave(property, validExpenses) }
                        isLoading = false
                    }
                },
                enabled = isValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.38f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (isEditing) "Guardar cambios" else "Añadir propiedad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 13.sp, color = TextSecondary)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryDark,
    unfocusedBorderColor = BorderGray,
    cursorColor = PrimaryDark,
    focusedLabelColor = PrimaryDark,
    unfocusedLabelColor = TextTertiary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

private fun formatDate(epochMillis: Long): String {
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.dayOfMonth}/${local.monthNumber}/${local.year}"
}
