package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.Portfolio
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_name
import n3to.composeapp.generated.resources.common_save_changes
import n3to.composeapp.generated.resources.portfolio_add_asset_name_required
import n3to.composeapp.generated.resources.portfolio_edit_portfolio_title_alt
import n3to.composeapp.generated.resources.portfolio_new_portfolio_title
import n3to.composeapp.generated.resources.portfolio_portfolio_name
import n3to.composeapp.generated.resources.portfolio_save
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPortfolioBottomSheet(
    existing: Portfolio? = null,
    onSave: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEditing = existing != null
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var nameError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.appColors.navySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.navyBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding()
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text = if (isEditing) stringResource(Res.string.portfolio_edit_portfolio_title_alt)
                else stringResource(Res.string.portfolio_new_portfolio_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(stringResource(Res.string.common_name)) },
                placeholder = { Text(stringResource(Res.string.portfolio_portfolio_name)) },
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
                    focusedBorderColor = MaterialTheme.appColors.cyanAccent,
                    unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
                    cursorColor = MaterialTheme.appColors.cyanAccent,
                    focusedLabelColor = MaterialTheme.appColors.cyanAccent,
                    unfocusedLabelColor = MaterialTheme.appColors.textSecondary,
                    focusedTextColor = MaterialTheme.appColors.textPrimary,
                    unfocusedTextColor = MaterialTheme.appColors.textPrimary,
                    focusedContainerColor = MaterialTheme.appColors.navySurfaceLight,
                    unfocusedContainerColor = MaterialTheme.appColors.navySurfaceLight
                )
            )

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    onSave(name.trim())
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(11.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.appColors.cyanAccent,
                    contentColor = MaterialTheme.appColors.navyDeep
                )
            ) {
                Text(
                    text = if (isEditing) stringResource(Res.string.common_save_changes)
                    else stringResource(Res.string.portfolio_save),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

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
