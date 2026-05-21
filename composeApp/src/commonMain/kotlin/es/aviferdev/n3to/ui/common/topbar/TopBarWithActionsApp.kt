package es.aviferdev.n3to.ui.common.topbar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.button.IconButtonApp
import es.aviferdev.n3to.ui.common.separator.SpacerHorizontalApp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_back_cd
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TopBarWithActionsApp(
    title: String,
    navigateBack: (() -> Unit)? = null,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.appColors.navySurface,
    dividerColor: Color = MaterialTheme.appColors.navyBorder,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(color = containerColor) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 8.dp)
                .heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigateBack != null) {
                IconButtonApp(
                    clickButton = navigateBack,
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.common_back_cd),
                    backgroundColor = MaterialTheme.appColors.navySelected,
                    iconTint = MaterialTheme.appColors.textSecondary
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
                    color = MaterialTheme.appColors.textPrimary,
                    letterSpacing = (-0.3).sp
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.appColors.textTertiary
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
    TopBarWithActionsApp(
        title = "Ir atras",
        navigateBack = {}
    )
}