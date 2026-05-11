package es.aviferdev.trackfolio.ui.common.button

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
import es.aviferdev.trackfolio.ui.theme.SurfaceElevated
import es.aviferdev.trackfolio.ui.theme.TextTertiary
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun IconButtomApp(
    clickButton: () -> Unit,
    icon: ImageVector,
    contentDescription: String = "none"
) {
    IconButton(
        onClick = clickButton,
        modifier = Modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = TextTertiary,
            modifier = Modifier.size(32.dp)
                .padding(4.dp)
        )
    }
}

@Preview
@Composable
private fun CustomIconButtonPreview() {
    IconButtomApp(
        clickButton = {},
        Icons.AutoMirrored.Filled.ArrowBack
    )
}