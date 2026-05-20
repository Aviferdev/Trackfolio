package es.aviferdev.n3to.ui.common.component

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import es.aviferdev.n3to.ui.theme.N3toTheme
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_action_cd
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Botón cuadrado con fondo elevado para la cabecera de las pantallas.
 * Puede mostrar un icono o un texto corto.
 */
@Composable
fun IconActionButton(
    onClick: () -> Unit,
    icon: ImageVector? = null,
    label: String? = null,
    iconTint: Color = MaterialTheme.appColors.textTertiary,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.appColors.navySurface)
            .border(1.dp, MaterialTheme.appColors.navyBorder, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label ?: stringResource(Res.string.common_action_cd),
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        } else if (label != null) {
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.appColors.textPrimary
            )
        }
    }
}

@Preview
@Composable
private fun IconActionButtonPreview() {
    N3toTheme {
        IconActionButton(
            onClick = {},
            icon = Icons.Default.Settings,
            label = "Ajustes"
        )
    }
}
