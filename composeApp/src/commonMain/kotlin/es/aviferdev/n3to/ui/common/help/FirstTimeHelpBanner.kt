package es.aviferdev.n3to.ui.common.help

import androidx.compose.material3.MaterialTheme
import es.aviferdev.n3to.ui.theme.appColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import es.aviferdev.n3to.ui.common.AlertBanner

import org.koin.compose.koinInject

@Composable
fun FirstTimeHelpBanner(
    key: String,
    icon: String,
    label: String,
    color: Color = MaterialTheme.appColors.cyanAccent,
    modifier: Modifier = Modifier,
    helpPreferences: HelpPreferences = koinInject()
) {
    var dismissed by remember { mutableStateOf(helpPreferences.isDismissed(key)) }

    AnimatedVisibility(
        visible = !dismissed,
        exit = fadeOut() + shrinkVertically()
    ) {
        AlertBanner(
            icon = icon,
            label = label,
            color = color,
            onDismiss = {
                helpPreferences.dismiss(key)
                dismissed = true
            },
            modifier = modifier
        )
    }
}
