package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.platform.nowMillis
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_name
import n3to.composeapp.generated.resources.common_save_changes
import n3to.composeapp.generated.resources.portfolio_add_asset_name_required
import n3to.composeapp.generated.resources.portfolio_add_asset_platforms_hint
import n3to.composeapp.generated.resources.portfolio_add_tx_notes_label
import n3to.composeapp.generated.resources.portfolio_platform_create_title
import n3to.composeapp.generated.resources.portfolio_platform_edit_title
import n3to.composeapp.generated.resources.portfolio_platform_icon_label
import n3to.composeapp.generated.resources.portfolio_platform_name_label
import n3to.composeapp.generated.resources.portfolio_platform_notes_label
import n3to.composeapp.generated.resources.portfolio_platform_save
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Sheet para crear o editar una plataforma (broker, exchange, banco).
 *
 * Reutilizable desde Ajustes (CRUD pleno) y desde el flujo del Portfolio
 * cuando el usuario intenta registrar un movimiento sin tener todavía
 * ninguna plataforma creada (atajo inline — decisión 8.C).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlatformSheet(
    initial: Platform?,
    onSave: (name: String, icon: String, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = initial != null

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var icon by remember { mutableStateOf(initial?.icon ?: "🏦") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    // Iconos sugeridos para plataformas. El usuario puede pegar cualquier emoji.
    val suggestedIcons = listOf(
        "🏦", "💼", "💱", "📈", "💳", "🏢",
        "₿", "🪙", "💰", "🌐", "📊", "🛡️",
        "🎯", "🔐", "📱", "💻", "🚀", "⚖️"
    ).distinct()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (isEditing) stringResource(Res.string.portfolio_platform_edit_title) else stringResource(
                    Res.string.portfolio_platform_create_title
                ),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.portfolio_add_asset_platforms_hint),
                fontSize = 11.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(20.dp))

            // ── Selector de icono ────────────────────────────────────────────
            Text(
                stringResource(Res.string.portfolio_platform_icon_label),
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                suggestedIcons.forEach { ic ->
                    val isSel = ic == icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) MaterialTheme.appColors.surfaceElevated else MaterialTheme.appColors.surface)
                            .border(
                                width = if (isSel) 1.5.dp else 0.5.dp,
                                color = if (isSel) MaterialTheme.appColors.primary else MaterialTheme.appColors.border,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { icon = ic },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ic.toMaterialIcon(),
                            contentDescription = null,
                            tint = if (isSel) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Nombre ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(stringResource(Res.string.common_name)) },
                placeholder = { Text(stringResource(Res.string.portfolio_platform_name_label)) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(Res.string.portfolio_add_asset_name_required)) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Notas ─────────────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= 200) notes = it },
                label = { Text(stringResource(Res.string.portfolio_add_tx_notes_label)) },
                placeholder = { Text(stringResource(Res.string.portfolio_platform_notes_label)) },
                supportingText = { Text("${notes.length}/200") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.appColors.primary,
                    unfocusedBorderColor = MaterialTheme.appColors.border
                )
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true; return@Button
                    }
                    val notesValue = notes.trim().ifBlank { null }
                    onSave(name.trim(), icon, notesValue)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.primary)
            ) {
                Text(
                    text = if (isEditing) stringResource(Res.string.common_save_changes) else stringResource(
                        Res.string.portfolio_platform_save
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(Res.string.common_cancel),
                    fontSize = 14.sp,
                    color = MaterialTheme.appColors.textSecondary
                )
            }
        }
    }
}

@Preview
@Composable
private fun AddEditPlatformSheetCreatePreview() {
    N3toTheme {
        AddEditPlatformSheet(
            initial = null,
            onSave = { _, _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddEditPlatformSheetEditPreview() {
    N3toTheme {
        AddEditPlatformSheet(
            initial = Platform(
                id = "1",
                name = "Interactive Brokers",
                icon = "📊", // toMaterialIcon() → BarChart
                sortOrder = 0,
                archived = false,
                createdAt = nowMillis(),
                notes = "Cuenta principal para acciones USA"
            ),
            onSave = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
