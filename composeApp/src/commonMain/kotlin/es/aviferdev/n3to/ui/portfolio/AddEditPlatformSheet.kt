package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.*
import kotlinx.datetime.Clock
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

    var name      by remember { mutableStateOf(initial?.name ?: "") }
    var icon      by remember { mutableStateOf(initial?.icon ?: "🏦") }
    var notes     by remember { mutableStateOf(initial?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    // Iconos sugeridos para plataformas. El usuario puede pegar cualquier emoji.
    val suggestedIcons = listOf(
        "🏦", "💼", "💱", "📈", "💳", "🏢",
        "₿", "🪙", "💰", "🌐", "📊", "🛡️",
        "🎯", "🔐", "📱", "💻", "🚀", "⚖️"
    ).distinct()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = SurfaceWhite,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderGray)
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
                text       = if (isEditing) "Editar plataforma" else "Nueva plataforma",
                fontSize   = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color      = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text     = "Asocia los movimientos al broker, exchange o banco donde se ejecutaron.",
                fontSize = 11.sp,
                color    = TextSecondary
            )
            Spacer(Modifier.height(20.dp))

            // ── Selector de icono ────────────────────────────────────────────
            Text("Icono", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier              = Modifier
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
                            .background(if (isSel) SurfaceElevated else SurfaceWhite)
                            .border(
                                width = if (isSel) 1.5.dp else 0.5.dp,
                                color = if (isSel) PrimaryDark else BorderGray,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { icon = ic },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = ic.toMaterialIcon(),
                            contentDescription = null,
                            tint = if (isSel) PrimaryDark else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // ── Nombre ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it; nameError = false },
                label         = { Text("Nombre") },
                placeholder   = { Text("Ej. Binance, Trading212, MyInvestor") },
                isError       = nameError,
                supportingText = if (nameError) {{ Text("El nombre es obligatorio") }} else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Notas ─────────────────────────────────────────────────────
            OutlinedTextField(
                value         = notes,
                onValueChange = { if (it.length <= 200) notes = it },
                label         = { Text("Notas (opcional)") },
                placeholder   = { Text("Ej: Guardado en caja fuerte") },
                supportingText = { Text("${notes.length}/200") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction      = ImeAction.Done
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    val notesValue = notes.trim().ifBlank { null }
                    onSave(name.trim(), icon, notesValue)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(
                    text       = if (isEditing) "Guardar cambios" else "Crear plataforma",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar", fontSize = 14.sp, color = TextSecondary)
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
                createdAt = Clock.System.now().toEpochMilliseconds(),
                notes = "Cuenta principal para acciones USA"
            ),
            onSave = { _, _, _ -> },
            onDismiss = {}
        )
    }
}
