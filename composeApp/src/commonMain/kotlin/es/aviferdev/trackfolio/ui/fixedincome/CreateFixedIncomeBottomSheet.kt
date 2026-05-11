package es.aviferdev.trackfolio.ui.fixedincome

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.*
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateFixedIncomeBottomSheet(
    platforms: List<es.aviferdev.trackfolio.domain.model.Platform>,
    categories: List<es.aviferdev.trackfolio.domain.model.AssetCategory>,
    bondIssuers: List<Issuer>,
    bankIssuers: List<Issuer>,
    accountId: String,
    onSave: (FixedIncomePosition, FixedIncomeEvent) -> Unit,
    onSaveIssuer: (String, String, IssuerType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(FixedIncomeType.DEPOSIT) }
    var durationYears by remember { mutableStateOf(0) }
    var durationMonths by remember { mutableStateOf(6) }
    var startDateMillis by remember { mutableStateOf(Clock.System.now().toEpochMilliseconds()) }
    var principalStr by remember { mutableStateOf("") }
    var nominalPerUnitStr by remember { mutableStateOf("") }
    var interestRateStr by remember { mutableStateOf("") }
    var selectedIssuer by remember { mutableStateOf<Issuer?>(null) }
    var issuerDropdownExpanded by remember { mutableStateOf(false) }
    var showNewIssuerDialog by remember { mutableStateOf(false) }
    var newIssuerName by remember { mutableStateOf("") }
    var newIssuerIcon by remember { mutableStateOf("🏦") }
    var feeNote by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Cuando cambia el tipo, reseteamos el emisor y el diálogo de nuevo emisor
    LaunchedEffect(selectedType) {
        selectedIssuer = null
        newIssuerIcon = when (selectedType) {
            FixedIncomeType.DEPOSIT -> "🏦"
            else -> "📜"
        }
    }

    // Determinar qué lista de emisores mostrar según el tipo
    val currentIssuers = when (selectedType) {
        FixedIncomeType.DEPOSIT -> bankIssuers
        FixedIncomeType.BOND, FixedIncomeType.BILL -> bondIssuers
    }

    // Determinar el IssuerType para crear nuevos emisores
    val currentIssuerType = when (selectedType) {
        FixedIncomeType.DEPOSIT -> IssuerType.BANK
        FixedIncomeType.BOND, FixedIncomeType.BILL -> IssuerType.BOND_ISSUER
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
        return listOf(typeLabel, duration, issuer).filter { it.isNotBlank() }.joinToString(" ")
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
        FixedIncomeType.BOND -> "Emisor (Estado/Empresa)"
        FixedIncomeType.BILL -> "Emisor (Estado)"
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
                text = "Nueva posición de renta fija",
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
                        label = { Text("${type.emoji} ${type.label}") },
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
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
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFBDBDBD)
                ),
                supportingText = { Text("Calculada automáticamente", color = TextSecondary, fontSize = 11.sp) }
            )

            Spacer(Modifier.height(12.dp))

            // ── Emisor ────────────────────────────────────────────────
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

            // ── Capital y Nominal ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Capital (€)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
                )
                OutlinedTextField(
                    value = nominalPerUnitStr.ifBlank { principalStr },
                    onValueChange = { nominalPerUnitStr = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Nominal (€)") },
                    modifier = Modifier.weight(1f),
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

            // ── Botón crear ───────────────────────────────────────────
            Button(
                onClick = {
                    val principal = principalStr.toDoubleOrNull() ?: 0.0
                    val nominal = nominalPerUnitStr.toDoubleOrNull() ?: principal
                    val rate = parseInterestRate(interestRateStr) ?: 0.0
                    val now = Clock.System.now().toEpochMilliseconds()
                    val positionId = "fi_${now}"
                    val eventId = "fie_${now}"

                    val position = FixedIncomePosition(
                        id = positionId,
                        accountId = accountId,
                        assetCategoryId = null,
                        name = buildAutoName(),
                        ticker = "",
                        type = selectedType,
                        notes = null,
                        principal = principal,
                        quantity = 1.0,
                        nominalPerUnit = nominal,
                        interestRate = rate,
                        interestFrequency = InterestFrequency.AT_MATURITY,
                        startDate = startDateMillis,
                        maturityDate = maturityDateMillis,
                        platformId = "",
                        issuerId = selectedIssuer?.id,
                        autoRenew = false,
                        archived = false,
                        closedAt = null,
                        closeType = null,
                        feeNote = feeNote.ifBlank { null },
                        createdAt = now
                    )

                    val acquisitionEvent = FixedIncomeEvent(
                        id = eventId,
                        positionId = positionId,
                        type = FixedIncomeEventType.ACQUISITION,
                        grossAmount = principal,
                        irpfPercent = 0.0,
                        commissionAmount = 0.0,
                        netAmount = -principal,
                        date = startDateMillis,
                        notes = "Adquisición de ${position.name}",
                        createdAt = now
                    )

                    onSave(position, acquisitionEvent)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear posición", fontSize = 15.sp, modifier = Modifier.padding(vertical = 4.dp))
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
                                label = { Text(icon, fontSize = 18.sp) }
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
