package es.aviferdev.n3to.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.NavyDeep
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Marca de N3to: icono de la app dentro de un contenedor redondeado.
 *
 * Representa el icono vectorial de la aplicación: un rectángulo navy redondeado
 * con el logo "N3to" y su sparkline característico.
 *
 * @param containerSize Tamaño del contenedor exterior (default 96.dp).
 * @param iconSize Tamaño del icono interior (default 64.dp).
 * @param modifier Modifier adicional.
 */
@Composable
fun N3toMark(
    containerSize: Dp = 96.dp,
    iconSize: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .drawBehind {
                drawRoundRect(
                    color = NavySurface,
                    cornerRadius = CornerRadius(
                        x = size.width * 0.22f,
                        y = size.height * 0.22f
                    ),
                    size = size
                )
                drawRoundRect(
                    color = NavyBorder,
                    cornerRadius = CornerRadius(
                        x = size.width * 0.22f,
                        y = size.height * 0.22f
                    ),
                    size = size,
                    style = Stroke(width = 1.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        IconOutline(
            size = iconSize
        )
    }
}

/**
 * Capa interior del icono: muestra el PNG de la app (N3to + sparkline)
 * recortado en un rectángulo redondeado igual que el fondo.
 */
@Composable
private fun IconOutline(
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size * 0.22f)

    Image(
        painter = painterResource(Res.drawable.app_icon),
        contentDescription = "N3to",
        modifier = modifier
            .size(size)
            .clip(shape)
    )
}

@Preview
@Composable
private fun N3toMarkPreview() {
    N3toMark(containerSize = 128.dp, iconSize = 96.dp)
}
