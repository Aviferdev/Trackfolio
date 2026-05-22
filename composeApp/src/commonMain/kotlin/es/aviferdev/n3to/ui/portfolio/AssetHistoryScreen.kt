package es.aviferdev.n3to.ui.portfolio

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.ui.portfolio.assethistory.AssetHistoryContent
import es.aviferdev.n3to.ui.portfolio.components.formatFullDate
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.appColors
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import n3to.composeapp.generated.resources.Res
import n3to.composeapp.generated.resources.common_accept
import n3to.composeapp.generated.resources.common_cancel
import n3to.composeapp.generated.resources.common_delete
import n3to.composeapp.generated.resources.common_error
import n3to.composeapp.generated.resources.error_asset_not_found
import n3to.composeapp.generated.resources.portfolio_delete_transfer_message
import n3to.composeapp.generated.resources.portfolio_delete_transfer_title
import n3to.composeapp.generated.resources.portfolio_delete_tx_message
import n3to.composeapp.generated.resources.transaction_delete_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AssetHistoryScreen(
    assetId: String,
    onBack: () -> Unit,
    viewModel: AssetHistoryViewModel = koinViewModel(parameters = { parametersOf(assetId) })
) {
    val state by viewModel.uiState.collectAsState()
    val balancesHidden = LocalBalanceHidden.current
    var fabMenuOpen by remember { mutableStateOf(false) }

    AssetHistoryContent(
        state = state,
        balancesHidden = balancesHidden,
        fabMenuOpen = fabMenuOpen,
        onFabClick = { fabMenuOpen = true },
        onFabDismiss = { fabMenuOpen = false },
        onBack = onBack,
        onRefreshClick = { viewModel.openUpdatePriceSheet() },
        onAddTransactionClick = { viewModel.openAddSheet() },
        onEditTransaction = { viewModel.openEditSheet(it) },
        onDeleteTransaction = { viewModel.requestDelete(it) },
        onAddDividendClick = { viewModel.openDividendSheet() },
        onDeleteDividend = { viewModel.deleteDividend(it) },
        onTransferClick = { viewModel.openTransferSheet() }
    )

    if (state.showAddSheet && state.asset != null) {
        AddEditAssetTransactionBottomSheet(
            transaction = state.editing,
            fixedAsset = state.asset,
            allAssets = listOfNotNull(state.asset),
            platforms = state.allPlatforms,
            platformsByAsset = state.platformsByAsset,
            categories = state.categories,
            assetTransactions = state.transactionsAsc,
            onSave = { _, type, qty, price, date, platformId, feeNote, notes, _ ->
                viewModel.saveTransaction(type, qty, price, date, platformId, feeNote, notes)
            },
            onDismiss = { viewModel.closeAddSheet() }
        )
    }
    if (state.showUpdatePriceSheet && state.asset != null) {
        UpdateCurrentPriceSheet(
            asset = state.asset!!,
            onConfirm = { viewModel.refreshCurrentPrice(it) },
            onDismiss = { viewModel.closeUpdatePriceSheet() }
        )
    }
    state.pendingDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            containerColor = MaterialTheme.appColors.surface,
            icon = { Text("⚠️", fontSize = 26.sp) },
            title = {
                Text(
                    if (tx.isTransfer) stringResource(Res.string.portfolio_delete_transfer_title) else stringResource(Res.string.transaction_delete_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = {
                Text(
                    if (tx.isTransfer)
                        stringResource(Res.string.portfolio_delete_transfer_message)
                    else
                        stringResource(Res.string.portfolio_delete_tx_message, formatFullDate(tx.date)),
                    fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text(
                        stringResource(Res.string.common_delete),
                        color = MaterialTheme.appColors.expense,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text(
                        stringResource(Res.string.common_cancel),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    state.error?.let { err ->
        val msg = when (err) {
            is es.aviferdev.n3to.ui.portfolio.AssetHistoryError.AssetNotFound -> stringResource(Res.string.error_asset_not_found)
            is es.aviferdev.n3to.ui.portfolio.AssetHistoryError.PriceHistorySaveError -> err.message
            is es.aviferdev.n3to.ui.portfolio.AssetHistoryError.Unknown -> err.message
                ?: stringResource(Res.string.common_error)
        }
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = MaterialTheme.appColors.surface,
            title = {
                Text(
                    stringResource(Res.string.common_error),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.appColors.textPrimary
                )
            },
            text = { Text(msg, fontSize = 13.sp, color = MaterialTheme.appColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(
                        stringResource(Res.string.common_accept),
                        color = MaterialTheme.appColors.primary
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    if (state.showDividendSheet && state.asset != null) {
        AddDividendBottomSheet(
            fixedAssetName = state.asset!!.name,
            onSave = { _, grossAmount, irpfPercent, date ->
                viewModel.saveDividend(
                    grossAmount,
                    irpfPercent,
                    date
                )
            },
            onDismiss = { viewModel.closeDividendSheet() }
        )
    }
    if (state.showTransferSheet && state.asset != null) {
        TransferFundBottomSheet(
            sourceAsset = state.asset!!,
            destinations = state.transferableDestinations,
            platforms = state.allPlatforms,
            assetTransactions = state.transactionsAsc,
            onExecuteTransfer = { destId, qty, srcPlat, dstPlat, vl, date ->
                viewModel.executeTransfer(destId, qty, srcPlat, dstPlat, vl, date)
            },
            onDismiss = { viewModel.closeTransferSheet() }
        )
    }
}

fun formatShortDate(epochMillis: Long): String {
    val months =
        listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    val ld: LocalDate = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${ld.dayOfMonth} ${months[ld.monthNumber - 1]} ${ld.year}"
}
