package es.aviferdev.n3to.ui.settings.goal

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.platform.nowMonth
import es.aviferdev.n3to.platform.nowYear
import es.aviferdev.n3to.ui.common.help.HelpContent
import es.aviferdev.n3to.ui.common.help.HelpKeys
import es.aviferdev.n3to.ui.common.help.HelpTooltipIcon
import es.aviferdev.n3to.ui.common.input.InlineAmountField
import es.aviferdev.n3to.ui.common.navigation.TimeStepperHeader
import es.aviferdev.n3to.ui.common.navigation.TopBarApp

import es.aviferdev.n3to.ui.theme.ExpenseRed

import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalSettingsScreen(
    navigateBack: () -> Unit = {},
    viewModel: GoalSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedMonthKey by remember { mutableStateOf<String?>(null) }
    val todayYear = remember { nowYear() }
    val todayMonth = remember { nowMonth() }

    fun isPast(monthKey: String): Boolean {
        val y = state.year.toIntOrNull() ?: return false
        val m = monthKey.toIntOrNull() ?: return false
        return y < todayYear || (y == todayYear && m < todayMonth)
    }

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(2000)
            viewModel.clearMessage()
        }
    }

    // Deselect if the year changes (data reload)
    LaunchedEffect(state.year) { selectedMonthKey = null }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        TopBarApp(
            title = "Objetivos mensuales",
            navigateBack = {
                if (state.isDirty) viewModel.save()
                navigateBack()
            },
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder
        )

        TimeStepperHeader(
            currentValue = state.year,
            canGoBack = state.canGoPrevious,
            onPrevious = { viewModel.previousYear() },
            onNext = {},
            containerColor = MaterialTheme.appColors.navySurface,
            dividerColor = MaterialTheme.appColors.navyBorder
        )

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.appColors.cyanAccent, strokeWidth = 2.dp)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ── Hero card ──
                    item { GoalSettingsHeroCard() }

                    // ── Ahorro base ──
                    item {
                        NavySectionCard {
                            GoalTypeHeader(
                                icon = {
                                    Icon(
                                        Icons.Outlined.Savings,
                                        contentDescription = null,
                                        tint = MaterialTheme.appColors.income,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                iconBackground = MaterialTheme.appColors.income.copy(alpha = 0.12f),
                                label = "AHORRO MENSUAL BASE",
                                description = "Lo que reservas cada mes como ahorro líquido o en cuenta de ahorro."
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.baseSavingsText,
                                onValueChange = viewModel::onBaseSavingsChange,
                                placeholder = { Text("Ej: 500", color = MaterialTheme.appColors.textTertiary) },
                                trailingIcon = {
                                    Text(
                                        "€",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.appColors.textTertiary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.appColors.income,
                                    unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
                                    cursorColor = MaterialTheme.appColors.income,
                                    focusedTextColor = MaterialTheme.appColors.textPrimary,
                                    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
                                    focusedContainerColor = MaterialTheme.appColors.navyDeep,
                                    unfocusedContainerColor = MaterialTheme.appColors.navyDeep
                                ),
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── Inversión base ──
                    item {
                        NavySectionCard {
                            GoalTypeHeader(
                                icon = {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.appColors.cyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                iconBackground = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.12f),
                                label = "INVERSIÓN MENSUAL BASE",
                                description = "Lo que destinas a compra de activos: acciones, fondos, ETFs, etc."
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.baseInvestmentText,
                                onValueChange = viewModel::onBaseInvestmentChange,
                                placeholder = { Text("Ej: 300", color = MaterialTheme.appColors.textTertiary) },
                                trailingIcon = {
                                    Text(
                                        "€",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.appColors.textTertiary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.appColors.cyanAccent,
                                    unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
                                    cursorColor = MaterialTheme.appColors.cyanAccent,
                                    focusedTextColor = MaterialTheme.appColors.textPrimary,
                                    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
                                    focusedContainerColor = MaterialTheme.appColors.navyDeep,
                                    unfocusedContainerColor = MaterialTheme.appColors.navyDeep
                                ),
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── Personalizar por meses ──
                    item {
                        NavySectionCard {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "PERSONALIZAR MESES",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.appColors.textTertiary
                                    )
                                    Text(
                                        text = if (state.customizedCount > 0)
                                            "${state.customizedCount} ${if (state.customizedCount == 1) "mes personalizado" else "meses personalizados"}"
                                        else
                                            "Toca un mes para ajustarlo",
                                        fontSize = 11.sp,
                                        color = if (state.customizedCount > 0) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textTertiary
                                    )
                                }
                                HelpTooltipIcon(
                                    title = HelpContent.texts[HelpKeys.GOAL_CUSTOMIZE]?.title ?: "",
                                    body  = HelpContent.texts[HelpKeys.GOAL_CUSTOMIZE]?.body  ?: ""
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // Month chip grid (2 rows of 6)
                            state.months.chunked(6).forEach { rowMonths ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowMonths.forEach { month ->
                                        MonthChip(
                                            label = month.monthLabel.take(3),
                                            isSelected = selectedMonthKey == month.month,
                                            isCustomized = month.isCustomized,
                                            isPast = isPast(month.month),
                                            onClick = {
                                                if (!isPast(month.month)) {
                                                    selectedMonthKey =
                                                        if (selectedMonthKey == month.month) null
                                                        else month.month
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                            }

                            // Inline detail panel
                            AnimatedVisibility(
                                visible = selectedMonthKey != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                val month = state.months.find { it.month == selectedMonthKey }
                                if (month != null) {
                                    Column(modifier = Modifier.padding(top = 4.dp)) {
                                        HorizontalDivider(
                                            color = MaterialTheme.appColors.navyBorder,
                                            thickness = 0.5.dp,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        // Month label + reset / close
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                month.monthLabel,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.appColors.textPrimary
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (month.isCustomized) {
                                                    Text(
                                                        "Restablecer",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.appColors.expense.copy(alpha = 0.8f),
                                                        modifier = Modifier
                                                            .clickable {
                                                                viewModel.resetMonth(month.month)
                                                                selectedMonthKey = null
                                                            }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { selectedMonthKey = null },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Outlined.Close,
                                                        contentDescription = "Cerrar",
                                                        tint = MaterialTheme.appColors.textTertiary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(10.dp))

                                        // Two fields side by side
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            InlineAmountField(
                                                label = "Ahorro",
                                                value = month.savingsText,
                                                onChange = {
                                                    viewModel.onMonthSavingsChange(month.month, it)
                                                },
                                                placeholder = "Base",
                                                suffix = "€",
                                                singleLine = true,
                                                containerColor = MaterialTheme.appColors.navySurfaceLight,
                                                cursorColor = MaterialTheme.appColors.income,
                                                modifier = Modifier.weight(1f)
                                            )
                                            InlineAmountField(
                                                label = "Inversión",
                                                value = month.investmentText,
                                                onChange = {
                                                    viewModel.onMonthInvestmentChange(month.month, it)
                                                },
                                                placeholder = "Base",
                                                suffix = "€",
                                                singleLine = true,
                                                containerColor = MaterialTheme.appColors.navySurfaceLight,
                                                cursorColor = MaterialTheme.appColors.cyanAccent,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Botón guardar ──
                    item {
                        Button(
                            onClick = viewModel::save,
                            enabled = state.isDirty && !state.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.appColors.cyanAccent,
                                disabledContainerColor = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.appColors.navyDeep,
                                disabledContentColor = MaterialTheme.appColors.navyDeep.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.appColors.navyDeep,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Guardar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }
                    }

                    // ── Mensaje feedback ──
                    state.message?.let { msg ->
                        item {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (msg.startsWith("Error")) MaterialTheme.appColors.expense else MaterialTheme.appColors.income,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

// ── Hero card ─────────────────────────────────────────────────────────────────

@Composable
private fun GoalSettingsHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.appColors.navySurface, MaterialTheme.appColors.navySurfaceLight)))
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .background(
                    Brush.radialGradient(listOf(MaterialTheme.appColors.cyanGlow.copy(alpha = 0.14f), Color.Transparent)),
                    shape = CircleShape
                )
        )
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.appColors.cyanAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.GpsFixed,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.cyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "PLANIFICACIÓN FINANCIERA",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Objetivos mensuales",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Define cuánto quieres ahorrar e invertir cada mes. Establece una base anual y ajusta los meses con ingresos o gastos excepcionales.",
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

// ── Layout helpers ────────────────────────────────────────────────────────────

@Composable
private fun NavySectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.appColors.navySurface)
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        content = content
    )
}

@Composable
private fun GoalTypeHeader(
    icon: @Composable () -> Unit,
    iconBackground: Color,
    label: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.appColors.textTertiary
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textTertiary,
                lineHeight = 15.sp
            )
        }
    }
}

// ── Month chip ────────────────────────────────────────────────────────────────

@Composable
private fun MonthChip(
    label: String,
    isSelected: Boolean,
    isCustomized: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(
                when {
                    isPast -> MaterialTheme.appColors.navySurface
                    isSelected -> MaterialTheme.appColors.navySelected
                    isCustomized -> MaterialTheme.appColors.navySelected.copy(alpha = 0.6f)
                    else -> MaterialTheme.appColors.navySurface
                }
            )
            .border(
                width = if (!isPast && (isSelected || isCustomized)) 1.dp else 0.5.dp,
                color = when {
                    isPast -> MaterialTheme.appColors.navyBorder.copy(alpha = 0.4f)
                    isSelected -> MaterialTheme.appColors.cyanAccent
                    isCustomized -> MaterialTheme.appColors.cyanAccent.copy(alpha = 0.5f)
                    else -> MaterialTheme.appColors.navyBorder
                },
                shape = RoundedCornerShape(9.dp)
            )
            .then(if (!isPast) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (!isPast && (isSelected || isCustomized)) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isPast -> MaterialTheme.appColors.textTertiary.copy(alpha = 0.4f)
                    isSelected -> MaterialTheme.appColors.cyanAccent
                    isCustomized -> MaterialTheme.appColors.cyanAccent.copy(alpha = 0.7f)
                    else -> MaterialTheme.appColors.textSecondary
                }
            )
            if (isCustomized && !isPast) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            if (isSelected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.cyanAccent.copy(alpha = 0.6f),
                            CircleShape
                        )
                )
            }
        }
    }
}
