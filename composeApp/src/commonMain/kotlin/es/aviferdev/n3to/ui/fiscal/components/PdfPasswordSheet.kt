package es.aviferdev.n3to.ui.fiscal.components

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.*
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.fiscal_cancel
import n3to.composeapp.generated.resources.fiscal_confirm_password_label
import n3to.composeapp.generated.resources.fiscal_generate_with_password
import n3to.composeapp.generated.resources.fiscal_generate_without_password
import n3to.composeapp.generated.resources.fiscal_password_label
import n3to.composeapp.generated.resources.fiscal_password_min_length
import n3to.composeapp.generated.resources.fiscal_password_mismatch
import n3to.composeapp.generated.resources.fiscal_password_optional
import n3to.composeapp.generated.resources.fiscal_pdf_password_desc
import n3to.composeapp.generated.resources.fiscal_pdf_password_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PdfPasswordSheet(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error           by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.appColors.navySurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.appColors.navyBorder)
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
            Text(stringResource(Res.string.fiscal_pdf_password_title), fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.appColors.textPrimary)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(Res.string.fiscal_pdf_password_desc), fontSize = 12.sp, color = MaterialTheme.appColors.textTertiary)
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text(stringResource(Res.string.fiscal_password_label)) },
                placeholder = { Text(stringResource(Res.string.fiscal_password_optional)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.appColors.textTertiary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.appColors.cyanAccent,
                    unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
                    focusedLabelColor    = MaterialTheme.appColors.cyanAccent,
                    unfocusedLabelColor  = MaterialTheme.appColors.textTertiary,
                    focusedTextColor     = MaterialTheme.appColors.textPrimary,
                    unfocusedTextColor   = MaterialTheme.appColors.textPrimary,
                    cursorColor          = MaterialTheme.appColors.cyanAccent
                )
            )

            if (password.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text(stringResource(Res.string.fiscal_confirm_password_label)) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.appColors.cyanAccent,
                        unfocusedBorderColor = MaterialTheme.appColors.navyBorder,
                        focusedLabelColor    = MaterialTheme.appColors.cyanAccent,
                        unfocusedLabelColor  = MaterialTheme.appColors.textTertiary,
                        focusedTextColor     = MaterialTheme.appColors.textPrimary,
                        unfocusedTextColor   = MaterialTheme.appColors.textPrimary,
                        cursorColor          = MaterialTheme.appColors.cyanAccent
                    )
                )
            }

            error?.let { Text(it, fontSize = 11.sp, color = ExpenseRed, modifier = Modifier.padding(top = 4.dp)) }
            Spacer(Modifier.height(20.dp))

            val passwordMismatchText  = stringResource(Res.string.fiscal_password_mismatch)
            val passwordMinLengthText = stringResource(Res.string.fiscal_password_min_length)
            Button(
                onClick = {
                    if (password.isNotEmpty() && password != confirmPassword) { error = passwordMismatchText; return@Button }
                    if (password.isNotEmpty() && password.length < 4) { error = passwordMinLengthText; return@Button }
                    onConfirm(password)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.appColors.cyanAccent, contentColor = MaterialTheme.appColors.navyDeep)
            ) {
                Text(
                    if (password.isEmpty())
                        stringResource(Res.string.fiscal_generate_without_password)
                    else
                        stringResource(Res.string.fiscal_generate_with_password),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.fiscal_cancel), fontSize = 13.sp, color = MaterialTheme.appColors.textTertiary)
            }
        }
    }
}
