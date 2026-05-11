package es.aviferdev.trackfolio.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import es.aviferdev.trackfolio.ui.theme.*
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIssuerSheet(
    initial: Issuer?,
    type: IssuerType,
    onSave: (name: String, icon: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    val defaultIcon = when (type) {
        IssuerType.EMPLOYER           -> "🏢"
        IssuerType.BANK               -> "🏦"
        IssuerType.BOND_ISSUER        -> "📜"
        IssuerType.DIVIDEND_SOURCE    -> "📈"
        IssuerType.PROMOTION_PLATFORM -> "🎁"
        IssuerType.EXEMPT_SOURCE      -> "📋"
    }
    var icon by remember { mutableStateOf(initial?.icon ?: defaultIcon) }
    val isEditing = initial != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFBDBDBD))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (isEditing) "Editar emisor" else "Nuevo emisor",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nombre del emisor", color = TextTertiary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = PrimaryDark
                ),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), icon)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryDark,
                    disabledContainerColor = PrimaryDark.copy(alpha = 0.3f)
                )
            ) {
                Text("Guardar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}

@Preview
@Composable
private fun AddEditIssuerSheetCreatePreview() {
    TrackfolioTheme {
        AddEditIssuerSheet(
            initial = null,
            type = IssuerType.BANK,
            onSave = { _, _ -> },
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun AddEditIssuerSheetEditPreview() {
    TrackfolioTheme {
        AddEditIssuerSheet(
            initial = Issuer(
                id = "1",
                accountId = "acc1",
                name = "BBVA",
                icon = "🏦",
                type = IssuerType.BANK,
                createdAt = Clock.System.now().toEpochMilliseconds()
            ),
            type = IssuerType.BANK,
            onSave = { _, _ -> },
            onDismiss = {}
        )
    }
}
