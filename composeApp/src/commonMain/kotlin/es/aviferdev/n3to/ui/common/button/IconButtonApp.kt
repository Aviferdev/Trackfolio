package es.aviferdev.n3to.ui.common.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.ui.theme.appColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IconButtonApp(
    clickButton: () -> Unit,
    icon: ImageVector,
    contentDescription: String = "none",
    backgroundColor: Color = MaterialTheme.appColors.surfaceElevated,
    iconTint: Color = MaterialTheme.appColors.textTertiary
) {
    IconButton(
        onClick = clickButton,
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
                .padding(4.dp)
        )
    }
}

@Preview
@Composable
private fun CustomIconButtonPreview() {
    IconButtonApp(
        clickButton = {},
        Icons.AutoMirrored.Filled.ArrowBack
    )
}
