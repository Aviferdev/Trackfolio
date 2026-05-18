package es.aviferdev.n3to.ui.common.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.button.IconButtonApp
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_back_cd
import org.jetbrains.compose.resources.stringResource
import es.aviferdev.n3to.ui.common.separator.SpacerHorizontalApp
import androidx.compose.ui.graphics.Color
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun TopBarApp(
    title: String,
    navigateBack: (() -> Unit)? = null,
    subtitle: String? = null,
    containerColor: Color = SurfaceWhite,
    dividerColor: Color = BorderGray,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = containerColor) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigateBack != null) {
                IconButtonApp(
                    clickButton = navigateBack,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back_cd)
                )
                SpacerHorizontalApp(8.dp)
            } else {
                Spacer(Modifier.width(56.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextTertiary
                    )
                }
            }
            actions()
        }
        HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    TopBarApp(
        title = "Ir atras",
        navigateBack = {}
    )
}