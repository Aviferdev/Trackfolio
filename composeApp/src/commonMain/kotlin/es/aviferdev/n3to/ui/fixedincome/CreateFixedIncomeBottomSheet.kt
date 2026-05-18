package es.aviferdev.n3to.ui.fixedincome

import es.aviferdev.n3to.platform.nowMillis
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
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
import kotlinx.datetime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateFixedIncomeBottomSheet(
    platforms: List<es.aviferdev.n3to.domain.model.Platform>,
    categories: List<es.aviferdev.n3to.domain.model.AssetCategory>,
    bondIssuers: List<Issuer>,
    bankIssuers: List<Issuer>,
    accountId: String,
    onSave: (FixedIncomePosition, FixedIncomeEvent) -> Unit,
    onSaveIssuer: (String, String, IssuerType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(FixedIncomeType.DEPOSIT) }
    var selectedFrequency by remember { mutableStateOf(InterestFrequency.AT_MATURITY) }
    var durationMonthsTotal by remember { mutableStateOf(6) } // Duración total en meses
    var startDateMillis by remember { mutableStateOf(nowMillis()) }
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

    // Región y sector para distribución
    var selectedRegion by remember { mutableStateOf<String?>(null) }
    var selectedSector by remember { mutableStateOf<String?>(null) }

    // Valores predefinidos para región y sector
    val availableRegions = listOf("Europa", "EE.UU.", "España", "Emerging Markets", "Global")
    val availableSectors = listOf("Gobierno", "Corporativo", "Banca", "Energía", "Inmobiliario", "Otro")

    // Frecuencias permitidas para el tipo seleccionado
    val allowedFrequencies = selectedType.allowedFrequencies.toList()

    // Cuando cambia el tipo, reseteamos el emisor, el diálogo y la frecuencia
    LaunchedEffect(selectedType) {
        selectedIssuer = null
        newIssuerIcon = when (selectedType) {
            FixedIncomeType.DEPOSIT -> "🏦"
            FixedIncomeType.BILL -> "📋"
            FixedIncomeType.BOND -> "📜"
            FixedIncomeType.GOVERNMENT_OBLIGATION -> "🏛️"
            FixedIncomeType.CORPORATE_BOND -> "🏢"
        }
        // Resetear frecuencia a la primera permitida por el tipo
        selectedFrequency = selectedType.allowedFrequencies.first()
    }

    // Determinar qué lista de emisores mostrar según el tipo
    val currentIssuers = when (selectedType) {
        FixedIncomeType.DEPOSIT -> bankIssuers
        FixedIncomeType.BILL, FixedIncomeType.BOND, FixedIncomeType.GOVERNMENT_OBLIGATION, FixedIncomeType.CORPORATE_BOND -> bondIssuers
    }

    // Determinar el IssuerType para crear nuevos emisores
    val currentIssuerType = when (selectedType) {
        FixedIncomeType.DEPOSIT -> IssuerType.BANK
        FixedIncomeType.BILL, FixedIncomeType.BOND, FixedIncomeType.GOVERNMENT_OBLIGATION, FixedIncomeType.CORPORATE_BOND -> IssuerType.BOND_ISSUER
    }

    // Calcular fecha de vencimiento a partir de inicio + duración
    fun calculateMaturity(): Long {
        val startInstant = Instant.fromEpochMilliseconds(startDateMillis)
        val years = durationMonthsTotal / 12
        val months = durationMonthsTotal % 12
        return try {
            startInstant.plus(
                DateTimePeriod(years = years, months = months),
                TimeZone.currentSystemDefault()
            ).toEpochMilliseconds()
        } catch (e: Exception) {
            startDateMillis + (durationMonthsTotal.toLong() * 30L) * 24 * 60 * 60 * 1000
        }
    }

    val maturityDateMillis = remember(startDateMillis, durationMonthsTotal) {
        calculateMaturity()
    }

    fun parseInterestRate(input: String): Double? {
        val normalized = input.replace(",", ".")
        return normalized.toDoubleOrNull()
    }

    // Generar nombre automático
    fun buildAutoName(): String {
        val typeLabel = selectedType.label
        val years = durationMonthsTotal / 12
        val months = durationMonthsTotal % 12
        val duration = when {
            years > 0 && months > 0 -> "$years años $months meses"
            years > 0 -> "$years año${if (years > 1) "s" else ""}"
            months > 0 -> "$months mes${if (months > 1) "es" else ""}"
            else -> "0 meses"
        }
        val issuer = selectedIssuer?.name ?: ""
        val freqLabel = selectedFrequency.label
        val rate = parseInterestRate(interestRateStr)
        val rateLabel = if (rate != null && rate > 0) "${rate}%" else ""
        return listOf(typeLabel, duration, issuer, rateLabel, "($freqLabel)").filter { it.isNotBlank() }.joinToString(" ")
    }

    val isValid = principalStr.toDoubleOrNull() != null &&
            parseInterestRate(interestRateStr) != null &&
            selectedIssuer != null &&
            durationMonthsTotal > 0

    val entityLabel = when (selectedType) {
        FixedIncomeType.DEPOSIT -> "Entidad financiera"
        FixedIncomeType.BILL -> "Emisor (Estado)"
        FixedIncomeType.BOND, FixedIncomeType.GOVERNMENT_OBLIGATION, FixedIncomeType.CORPORATE_BOND -> "Emisor (Estado/Empresa)"
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
            Text("Duración (meses)", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))

            // Slider de duración en meses (máximo 60 meses = 5 años)
            Slider(
                value = durationMonthsTotal.toFloat(),
                onValueChange = { durationMonthsTotal = it.toInt() },
                valueRange = 1f..60f,
                steps = 58,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = PrimaryDark,
                    activeTrackColor = PrimaryDark
                )
            )

            // Tag que muestra la duración en años y meses
            val yearsDisplay = durationMonthsTotal / 12
            val monthsDisplay = durationMonthsTotal % 12
            val durationLabel = when {
                yearsDisplay > 0 && monthsDisplay > 0 -> "$yearsDisplay años y $monthsDisplay meses"
                yearsDisplay > 0 -> "$yearsDisplay año${if (yearsDisplay > 1) "s" else ""}"
                else -> "$monthsDisplay mes${if (monthsDisplay > 1) "es" else ""}"
            }
            Surface(
                color = PrimaryDark.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = durationLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryDark,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Guía de duración típica (no bloqueante)
            Text(
                text = "Rango habitual para ${selectedType.label}: ${selectedType.typicalMinMonths} - ${selectedType.typicalMaxMonths} meses",
                fontSize = 11.sp,
                color = TextTertiary,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── Fecha de inicio ───────────────────────────────────────
            OutlinedTextField(
                value = formatDate(startDateMillis),
                onValueChange = { },
                label = { Text("Fecha de inicio") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Seleccionar fecha",
                            tint = PrimaryDark
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryDark)
            )

            Spacer(Modifier.height(8.dp))

            // ── Fecha de vencimiento (auto-calculada) ──────────────────
            Text("Fecha de vencimiento", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(maturityDateMillis),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Calculada automáticamente",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }

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

            // ── Región para distribución ───────────────────────────────────
            Text("Región", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableRegions.forEach { region ->
                    FilterChip(
                        selected = selectedRegion == region,
                        onClick = { selectedRegion = if (selectedRegion == region) null else region },
                        label = { Text(region) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDark,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Sector para distribución ───────────────────────────────────
            Text("Sector", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableSectors.forEach { sector ->
                    FilterChip(
                        selected = selectedSector == sector,
                        onClick = { selectedSector = if (selectedSector == sector) null else sector },
                        label = { Text(sector) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryDark,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Capital (y Nominal para bonos) ─────────────────────────────
            // Para depósitos: solo Capital (Nominal = Capital)
            // Para bonos/letras: Capital y Nominal pueden ser diferentes
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
                        value = nominalPerUnitStr.ifBlank { principalStr },
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
                    // Para depósitos: nominal = capital. Para bonos: usar valor del campo o default a capital
                    val nominal = if (selectedType == FixedIncomeType.DEPOSIT) {
                        principal
                    } else {
                        nominalPerUnitStr.toDoubleOrNull() ?: principal
                    }
                    val rate = parseInterestRate(interestRateStr) ?: 0.0
                    val now = nowMillis()
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
                        interestFrequency = selectedFrequency,
                        startDate = startDateMillis,
                        maturityDate = maturityDateMillis,
                        platformId = "",
                        issuerId = selectedIssuer?.id,
                        region = selectedRegion,
                        sector = selectedSector,
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
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { startDateMillis = it }
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
            DatePicker(state = pickerState)
        }
    }
}

private fun createMockPlatforms(): List<Platform> {
    val now = nowMillis()
    return listOf(
        Platform(id = "1", name = "Banco Sabadell", icon = "🏦", sortOrder = 0, archived = false, createdAt = now),
        Platform(id = "2", name = "ING", icon = "🏦", sortOrder = 1, archived = false, createdAt = now)
    )
}

private fun createMockIssuers(): List<Issuer> {
    val now = nowMillis()
    return listOf(
        Issuer(id = "1", accountId = "acc1", name = "Banco de España", icon = "🏛️", type = IssuerType.BOND_ISSUER, createdAt = now),
        Issuer(id = "2", accountId = "acc1", name = "Santander", icon = "🏦", type = IssuerType.BANK, createdAt = now)
    )
}

@Preview
@Composable
private fun CreateFixedIncomeBottomSheetPreview() {
    N3toTheme {
        CreateFixedIncomeBottomSheet(
            platforms = createMockPlatforms(),
            categories = emptyList(),
            bondIssuers = createMockIssuers(),
            bankIssuers = createMockIssuers(),
            accountId = "acc1",
            onSave = { _, _ -> },
            onSaveIssuer = { _, _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun CreateFixedIncomeBottomSheetWithAccountPreview() {
    N3toTheme {
        CreateFixedIncomeBottomSheet(
            platforms = createMockPlatforms(),
            categories = emptyList(),
            bondIssuers = createMockIssuers(),
            bankIssuers = createMockIssuers(),
            accountId = "acc1",
            onSave = { _, _ -> },
            onSaveIssuer = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
