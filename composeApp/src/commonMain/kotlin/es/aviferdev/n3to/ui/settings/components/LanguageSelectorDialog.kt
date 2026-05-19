package es.aviferdev.n3to.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.LanguageOption
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.language_beta
import n3to.composeapp.generated.resources.language_chinese
import n3to.composeapp.generated.resources.language_english
import n3to.composeapp.generated.resources.language_french
import n3to.composeapp.generated.resources.language_german
import n3to.composeapp.generated.resources.language_italian
import n3to.composeapp.generated.resources.language_japanese
import n3to.composeapp.generated.resources.language_korean
import n3to.composeapp.generated.resources.language_portuguese
import n3to.composeapp.generated.resources.language_russian
import n3to.composeapp.generated.resources.language_select_title
import n3to.composeapp.generated.resources.language_spanish
import n3to.composeapp.generated.resources.language_system
import org.jetbrains.compose.resources.stringResource

/**
 * Diálogo para seleccionar un idioma.
 *
 * @param currentLanguage Código ISO del idioma actualmente activo.
 * @param isSystemDefault Si el usuario usa el idioma del sistema.
 * @param onLanguageSelected Recibe el código del idioma seleccionado ("" = sistema).
 * @param onDismiss Cierra el diálogo sin cambios.
 */
@Composable
fun LanguageSelectorDialog(
    currentLanguage: String,
    isSystemDefault: Boolean,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.appColors.navySurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                stringResource(Res.string.language_select_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LanguageOption.ALL.forEach { option ->
                    // Cuando el usuario usa el idioma del sistema, solo se marca
                    // la opción "Idioma del sistema". Las opciones específicas
                    // solo se marcan si el usuario las ha seleccionado explícitamente.
                    val isActive = when {
                        option.code.isEmpty() -> isSystemDefault
                        else -> !isSystemDefault && currentLanguage == option.code
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isActive) MaterialTheme.appColors.cyanAccent.copy(alpha = 0.10f)
                                else MaterialTheme.appColors.navySurface
                            )
                            .clickable { onLanguageSelected(option.code) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.flag,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = stringResource(
                                when (option.displayNameKey) {
                                    "language_system" -> Res.string.language_system
                                    "language_spanish" -> Res.string.language_spanish
                                    "language_english" -> Res.string.language_english
                                    "language_french" -> Res.string.language_french
                                    "language_german" -> Res.string.language_german
                                    "language_chinese" -> Res.string.language_chinese
                                    "language_russian" -> Res.string.language_russian
                                    "language_japanese" -> Res.string.language_japanese
                                    "language_portuguese" -> Res.string.language_portuguese
                                    "language_italian" -> Res.string.language_italian
                                    "language_korean" -> Res.string.language_korean
                                    else -> Res.string.language_spanish
                                }
                            ),
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.appColors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if (option.isBeta) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.appColors.cyanAccent.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.language_beta),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.appColors.cyanAccent
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        if (isActive) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.appColors.cyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.common_cancel),
                    color = MaterialTheme.appColors.cyanAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
