package es.aviferdev.n3to.ui.common.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.toMaterialIcon
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun EmptyStateView(
    icon: Any,
    title: String,
    subtitle: String,
    iconTint: Color = MaterialTheme.appColors.cyanAccent,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon is String) {
                Icon(
                    imageVector = icon.toMaterialIcon(),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = iconTint
                )
            } else if (icon is ImageVector) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = iconTint
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.appColors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.appColors.textTertiary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.appColors.cyanAccent,
                        contentColor = MaterialTheme.appColors.navyDeep
                    )
                ) {
                    Text(
                        actionLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}