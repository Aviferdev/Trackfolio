package es.aviferdev.n3to.ui.portfolio.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.portfolio_settings_reminder_desc
import n3to.composeapp.generated.resources.portfolio_settings_reminder_off
import n3to.composeapp.generated.resources.portfolio_settings_reminder_section
import n3to.composeapp.generated.resources.portfolio_settings_reminder_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderIntervalSection(
    currentInterval: Int,
    onIntervalChange: (Int) -> Unit
) {
    Text(
        stringResource(Res.string.portfolio_settings_reminder_section),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.appColors.textSecondary
    )
    SettingsGroupCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                stringResource(Res.string.portfolio_settings_reminder_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(Res.string.portfolio_settings_reminder_desc),
                fontSize = 12.sp,
                color = MaterialTheme.appColors.textSecondary
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val offLabel = stringResource(Res.string.portfolio_settings_reminder_off)
                listOf(0, 7, 14, 30).forEach { days ->
                    val isSelected = currentInterval == days
                    OutlinedButton(
                        onClick = { onIntervalChange(days) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.appColors.primary else Color.Transparent,
                            contentColor = if (isSelected) Color.White else MaterialTheme.appColors.textPrimary
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (days == 0) offLabel else "${days}d",
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
