package es.aviferdev.n3to.ui.common.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LockPerson
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.home_locked_no_account_subtitle
import n3to.composeapp.generated.resources.home_locked_no_account_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun LockedFeatureOverlay(
    locked: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    overlayColor: Color? = null,
    icon: ImageVector = Icons.Outlined.LockPerson,
    title: String? = null,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    val defaultTitle = stringResource(Res.string.home_locked_no_account_title)
    val defaultSubtitle = stringResource(Res.string.home_locked_no_account_subtitle)

    Box(modifier = modifier) {
        content()

        if (locked) {
            val background = overlayColor ?: MaterialTheme.appColors.navyDeep.copy(alpha = 0.80f)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(background)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitPointerEvent()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.appColors.textSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = title ?: defaultTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.appColors.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle ?: defaultSubtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.appColors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
