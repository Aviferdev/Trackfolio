package es.aviferdev.n3to.ui.common.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Barra de búsqueda reutilizable con icono de lupa y botón de limpiar.
 *
 * @param query Texto de búsqueda actual.
 * @param onChange Callback cuando el texto cambia.
 * @param placeholder Texto de placeholder.
 * @param modifier Modifier para personalizar.
 */
@Composable
fun SearchBar(
    query: String,
    onChange: (String) -> Unit,
    placeholder: String = "Buscar…",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(SurfaceWhite)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Buscar",
            tint = TextTertiary,
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(8.dp))

        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 13.sp,
                    color = TextTertiary
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(PrimaryDark)
            )
        }

        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Limpiar búsqueda",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun SearchBarWithTextPreview() {
    N3toTheme {
        SearchBar(
            query = "Manzana",
            onChange = {}
        )
    }
}

@Preview
@Composable
private fun SearchBarEmptyPreview() {
    N3toTheme {
        SearchBar(
            query = "",
            onChange = {},
            placeholder = "Buscar categoría…"
        )
    }
}
