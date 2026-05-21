package es.aviferdev.n3to.ui.common.button

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors

@Composable
fun FloatingButtonAdd(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean = true
) {
    FloatingActionButton(
        onClick = { if (enabled) onClick() },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.appColors.navySurface,
        contentColor = if (enabled) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textTertiary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp
        )
    ) {
        Text(
            text = "+",
            fontSize = 26.sp,
            fontWeight = FontWeight.Light,
            color = if (enabled) MaterialTheme.appColors.cyanAccent else MaterialTheme.appColors.textTertiary
        )
    }
}