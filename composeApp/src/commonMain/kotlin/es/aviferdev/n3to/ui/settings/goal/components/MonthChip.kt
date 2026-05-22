package es.aviferdev.n3to.ui.settings.goal.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun MonthChip(
    label: String,
    isSelected: Boolean,
    isCustomized: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(
                when {
                    isPast -> MaterialTheme.appColors.navySurface
                    isSelected -> MaterialTheme.appColors.navySelected
                    isCustomized -> MaterialTheme.appColors.navySelected.copy(alpha = 0.6f)
                    else -> MaterialTheme.appColors.navySurface
                }
            )
            .border(
                width = if (!isPast && (isSelected || isCustomized)) 1.dp else 0.5.dp,
                color = when {
                    isPast -> MaterialTheme.appColors.navyBorder.copy(alpha = 0.4f)
                    isSelected -> MaterialTheme.appColors.cyanAccent
                    isCustomized -> MaterialTheme.appColors.cyanAccent.copy(alpha = 0.5f)
                    else -> MaterialTheme.appColors.navyBorder
                },
                shape = RoundedCornerShape(9.dp)
            )
            .then(if (!isPast) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (!isPast && (isSelected || isCustomized)) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    isPast -> MaterialTheme.appColors.textTertiary.copy(alpha = 0.4f)
                    isSelected -> MaterialTheme.appColors.cyanAccent
                    isCustomized -> MaterialTheme.appColors.cyanAccent.copy(alpha = 0.7f)
                    else -> MaterialTheme.appColors.textSecondary
                }
            )
            if (isCustomized && !isPast) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            if (isSelected) MaterialTheme.appColors.cyanAccent
                            else MaterialTheme.appColors.cyanAccent.copy(alpha = 0.6f),
                            CircleShape
                        )
                )
            }
        }
    }
}
