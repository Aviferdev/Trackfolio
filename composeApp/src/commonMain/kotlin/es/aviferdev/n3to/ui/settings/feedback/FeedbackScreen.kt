package es.aviferdev.n3to.ui.settings.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.theme.CyanAccent
import es.aviferdev.n3to.ui.theme.NavyBorder
import es.aviferdev.n3to.ui.theme.NavyDeep
import es.aviferdev.n3to.ui.theme.NavySurface
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.feedback_category_label
import n3to.composeapp.generated.resources.feedback_description_field
import n3to.composeapp.generated.resources.feedback_description_placeholder
import n3to.composeapp.generated.resources.feedback_disclaimer_text
import n3to.composeapp.generated.resources.feedback_disclaimer_title
import n3to.composeapp.generated.resources.feedback_send_button
import n3to.composeapp.generated.resources.feedback_sending
import n3to.composeapp.generated.resources.feedback_subtitle
import n3to.composeapp.generated.resources.feedback_success
import n3to.composeapp.generated.resources.feedback_title
import n3to.composeapp.generated.resources.feedback_title_field
import n3to.composeapp.generated.resources.feedback_title_placeholder
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.delay

@Composable
fun FeedbackScreen(
    navigateBack: () -> Unit = {},
    viewModel: FeedbackViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successText = stringResource(Res.string.feedback_success)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FeedbackEvent.Success -> {
                    snackbarHostState.showSnackbar(successText)
                    delay(1200)
                    navigateBack()
                }
                is FeedbackEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = NavyDeep
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NavyDeep)
        ) {
            TopBarApp(
                title = stringResource(Res.string.feedback_title),
                navigateBack = navigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // ── Subtítulo ─────────────────────────────────────────────
                Text(
                    text = stringResource(Res.string.feedback_subtitle),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 18.dp)
                )

                // ── Categoría ─────────────────────────────────────────────
                Text(
                    text = stringResource(Res.string.feedback_category_label),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                CategoryChipRow(
                    categories = FeedbackCategory.entries,
                    selected = state.selectedCategory,
                    onSelected = viewModel::onCategorySelected
                )

                Spacer(Modifier.height(18.dp))

                // ── Título ────────────────────────────────────────────────
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChanged,
                    label = { Text(stringResource(Res.string.feedback_title_field)) },
                    placeholder = { Text(stringResource(Res.string.feedback_title_placeholder), color = TextTertiary) },
                    singleLine = true,
                    enabled = !state.isSending,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(11.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextTertiary,
                        cursorColor = CyanAccent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface
                    )
                )

                Spacer(Modifier.height(14.dp))

                // ── Descripción ───────────────────────────────────────────
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChanged,
                    label = { Text(stringResource(Res.string.feedback_description_field)) },
                    placeholder = { Text(stringResource(Res.string.feedback_description_placeholder), color = TextTertiary) },
                    minLines = 4,
                    maxLines = 8,
                    enabled = !state.isSending,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(11.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyBorder,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextTertiary,
                        cursorColor = CyanAccent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = NavySurface,
                        unfocusedContainerColor = NavySurface
                    )
                )

                Spacer(Modifier.height(24.dp))

                // ── Botón enviar ──────────────────────────────────────────
                Button(
                    onClick = viewModel::onSendFeedback,
                    enabled = !state.isSending,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(11.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = NavyDeep,
                        disabledContainerColor = CyanAccent.copy(alpha = 0.4f),
                        disabledContentColor = NavyDeep.copy(alpha = 0.6f)
                    )
                ) {
                    if (state.isSending) {
                        CircularProgressIndicator(
                            color = NavyDeep,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            stringResource(Res.string.feedback_sending),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            stringResource(Res.string.feedback_send_button),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Disclaimer ────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(11.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = stringResource(Res.string.feedback_disclaimer_title),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(Res.string.feedback_disclaimer_text),
                            fontSize = 11.sp,
                            color = TextTertiary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

// ─── Category Chip Row ────────────────────────────────────────────────────────
@Composable
private fun CategoryChipRow(
    categories: List<FeedbackCategory>,
    selected: FeedbackCategory,
    onSelected: (FeedbackCategory) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) CyanAccent.copy(alpha = 0.12f)
                        else NavySurface
                    )
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) CyanAccent else NavyBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelected(category) }
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.displayName,
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) CyanAccent else TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
