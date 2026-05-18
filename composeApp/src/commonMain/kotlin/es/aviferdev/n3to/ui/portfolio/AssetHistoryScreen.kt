package es.aviferdev.n3to.ui.portfolio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.aviferdev.n3to.domain.model.AssetCategoryType
import es.aviferdev.n3to.domain.model.AssetTransaction
import es.aviferdev.n3to.domain.model.AssetTransactionType
import es.aviferdev.n3to.domain.model.Platform
import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.model.TransactionType
import es.aviferdev.n3to.domain.portfolio.AssetPosition
import es.aviferdev.n3to.domain.portfolio.FifoBreakdown
import es.aviferdev.n3to.domain.portfolio.FifoOpenLot
import es.aviferdev.n3to.domain.portfolio.FifoSaleMatch
import es.aviferdev.n3to.ui.common.navigation.TopBarApp
import es.aviferdev.n3to.ui.portfolio.assethistory.AssetHistoryContent
import es.aviferdev.n3to.ui.theme.BackgroundGray
import es.aviferdev.n3to.ui.theme.BorderGray
import es.aviferdev.n3to.ui.theme.ExpenseRed
import es.aviferdev.n3to.ui.theme.IncomeGreen
import es.aviferdev.n3to.ui.theme.LocalBalanceHidden
import es.aviferdev.n3to.ui.theme.PrimaryDark
import es.aviferdev.n3to.ui.theme.SurfaceElevated
import es.aviferdev.n3to.ui.theme.SurfaceWhite
import es.aviferdev.n3to.ui.theme.TextPrimary
import es.aviferdev.n3to.ui.theme.TextSecondary
import es.aviferdev.n3to.ui.theme.TextTertiary
import es.aviferdev.n3to.ui.theme.N3toTheme
import es.aviferdev.n3to.ui.theme.formatAmount
import es.aviferdev.n3to.ui.theme.maskAmount
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.abs

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
                        onSave = { _, type, qty, price, date, platformId, feeNote, notes ->
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
            containerColor = SurfaceWhite,
            icon = { Text("⚠️", fontSize = 26.sp) },
            title = {
                Text(
                    if (tx.isTransfer) "Eliminar traspaso" else "Eliminar movimiento",
                    fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                )
            },
            text = {
                Text(
                    if (tx.isTransfer)
                        "Se eliminarán ambas patas del traspaso. El P&L se recalculará. Esta acción no se puede deshacer."
                    else
                        "Se eliminará el movimiento del ${formatFullDate(tx.date)}. El P&L se recalculará. Esta acción no se puede deshacer.",
                    fontSize = 13.sp, color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Eliminar", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text(
                        "Cancelar",
                        color = PrimaryDark
                    )
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
    state.error?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            containerColor = SurfaceWhite,
            title = {
                Text(
                    "Error",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = { Text(msg, fontSize = 13.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text(
                        "Aceptar",
                        color = PrimaryDark
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
