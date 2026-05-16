package es.aviferdev.trackfolio.ui.realestate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
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
import es.aviferdev.trackfolio.domain.model.Loan
import es.aviferdev.trackfolio.domain.model.PropertyType
import es.aviferdev.trackfolio.domain.model.RealEstateProperty
import es.aviferdev.trackfolio.domain.model.RentalStatus
import es.aviferdev.trackfolio.domain.usecase.realestate.SavePropertyUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.common.component.SelectableChip
import es.aviferdev.trackfolio.ui.theme.*
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
    onSave: (RealEstateProperty) -> Unit,
    savePropertyUseCase: SavePropertyUseCase = koinInject(),
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
        dragHandle = { Box(Modifier.padding(top = 12.dp, bottom = 4.dp).width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFBDBDBD))) }
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp).verticalScroll(rememberScrollState())) {
            Text(if (isEditing) "Editar propiedad" else "Nueva propiedad", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, placeholder = { Text("Ej: Mi casa") }, singleLine = true, colors = fieldColors(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección") }, placeholder = { Text("Ej: Calle Mayor 1, Madrid") }, singleLine = true, colors = fieldColors(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            Text("Tipo de vivienda", fontSize = 13.sp, color = TextSecondary); Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PropertyType.entries) { type -> SelectableChip(label = "${type.emoji} ${type.label}", selected = selectedPropertyType == type, onClick = { selectedPropertyType = type }) }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = purchaseValueText, onValueChange = { purchaseValueText = it }, label = { Text("Valor de compra (€)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = fieldColors(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = estimatedValueText, onValueChange = { estimatedValueText = it }, label = { Text("Valor estimado actual (€)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = fieldColors(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            Text("Fecha de adquisición", fontSize = 13.sp, color = TextSecondary); Spacer(Modifier.height(4.dp))
            OutlinedTextField(value = formatDate(acquisitionDateMillis), onValueChange = {}, readOnly = true, colors = fieldColors(), modifier = Modifier.fillMaxWidth(), trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Outlined.CalendarMonth, "Seleccionar fecha", tint = TextTertiary) } })
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(value = ownershipText, onValueChange = { ownershipText = it }, label = { Text("% de propiedad") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = fieldColors(), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            Text("Estado de alquiler", fontSize = 13.sp, color = TextSecondary); Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(RentalStatus.entries) { status -> SelectableChip(label = "${status.emoji} ${status.label}", selected = selectedRentalStatus == status, onClick = { selectedRentalStatus = status }) }
            }
            Spacer(Modifier.height(12.dp))

            if (selectedRentalStatus == RentalStatus.RENTED) {
                OutlinedTextField(value = monthlyRentText, onValueChange = { monthlyRentText = it }, label = { Text("Renta mensual (€)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = fieldColors(), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }

            // Loan picker CTA
            OutlinedButton(
                onClick = { showLoanPicker = true },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (selectedLoanId != null) "Cambiar hipoteca" else "Vincular hipoteca", fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val effectiveAccountId = if (accountId.isNotBlank()) accountId else session.selectedAccountId.value ?: run { isLoading = false; return@launch }
                        val property = RealEstateProperty(
                            id = existingProperty?.id ?: uuid4().toString(),
                            accountId = effectiveAccountId,
                            name = name.trim(), address = address.trim(),
                            propertyType = selectedPropertyType,
                            purchaseValue = purchaseValue, currentEstimatedValue = estimatedValue,
                            acquisitionDate = acquisitionDateMillis,
                            ownershipPercentage = ownership,
                            linkedLoanId = selectedLoanId,
                            rentalStatus = selectedRentalStatus,
                            monthlyRent = if (selectedRentalStatus == RentalStatus.RENTED) monthlyRent else null,
                            mortgageReminderDismissed = existingProperty?.mortgageReminderDismissed ?: false,
                            archived = existingProperty?.archived ?: false
                        )
                        savePropertyUseCase(property).fold(onSuccess = { onSave(property) }, onFailure = {})
                        isLoading = false
                    }
                },
                enabled = isValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (isEditing) "Guardar cambios" else "Añadir propiedad", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryDark, unfocusedBorderColor = BorderGray,
    cursorColor = PrimaryDark, focusedLabelColor = PrimaryDark,
    unfocusedLabelColor = TextTertiary, focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

private fun formatDate(epochMillis: Long): String {
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.dayOfMonth}/${local.monthNumber}/${local.year}"
}
