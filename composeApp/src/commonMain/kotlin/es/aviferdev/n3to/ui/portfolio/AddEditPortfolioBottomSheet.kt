package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_name
import n3to.composeapp.generated.resources.common_save_changes
import n3to.composeapp.generated.resources.portfolio_add_asset_name_required
import n3to.composeapp.generated.resources.portfolio_edit_portfolio_title_alt
import n3to.composeapp.generated.resources.portfolio_new_portfolio_title
import n3to.composeapp.generated.resources.portfolio_portfolio_desc
import n3to.composeapp.generated.resources.portfolio_portfolio_name
import n3to.composeapp.generated.resources.portfolio_save
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPortfolioBottomSheet(
    existing: Portfolio? = null,  // null = crear
    onSave: (name: String, description: String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = existing != null
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SurfaceWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .imePadding()
        ) {
            Text(
                text = if (isEditing) stringResource(Res.string.portfolio_edit_portfolio_title_alt) else stringResource(Res.string.portfolio_new_portfolio_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(stringResource(Res.string.common_name)) },
                placeholder = { Text(stringResource(Res.string.portfolio_portfolio_name)) },
                isError = nameError,
                supportingText = if (nameError) {{ Text(stringResource(Res.string.portfolio_add_asset_name_required)) }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(Res.string.portfolio_portfolio_desc)) },
                placeholder = { Text(stringResource(Res.string.portfolio_portfolio_desc)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDark,
                    unfocusedBorderColor = BorderGray
                )
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    onSave(name.trim(), description.trim().ifBlank { null })
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDark)
            ) {
                Text(
                    text = if (isEditing) stringResource(Res.string.common_save_changes) else stringResource(Res.string.portfolio_save),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
