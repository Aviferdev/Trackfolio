package es.aviferdev.n3to.ui.common.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_loading
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ButtonLarge(
    text: StringResource,
    textLoading: StringResource = Res.string.common_loading,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enable: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enable,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.appColors.cyanAccent,
            contentColor = MaterialTheme.appColors.navyDeep,
            disabledContainerColor = MaterialTheme.appColors.navySurfaceLight,
            disabledContentColor = MaterialTheme.appColors.textTertiary
        )
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.appColors.navyDeep,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
        } else {
            icon?.let {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
            }
        }

        Text(
            text = stringResource(
                resource = if (loading) {
                    text
                } else {
                    textLoading
                }
            ),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}