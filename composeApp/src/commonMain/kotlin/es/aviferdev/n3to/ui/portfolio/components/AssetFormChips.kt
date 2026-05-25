package es.aviferdev.n3to.ui.portfolio.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryAlpha
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun CategoryChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg =
        if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.surfaceElevated
    val border = if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border
    val text =
        if (isSelected) MaterialTheme.appColors.navyDeep else MaterialTheme.appColors.textPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
internal fun PlatformToggleChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg =
        if (isSelected) MaterialTheme.appColors.primary.copy(alpha = 0.12f) else MaterialTheme.appColors.surfaceElevated
    val border = if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border
    val text =
        if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.textPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(Modifier.width(4.dp))
            Text(
                "✓",
                fontSize = 12.sp,
                color = MaterialTheme.appColors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun SectorToggleChip(
    icon: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg =
        if (isSelected) MaterialTheme.appColors.primary.copy(alpha = 0.12f) else MaterialTheme.appColors.surfaceElevated
    val border = if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border
    val text =
        if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.textPrimary

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(if (isSelected) 1.5.dp else 0.5.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 14.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = text,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (isSelected) {
            Spacer(Modifier.width(4.dp))
            Text(
                "✓",
                fontSize = 12.sp,
                color = MaterialTheme.appColors.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
internal fun PortfolioChipSimple(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) PrimaryAlpha else Color.Transparent
    val border = if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.border
    val textColor =
        if (isSelected) MaterialTheme.appColors.primary else MaterialTheme.appColors.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

internal fun formatFullDate(epochMillis: Long): String {
    val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
    val instant = Instant.fromEpochMilliseconds(epochMillis)
    val ld: LocalDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} de ${months[ld.monthNumber - 1]} de ${ld.year}"
}
