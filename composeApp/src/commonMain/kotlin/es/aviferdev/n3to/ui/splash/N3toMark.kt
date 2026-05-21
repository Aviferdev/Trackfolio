package es.aviferdev.n3to.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun N3toMark(
    containerSize: Dp = 148.dp,
    iconSize: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(containerSize * 0.22f)
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(shape)
            .background(MaterialTheme.appColors.navySurface),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon),
            contentDescription = "N3to",
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview
@Composable
private fun N3toMarkPreview() {
    N3toMark()
}
