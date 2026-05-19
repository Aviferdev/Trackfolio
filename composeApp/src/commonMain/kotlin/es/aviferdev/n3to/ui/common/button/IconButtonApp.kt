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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.TextTertiary
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun IconButtonApp(
    clickButton: () -> Unit,
    icon: ImageVector,
    contentDescription: String = "none",
    backgroundColor: Color = SurfaceElevated,
    iconTint: Color = TextTertiary
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
