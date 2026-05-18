package es.aviferdev.n3to.ui.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.*
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.*
import es.aviferdev.n3to.ui.theme.DragHandleColor
import kotlinx.datetime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun EditFixedIncomeBottomSheet(
    position: FixedIncomePosition,
    platforms: List<Platform>,
    bondIssuers: List<Issuer>,
    bankIssuers: List<Issuer>,
    onSave: (FixedIncomePosition) -> Unit,
    onSaveIssuer: (String, String, IssuerType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(position.type) }
    var selectedFrequency by remember { mutableStateOf(position.interestFrequency) }
    var durationYears by remember {
        mutableStateOf((position.totalTermDays / 365).toInt())
    }
    var durationMonths by remember {
        mutableStateOf(((position.totalTermDays % 365) / 30).toInt())
    }
    var startDateMillis by remember { mutableStateOf(position.startDate) }
    var principalStr by remember { mutableStateOf(position.principal.toString()) }
    var nominalPerUnitStr by remember { mutableStateOf(position.nominalPerUnit.toString()) }
    var interestRateStr by remember { mutableStateOf(position.interestRate.toString()) }
    var selectedIssuer by remember {
        mutableStateOf(
            bondIssuers.find { it.id == position.issuerId }
                ?: bankIssuers.find { it.id == position.issuerId }
        )
    }
    var issuerDropdownExpanded by remember { mutableStateOf(false) }
    var showNewIssuerDialog by remember { mutableStateOf(false) }
    var newIssuerName by remember { mutableStateOf("") }
    var newIssuerIcon by remember { mutableStateOf("🏦") }
    var feeNote by remember { mutableStateOf(position.feeNote ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var autoRenew by remember { mutableStateOf(position.autoRenew) }

    // Frecuencias permitidas para el tipo seleccionado
    val allowedFrequencies = selectedType.allowedFrequencies.toList()

    // Determinar qué lista de emisores mostrar según el tipo
    val currentIssuers = when (selectedType) {
        FixedIncomeType.DEPOSIT -> bankIssuers
        FixedIncomeType.BOND, FixedIncomeType.BILL, FixedIncomeType.GOVERNMENT_OBLIGATION, FixedIncomeType.CORPORATE_BOND -> bondIssuers
    }

    // Determinar el IssuerType para crear nuevos emisores
    val currentIssuerType = when (selectedType) {
        FixedIncomeType.DEPOSIT -> IssuerType.BANK
        else -> IssuerType.BOND_ISSUER
    }

    // Calcular fecha de vencimiento a partir de inicio + duración
    fun calculateMaturity(): Long {
        val startInstant = Instant.fromEpochMilliseconds(startDateMillis)
        return try {
            startInstant.plus(
                DateTimePeriod(years = durationYears, months = durationMonths),
                TimeZone.currentSystemDefault()
            ).toEpochMilliseconds()
        } catch (e: Exception) {
            startDateMillis + (durationYears * 365L + durationMonths * 30L) * 24 * 60 * 60 * 1000
        }
    }

    val maturityDateMillis = remember(startDateMillis, durationYears, durationMonths) {
        calculateMaturity()
    }

    // Generar nombre automático
    fun buildAutoName(): String {
        val typeLabel = selectedType.label
        val duration = when {
            durationYears > 0 && durationMonths > 0 -> "$durationYears años $durationMonths meses"
            durationYears > 0 -> "$durationYears años"
            durationMonths > 0 -> "$durationMonths meses"
            durationYears == 0 && durationMonths == 0 -> "0 meses"
            else -> ""
        }
        val issuer = selectedIssuer?.name ?: ""
        val freqLabel = selectedFrequency.label
        return listOf(typeLabel, duration, issuer, "($freqLabel)").filter { it.isNotBlank() }.joinToString(" ")
    }

    fun parseInterestRate(input: String): Double? {
        val normalized = input.replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    val isValid = principalStr.toDoubleOrNull() != null &&
            parseInterestRate(interestRateStr) != null &&
            selectedIssuer != null &&
            (durationYears >= 0 && durationMonths >= 0) &&
            (durationYears * 12 + durationMonths >= 0)

    val entityLabel = when (selectedType) {
        FixedIncomeType.DEPOSIT -> "Entidad financiera"
        FixedIncomeType.BOND, FixedIncomeType.BILL, FixedIncomeType.GOVERNMENT_OBLIGATION, FixedIncomeType.CORPORATE_BOND -> "Emisor (Estado/Empresa)"
    }

    val iconsByType = when (selectedType) {
        FixedIncomeType.DEPOSIT -> listOf("🏦", "🏛️", "🏢", "💰")
        else -> listOf("📜", "🏛️", "🏢", "🌍")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Editar posición de renta fija",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(20.dp))

            // ── Tipo de producto ──────────────────────────────────────
            Text("Tipo de producto", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FixedIncomeType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(type.toMaterialIcon(), contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(type.label, maxLines = 1)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDark,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Frecuencia de intereses ───────────────────────────────
            Text("Frecuencia de cobro de intereses", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allowedFrequencies.forEach { freq ->
                    FilterChip(
                        selected = selectedFrequency == freq,
                        onClick = { selectedFrequency = freq },
                        label = { Text(freq.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDark,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Duración ──────────────────────────────────────────────
            Text("Duración", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = if (durationYears > 0) durationYears.toString() else "",
                    onValueChange = { v ->
                        durationYears = v.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                    },
                    label = { Text("Años") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
                OutlinedTextField(
                    value = if (durationMonths > 0) durationMonths.toString() else "",
                    onValueChange = { v ->
                        durationMonths = v.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                    },
                    label = { Text("Meses") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
            }

            // Guía de duración típica (no bloqueante)
            Text(
                text = "Rango habitual para ${selectedType.label}: ${selectedType.typicalMinMonths} - ${selectedType.typicalMaxMonths} meses",
                fontSize = 11.sp,
                color = TextTertiary,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Fecha de inicio ───────────────────────────────────────
            OutlinedTextField(
                value = formatDate(startDateMillis),
                onValueChange = { },
                label = { Text("Fecha de inicio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(8.dp))

            // ── Fecha de vencimiento (auto-calculada) ──────────────────
            OutlinedTextField(
                value = formatDate(maturityDateMillis),
                onValueChange = { },
                label = { Text("Fecha de vencimiento") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = DragHandleColor
                ),
                supportingText = { Text("Calculada automáticamente", color = TextSecondary, fontSize = 11.sp) }
            )

            Spacer(Modifier.height(12.dp))

            // ── Emisor ───────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = issuerDropdownExpanded,
                onExpandedChange = { issuerDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedIssuer?.let { "${it.icon} ${it.name}" } ?: "",
                    onValueChange = { },
                    label = { Text(entityLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issuerDropdownExpanded) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
                ExposedDropdownMenu(
                    expanded = issuerDropdownExpanded,
                    onDismissRequest = { issuerDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("➕ Nuevo emisor", color = PrimaryDark) },
                        onClick = {
                            issuerDropdownExpanded = false
                            newIssuerIcon = when (selectedType) {
                                FixedIncomeType.DEPOSIT -> "🏦"
                                else -> "📜"
                            }
                            showNewIssuerDialog = true
                        }
                    )
                    HorizontalDivider()
                    if (currentIssuers.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay emisores. Crea uno nuevo.", color = TextSecondary) },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        currentIssuers.forEach { issuer ->
                            DropdownMenuItem(
                                text = { Text("${issuer.icon} ${issuer.name}") },
                                onClick = {
                                    selectedIssuer = issuer
                                    issuerDropdownExpanded = false
                                },
                                trailingIcon = if (selectedIssuer?.id == issuer.id) {
                                    { Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryDark) }
                                } else null
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Capital (y Nominal para bonos) ─────────────────────────────
            val showNominalField = selectedType != FixedIncomeType.DEPOSIT

            if (showNominalField) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Capital invertido (€)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                    )
                    OutlinedTextField(
                        value = nominalPerUnitStr,
                        onValueChange = { nominalPerUnitStr = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Valor nominal (€)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                    )
                }
                Text(
                    text = "El nominal es el valor facial del bono (base para calcular intereses y amortización)",
                    fontSize = 10.sp,
                    color = TextTertiary,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            } else {
                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Capital (€)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── TAE ───────────────────────────────────────────────────
            OutlinedTextField(
                value = interestRateStr,
                onValueChange = { interestRateStr = it.filter { c -> c.isDigit() || c == '.' || c == ',' } },
                label = { Text("TAE (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(12.dp))

            // ── Auto-renovación ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = autoRenew,
                    onCheckedChange = { autoRenew = it },
                    colors = CheckboxDefaults.colors(checkedColor = PrimaryDark)
                )
                Text(
                    text = "Auto-renovar al vencimiento",
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Notas ─────────────────────────────────────────────────
            OutlinedTextField(
                value = feeNote,
                onValueChange = { feeNote = it },
                label = { Text("Notas de comisiones (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            // ── Nombre generado ──────────────────────────────────────
            if (isValid) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = buildAutoName(),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Botón guardar ───────────────────────────────────────────
            Button(
                onClick = {
                    val principal = principalStr.toDoubleOrNull() ?: 0.0
                    // Para depósitos: nominal = capital. Para bonos: usar valor del campo o default a capital
                    val nominal = if (selectedType == FixedIncomeType.DEPOSIT) {
                        principal
                    } else {
                        nominalPerUnitStr.toDoubleOrNull() ?: principal
                    }
                    val rate = parseInterestRate(interestRateStr) ?: 0.0

                    val updatedPosition = position.copy(
                        name = buildAutoName(),
                        type = selectedType,
                        interestFrequency = selectedFrequency,
                        principal = principal,
                        nominalPerUnit = nominal,
                        interestRate = rate,
                        startDate = startDateMillis,
                        maturityDate = maturityDateMillis,
                        issuerId = selectedIssuer?.id,
                        autoRenew = autoRenew,
                        feeNote = feeNote.ifBlank { null }
                    )

                    onSave(updatedPosition)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Guardar cambios", fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Diálogo nuevo emisor ──────────────────────────────────────
    if (showNewIssuerDialog) {
        AlertDialog(
            onDismissRequest = { showNewIssuerDialog = false },
            title = { Text("Nuevo emisor") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newIssuerName,
                        onValueChange = { newIssuerName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Icono", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconsByType.forEach { icon ->
                            FilterChip(
                                selected = newIssuerIcon == icon,
                                onClick = { newIssuerIcon = icon },
                                label = { Icon(icon.toMaterialIcon(), contentDescription = null, modifier = Modifier.size(20.dp)) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newIssuerName.isNotBlank()) {
                            onSaveIssuer(newIssuerName, newIssuerIcon, currentIssuerType)
                            showNewIssuerDialog = false
                            newIssuerName = ""
                        }
                    },
                    enabled = newIssuerName.isNotBlank()
                ) {
                    Text("Crear", color = PrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewIssuerDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // ── DatePicker (fecha de inicio) ──────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDateMillis = it }
                    showDatePicker = false
                }) {
                    Text("Aceptar", color = PrimaryDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview
@Composable
private fun EditFixedIncomeBottomSheetPreview() {
    N3toTheme {
        val now = nowMillis()
        val samplePosition = FixedIncomePosition(
            id = "pos-1",
            accountId = "acc-1",
            assetCategoryId = null,
            name = "Bono Tesoro 2025",
            ticker = "ES0000000001",
            type = FixedIncomeType.BOND,
            notes = null,
            principal = 10000.0,
            quantity = 100.0,
            nominalPerUnit = 100.0,
            interestRate = 4.5,
            interestFrequency = InterestFrequency.ANNUAL,
            startDate = now - 30L * 24 * 60 * 60 * 1000,
            maturityDate = now + 335L * 24 * 60 * 60 * 1000,
            platformId = "platform-1",
            issuerId = "issuer-1",
            autoRenew = false,
            archived = false,
            closedAt = null,
            closeType = null,
            feeNote = null,
            createdAt = now - 30L * 24 * 60 * 60 * 1000
        )
        val samplePlatforms = listOf(
            Platform("platform-1", "Banco Santander", "🏦", 0, false, now),
            Platform("platform-2", "BBVA", "🏛️", 1, false, now)
        )
        val sampleIssuers = listOf(
            Issuer(id = "issuer-1", accountId = "acc-1", name = "Tesoro Público", type = IssuerType.BOND_ISSUER, icon = "📊", archived = false, createdAt = now),
            Issuer(id = "issuer-2", accountId = "acc-1", name = "Banco de España", type = IssuerType.BANK, icon = "🏦", archived = false, createdAt = now)
        )

        EditFixedIncomeBottomSheet(
            position = samplePosition,
            platforms = samplePlatforms,
            bondIssuers = sampleIssuers,
            bankIssuers = sampleIssuers,
            onSave = {},
            onSaveIssuer = { _, _, _ -> },
            onDismiss = {}
        )
    }
}