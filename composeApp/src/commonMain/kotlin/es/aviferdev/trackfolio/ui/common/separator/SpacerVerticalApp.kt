package es.aviferdev.trackfolio.ui.common.separator

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun SpacerVerticalApp(
    space: Dp,
) {
    Spacer(
        modifier = Modifier.height(space)
    )
}