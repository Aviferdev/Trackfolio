package es.aviferdev.n3to.ui.settings.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.input.InlineAmountField
import es.aviferdev.n3to.ui.common.navigation.TimeStepperHeader
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GoalSettingsScreen(
    navigateBack: () -> Unit = {},
    viewModel: GoalSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(
            title = "Objetivos mensuales",
            navigateBack = {
                if (state.isDirty) viewModel.save()
                navigateBack()
            }
        )

        TimeStepperHeader(
            currentValue = state.year,
            canGoBack = state.canGoPrevious,
            onPrevious = { viewModel.previousYear() },
            onNext = {}
        )

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 40.dp),
                color = PrimaryDark
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Objetivo base mensual
                item {
                    Text(
                        text = "Objetivo base mensual",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Se aplicará a todos los meses. Puedes personalizar meses concretos más abajo.",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InlineAmountField(
                                label = "Ahorro mensual",
                                value = state.baseSavingsText,
                                onChange = { viewModel.onBaseSavingsChange(it) },
                                placeholder = "0,00",
                                suffix = "€",
                                modifier = Modifier.weight(1f)
                            )
                            InlineAmountField(
                                label = "Inversión mensual",
                                value = state.baseInvestmentText,
                                onChange = { viewModel.onBaseInvestmentChange(it) },
                                placeholder = "0,00",
                                suffix = "€",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Sección desplegable: Personalizar meses
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column {
                            // Botón para expandir/colapsar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleMonthDetails() }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Personalizar por meses",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    if (state.customizedCount > 0) {
                                        Text(
                                            text = "${state.customizedCount} mes${if (state.customizedCount != 1) "es" else ""} personalizado${if (state.customizedCount != 1) "s" else ""}",
                                            fontSize = 11.sp,
                                            color = PrimaryDark
                                        )
                                    } else {
                                        Text(
                                            text = "Toca para personalizar meses concretos",
                                            fontSize = 11.sp,
                                            color = TextTertiary
                                        )
                                    }
                                }
                                Icon(
                                    if (state.showMonthDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Lista de meses (animada)
                            AnimatedVisibility(
                                visible = state.showMonthDetails,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column {
                                    state.months.forEach { month ->
                                        MonthCustomizationRow(
                                            month = month,
                                            onSavingsChange = {
                                                viewModel.onMonthSavingsChange(
                                                    month.month,
                                                    it
                                                )
                                            },
                                            onInvestmentChange = {
                                                viewModel.onMonthInvestmentChange(
                                                    month.month,
                                                    it
                                                )
                                            },
                                            onReset = { viewModel.resetMonth(month.month) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Botón guardar
                item {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryDark,
                            contentColor = Color.White,
                            disabledContainerColor = PrimaryDark.copy(alpha = 0.4f)
                        ),
                        enabled = state.isDirty && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Guardar objetivos",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Mensaje de feedback
                state.message?.let { msg ->
                    item {
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            color = if (msg.startsWith("Error")) {
                                es.aviferdev.n3to.ui.theme.ExpenseRed
                            } else {
                                IncomeGreen
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun MonthCustomizationRow(
    month: MonthGoalUi,
    onSavingsChange: (String) -> Unit,
    onInvestmentChange: (String) -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nombre del mes
        Text(
            text = month.monthLabel,
            fontSize = 12.sp,
            fontWeight = if (month.isCustomized) FontWeight.SemiBold else FontWeight.Normal,
            color = if (month.isCustomized) TextPrimary else TextSecondary,
            modifier = Modifier.width(65.dp)
        )

        // Campos
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InlineAmountField(
                label = if (month.isCustomized) "Ahorro" else "",
                value = month.savingsText,
                onChange = onSavingsChange,
                placeholder = "Base",
                suffix = "€",
                modifier = Modifier.weight(1f)
            )
            InlineAmountField(
                label = if (month.isCustomized) "Inversión" else "",
                value = month.investmentText,
                onChange = onInvestmentChange,
                placeholder = "Base",
                suffix = "€",
                modifier = Modifier.weight(1f)
            )
        }

        // Botón reset (solo si está personalizado)
        if (month.isCustomized) {
            IconButton(
                onClick = onReset,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = "Restablecer",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
