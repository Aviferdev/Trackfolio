package es.aviferdev.n3to.ui.portfolio.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.common.topbar.TopBarWithActionsApp
import es.aviferdev.n3to.ui.portfolio.AddEditPlatformSheet
import es.aviferdev.n3to.ui.portfolio.PlatformError
import es.aviferdev.n3to.ui.portfolio.PlatformViewModel
import es.aviferdev.n3to.ui.portfolio.settings.components.SettingsGroupCard
import es.aviferdev.n3to.ui.theme.appColors
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_platform_already_exists
import n3to.composeapp.generated.resources.portfolio_settings_add
import n3to.composeapp.generated.resources.portfolio_settings_platform_empty
import n3to.composeapp.generated.resources.portfolio_settings_platform_section
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PortfolioPlatformsScreen(
    onBack: () -> Unit,
    viewModel: PlatformViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.appColors.navyDeep)
    ) {
        TopBarWithActionsApp(
            title = stringResource(Res.string.portfolio_settings_platform_section),
            navigateBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.openAddSheet() }) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(Res.string.portfolio_settings_add),
                        tint = MaterialTheme.appColors.primary
                    )
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp)
        ) {
            item {
                SettingsGroupCard {
                    if (state.platforms.isEmpty()) {
                        Text(
                            stringResource(Res.string.portfolio_settings_platform_empty),
                            fontSize = 13.sp,
                            color = MaterialTheme.appColors.textSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                        )
                    } else {
                        state.platforms.forEachIndexed { index, platform ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.openEditSheet(platform) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    platform.icon,
                                    fontSize = 18.sp,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = platform.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.appColors.textPrimary
                                    )
                                    if (!platform.notes.isNullOrBlank()) {
                                        Text(
                                            text = platform.notes,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.appColors.textSecondary,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Text("›", fontSize = 18.sp, color = MaterialTheme.appColors.textSecondary)
                            }
                            if (index < state.platforms.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.appColors.border,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 56.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showAddSheet) {
        AddEditPlatformSheet(
            initial = null,
            onSave = { name, icon, notes -> viewModel.addPlatform(name, icon, notes) },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }

    state.editing?.let { platform ->
        AddEditPlatformSheet(
            initial = platform,
            onSave = { name, icon, notes -> viewModel.renamePlatform(platform.id, name, icon, notes) },
            onDismiss = { viewModel.closeEditSheet() }
        )
    }

    state.error?.let { error ->
        val errorText = when (error) {
            is PlatformError.AlreadyExists -> stringResource(Res.string.error_platform_already_exists)
            is PlatformError.Unknown -> error.message ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.common_error),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(errorText, fontSize = 14.sp, color = MaterialTheme.appColors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
