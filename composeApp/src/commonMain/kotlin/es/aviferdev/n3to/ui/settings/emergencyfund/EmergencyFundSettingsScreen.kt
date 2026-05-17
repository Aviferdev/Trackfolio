package es.aviferdev.n3to.ui.settings.emergencyfund

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.BorderGray2
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun EmergencyFundSettingsScreen(
    navigateBack: () -> Unit = {},
    viewModel: EmergencyFundSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    // Snackbar-like message auto-dismiss
    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(2000)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundGray)) {
        TopBarApp(title = "Fondo de emergencia", navigateBack = navigateBack)

        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 10 })
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryDark)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // ── Descripción ──
                    item {
                        Text(
                            text = "Define cuántos meses de gastos quieres tener cubiertos " +
                                    "con tu saldo disponible como fondo de emergencia.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }

                    // ── Meses ──
                    item {
                        Column {
                            Text(
                                text = "Meses a cubrir",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = state.monthsText,
                                onValueChange = viewModel::onMonthsChange,
                                placeholder = { Text("Ej: 6", color = TextTertiary) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryDark,
                                    unfocusedBorderColor = BorderGray2,
                                    cursorColor = PrimaryDark,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    focusedContainerColor = SurfaceElevated,
                                    unfocusedContainerColor = SurfaceElevated
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // ── Método ──
                    item {
                        Column {
                            Text(
                                text = "Método de cálculo",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MethodChip(
                                    label = "Gasto estimado",
                                    selected = state.method == EmergencyFundMethod.MANUAL,
                                    onClick = { viewModel.onMethodChange(EmergencyFundMethod.MANUAL) }
                                )
                                MethodChip(
                                    label = "Media de gastos",
                                    selected = state.method == EmergencyFundMethod.AUTO,
                                    onClick = { viewModel.onMethodChange(EmergencyFundMethod.AUTO) }
                                )
                            }
                        }
                    }

                    // ── Gasto manual (solo MANUAL) ──
                    if (state.method == EmergencyFundMethod.MANUAL) {
                        item {
                            Column {
                                Text(
                                    text = "Gasto mensual estimado (€)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = state.manualExpenseText,
                                    onValueChange = viewModel::onManualExpenseChange,
                                    placeholder = { Text("Ej: 1500", color = TextTertiary) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = PrimaryDark,
                                        unfocusedBorderColor = BorderGray2,
                                        cursorColor = PrimaryDark,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedContainerColor = SurfaceElevated,
                                        unfocusedContainerColor = SurfaceElevated
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // ── Exclusión de categorías (solo AUTO) ──
                    if (state.method == EmergencyFundMethod.AUTO) {
                        item {
                            Text(
                                text = "Excluir categorías",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Selecciona las categorías de gasto que NO quieres incluir " +
                                        "en el cálculo de la media mensual.",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                lineHeight = 16.sp
                            )
                        }

                        if (state.expenseCategories.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay categorías de gasto disponibles. " +
                                            "Crea alguna desde Ajustes > Categorías de gastos.",
                                    fontSize = 12.sp,
                                    color = TextTertiary
                                )
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(SurfaceWhite)
                                ) {
                                    state.expenseCategories.forEachIndexed { index, category ->
                                        CategoryExclusionRow(
                                            name = category.name,
                                            isExcluded = category.id in state.excludedCategoryIds,
                                            onToggle = { viewModel.toggleCategoryExclusion(category.id) }
                                        )
                                        if (index < state.expenseCategories.lastIndex) {
                                            HorizontalDivider(
                                                color = BorderGray2,
                                                thickness = 0.5.dp,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Botón Guardar ──
                    item {
                        Button(
                            onClick = viewModel::save,
                            enabled = state.isValid && !state.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryDark,
                                disabledContainerColor = PrimaryDark.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    color = TextPrimary,
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
                                Text("Guardar", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }

                    // ── Botón Eliminar (solo si configurado) ──
                    if ((state.monthsText.toIntOrNull() ?: 0) > 0) {
                        item {
                            OutlinedButton(
                                onClick = viewModel::delete,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(ExpenseRed.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Desactivar fondo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }
                        }
                    }

                    // ── Mensaje ──
                    state.message?.let { msg ->
                        item {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = if (msg.startsWith("Error")) ExpenseRed else IncomeGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Espacio inferior
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MethodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) PrimaryAlpha else SurfaceWhite)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) PrimaryDark else TextSecondary
        )
    }
}

@Composable
private fun CategoryExclusionRow(
    name: String,
    isExcluded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox custom
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isExcluded) ExpenseRed.copy(alpha = 0.2f)
                    else PrimaryDark.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isExcluded) {
                Text(
                    text = "✕",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        Text(
            text = name,
            fontSize = 13.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (isExcluded) "Excluido" else "Incluido",
            fontSize = 11.sp,
            color = if (isExcluded) ExpenseRed else IncomeGreen
        )
    }
}
