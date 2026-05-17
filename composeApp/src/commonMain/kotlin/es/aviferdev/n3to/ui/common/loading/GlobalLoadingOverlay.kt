package es.aviferdev.n3to.ui.common.loading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.TextSecondary

/**
 * Overlay de carga global que bloquea toda interacción con la pantalla.
 *
 * Deja visible semi-transparentemente el contenido subyacente,
 * consume todos los eventos táctiles para impedir la interacción.
 *
 * @param isLoading  Si es true muestra el overlay con animación fade.
 * @param message    Texto opcional bajo el spinner (ej: "Cargando portfolio…").
 */
@Composable
fun GlobalLoadingOverlay(
    isLoading: Boolean,
    message: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible  = isLoading,
        enter    = fadeIn(animationSpec = tween(250)),
        exit     = fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyDeep.copy(alpha = 0.78f))
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
                CircularProgressIndicator(
                    modifier    = Modifier.size(40.dp),
                    color       = CyanAccent,
                    strokeWidth = 3.dp
                )
                if (message != null) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text       = message,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = TextSecondary
                    )
                }
            }
        }
    }
}
