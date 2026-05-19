package es.aviferdev.n3to.ui.settings.emergencyfund

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RemoveCircle
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.EmergencyFundMethod
import es.aviferdev.n3to.ui.common.help.HelpContent
import es.aviferdev.n3to.ui.common.help.HelpKeys
import es.aviferdev.n3to.ui.common.help.HelpTooltipIcon
import es.aviferdev.n3to.ui.common.navigation.TopBarApp

import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen

import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EmergencyFundSettingsScreen(
    navigateBack: () -> Unit = {},
    viewModel: EmergencyFundSettingsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(60); contentVisible = true }

    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(2000)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.appColors.navyDeep)) {
        TopBarApp(
            title = "Fondo de emergencia",
            navigateBack = navigateBack,
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Hero card ──
                    item {
                        EmergencyFundHeroCard()
                    }

                    // ── Descripción ──
                    item {
                        Text(
                            text = "Define cuántos meses de gastos quieres tener cubiertos " +
                                    "con tu saldo disponible como fondo de emergencia.",
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary,
                            lineHeight = 19.sp
                        )
                    }

                    // ── Info explicativa ──
                    item {
                        EmergencyFundInfoCard()
                    }

                    // ── Meses a cubrir ──
                    item {
                        NavySectionCard {
                            SectionLabel("Meses a cubrir")
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.monthsText,
                                onValueChange = viewModel::onMonthsChange,
                                placeholder = { Text("Ej: 6", color = MaterialTheme.appColors.textTertiary) },
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

                    // ── Método de cálculo ──
                    item {
                        NavySectionCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SectionLabel("Método de cálculo")
                                HelpTooltipIcon(
                                    title = HelpContent.texts[HelpKeys.EMERGENCY_FUND]?.title ?: "",
                                    body  = HelpContent.texts[HelpKeys.EMERGENCY_FUND]?.body  ?: ""
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MethodChip(
                                    label = "Gasto estimado",
                                    selected = state.method == EmergencyFundMethod.MANUAL,
                                    onClick = { viewModel.onMethodChange(EmergencyFundMethod.MANUAL) },
                                    modifier = Modifier.weight(1f)
                                )
                                MethodChip(
                                    label = "Media de gastos",
                                    selected = state.method == EmergencyFundMethod.AUTO,
                                    onClick = { viewModel.onMethodChange(EmergencyFundMethod.AUTO) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // ── Gasto manual (solo MANUAL) ──
                    if (state.method == EmergencyFundMethod.MANUAL) {
                        item {
                            NavySectionCard {
                                SectionLabel("Gasto mensual estimado (€)")
                                Spacer(Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = state.manualExpenseText,
                                    onValueChange = viewModel::onManualExpenseChange,
                                    placeholder = { Text("Ej: 1500", color = MaterialTheme.appColors.textTertiary) },
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
                    }

                    // ── Exclusión de categorías (solo AUTO) ──
                    if (state.method == EmergencyFundMethod.AUTO) {
                        item {
                            NavySectionCard {
                                SectionLabel("Excluir categorías")
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Selecciona las categorías que NO quieres incluir " +
                                            "en el cálculo de la media mensual.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.appColors.textTertiary,
                                    lineHeight = 16.sp
                                )

                                if (state.expenseCategories.isEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "No hay categorías de gasto disponibles. " +
                                                "Crea alguna desde Ajustes > Categorías.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.appColors.textTertiary,
                                        lineHeight = 16.sp
                                    )
                                } else {
                                    Spacer(Modifier.height(12.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(11.dp))
                                            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(11.dp))
                                            .background(MaterialTheme.appColors.navyDeep)
                                    ) {
                                        state.expenseCategories.forEachIndexed { index, category ->
                                            CategoryExclusionRow(
                                                name = category.name,
                                                isExcluded = category.id in state.excludedCategoryIds,
                                                onToggle = { viewModel.toggleCategoryExclusion(category.id) }
                                            )
                                            if (index < state.expenseCategories.lastIndex) {
                                                HorizontalDivider(
                                                    color = MaterialTheme.appColors.navyBorder,
                                                    thickness = 0.5.dp
                                                )
                                            }
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
                                containerColor = MaterialTheme.appColors.cyanAccent,
                                disabledContainerColor = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.3f),
                                contentColor = MaterialTheme.appColors.navyDeep,
                                disabledContentColor = MaterialTheme.appColors.navyDeep.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
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

                    // ── Botón Desactivar (solo si configurado) ──
                    if ((state.monthsText.toIntOrNull() ?: 0) > 0) {
                        item {
                            OutlinedButton(
                                onClick = viewModel::delete,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(ExpenseRed.copy(alpha = 0.5f))
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Desactivar fondo",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
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
                                color = if (msg.startsWith("Error")) ExpenseRed else IncomeGreen,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EmergencyFundInfoCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.appColors.cyanAccent.copy(alpha = 0.06f))
            .border(0.5.dp, MaterialTheme.appColors.cyanAccent.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.appColors.cyanAccent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "¿QUÉ ES?",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.appColors.cyanAccent
            )
        }
        Text(
            text = "Un colchón de dinero líquido para cubrir imprevistos sin endeudarte: pérdida de empleo, gastos médicos o reparaciones urgentes.",
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            lineHeight = 17.sp
        )
        HorizontalDivider(color = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.15f), thickness = 0.5.dp)
        Text(
            text = "CUÁNTO SE RECOMIENDA",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = MaterialTheme.appColors.textTertiary
        )
        Text(
            text = "• 3–6 meses con empleo estable\n• 6–12 meses si eres autónomo o tienes cargas familiares",
            fontSize = 12.sp,
            color = MaterialTheme.appColors.textSecondary,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun EmergencyFundHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.appColors.navySurface, MaterialTheme.appColors.navySurfaceLight)
                )
            )
            .border(0.5.dp, MaterialTheme.appColors.navyBorder, RoundedCornerShape(20.dp))
    ) {
        // Orb decorativo
        Box(
            modifier = Modifier
                .size(90.dp)
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
                .background(
                    Brush.radialGradient(
                        listOf(MaterialTheme.appColors.cyanGlow.copy(alpha = 0.14f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(MaterialTheme.appColors.cyanAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.appColors.cyanAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "PROTECCIÓN FINANCIERA",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.appColors.cyanAccent.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Fondo de emergencia",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Configura cuántos meses de cobertura necesitas.",
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary,
                lineHeight = 17.sp
            )
        }
    }
}

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
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.8.sp,
        color = MaterialTheme.appColors.textTertiary
    )
}

@Composable
private fun MethodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) MaterialTheme.appColors.navySelected else Color.Transparent)
            .border(
                width = 0.5.dp,
                color = if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.navyBorder,
                shape = RoundedCornerShape(11.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textSecondary,
            textAlign = TextAlign.Center
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
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExcluded) Icons.Outlined.RemoveCircle else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (isExcluded) ExpenseRed.copy(alpha = 0.8f) else MaterialTheme.appColors.cyanAccent.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = name,
            fontSize = 13.sp,
            color = MaterialTheme.appColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = if (isExcluded) "Excluida" else "Incluida",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isExcluded) ExpenseRed.copy(alpha = 0.8f) else MaterialTheme.appColors.cyanAccent.copy(alpha = 0.7f)
        )
    }
}
