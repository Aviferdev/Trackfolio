package es.aviferdev.trackfolio.di

import es.aviferdev.trackfolio.domain.usecase.account.DeleteAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.trackfolio.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.trackfolio.domain.usecase.account.SaveAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.trackfolio.domain.usecase.account.UpdateAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.GetOutdatedAssetsUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.SavePriceReminderShownUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.ShouldShowPriceReminderUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.trackfolio.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.ArchiveAssetCategoryUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.GetAssetCategoriesUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.RenameAssetCategoryUseCase
import es.aviferdev.trackfolio.domain.usecase.assetcategory.SaveAssetCategoryUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.ArchiveAssetTagUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.GetAssetTagAssignmentsUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.GetAssetTagsByCategoryUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.GetAssetTagsUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.RemoveAssetTagAssignmentUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.RenameAssetTagUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.SaveAssetTagUseCase
import es.aviferdev.trackfolio.domain.usecase.assettag.UpsertAssetTagAssignmentUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.ExecuteFundTransferUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.DeleteAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetTransactionsByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetTransactionsByAssetDescUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetTransactionsByAssetUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.UpdateAssetTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.category.GetAllCategoriesIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.DeleteDebtUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.GetActiveDebtsUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.MarkDebtAsPaidUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.SaveDebtUseCase
import es.aviferdev.trackfolio.domain.usecase.debt.UpdateDebtUseCase
import es.aviferdev.trackfolio.domain.usecase.reconciliation.BalanceAlreadyMatchesException
import es.aviferdev.trackfolio.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase
import es.aviferdev.trackfolio.domain.usecase.reconciliation.ReconcileBalanceUseCase
import es.aviferdev.trackfolio.domain.usecase.reconciliation.ShouldShowReconciliationReminderUseCase
import es.aviferdev.trackfolio.domain.usecase.fiscal.GetFiscalReportDataUseCase
import es.aviferdev.trackfolio.domain.usecase.home.GetHomeBalanceUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.ArchiveIssuerUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.GetAllIssuersIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.RenameIssuerUseCase
import es.aviferdev.trackfolio.domain.usecase.issuer.SaveIssuerUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.ArchivePlatformUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.GetAllPlatformsIncludingArchivedUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.RenamePlatformUseCase
import es.aviferdev.trackfolio.domain.usecase.platform.SavePlatformUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetAnnualSummaryUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyBreakdownUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.annual.AnnualViewModel
import es.aviferdev.trackfolio.ui.debt.DebtViewModel
import es.aviferdev.trackfolio.ui.fiscal.FiscalReportViewModel
import es.aviferdev.trackfolio.ui.home.AddTransactionViewModel
import es.aviferdev.trackfolio.ui.home.HomeViewModel
import es.aviferdev.trackfolio.ui.portfolio.AssetCatalogViewModel
import es.aviferdev.trackfolio.ui.portfolio.AssetCategoryDetailViewModel
import es.aviferdev.trackfolio.ui.portfolio.AssetDetailViewModel
import es.aviferdev.trackfolio.ui.portfolio.AssetCategoryViewModel
import es.aviferdev.trackfolio.ui.portfolio.AssetHistoryViewModel
import es.aviferdev.trackfolio.ui.portfolio.PlatformViewModel
import es.aviferdev.trackfolio.ui.portfolio.PortfolioViewModel
import es.aviferdev.trackfolio.ui.reconciliation.ReconciliationViewModel
import es.aviferdev.trackfolio.ui.settings.backup.BackupViewModel
import es.aviferdev.trackfolio.ui.settings.CategoryViewModel
import es.aviferdev.trackfolio.ui.settings.IssuerViewModel
import es.aviferdev.trackfolio.ui.transaction.TransactionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val useCaseModule = module {
    // Sesión compartida
    single { AccountSession() }

    // ── Account ──────────────────────────────────────────────────────────────
    factory { GetAccountsUseCase(get()) }
    factory { GetAccountByIdUseCase(get()) }
    factory { SaveAccountUseCase(get()) }
    factory { UpdateAccountUseCase(get()) }
    factory { DeleteAccountUseCase(get()) }
    factory { SetInitialBalanceUseCase(get()) }

    // ── Transaction ───────────────────────────────────────────────────────────
    factory { GetTransactionsByMonthUseCase(get()) }
    factory { GetMonthlyTotalsUseCase(get()) }
    factory { SaveTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetAnnualSummaryUseCase(get()) }
    factory { GetMonthlyBreakdownUseCase(get()) }
    factory { GetOldestTransactionDateUseCase(get()) }

    // ── Home ──────────────────────────────────────────────────────────────────
    factory { GetHomeBalanceUseCase(get(), get(), get()) }

    // ── Debts ─────────────────────────────────────────────────────────────────
    factory { GetActiveDebtsUseCase(get()) }
    factory { SaveDebtUseCase(get()) }
    factory { UpdateDebtUseCase(get()) }
    factory { MarkDebtAsPaidUseCase(get()) }
    factory { DeleteDebtUseCase(get()) }

    // ── Asset (catálogo) ──────────────────────────────────────────────────────
    factory { GetAssetsByAccountUseCase(get()) }
    factory { SaveAssetUseCase(get()) }
    factory { UpdateAssetUseCase(get()) }
    factory { UpdateAssetCurrentPriceUseCase(get(), get()) }
    factory { ArchiveAssetUseCase(get()) }
    factory { UnarchiveAssetUseCase(get()) }
    factory { GetOutdatedAssetsUseCase(get(), get()) }
    factory { ShouldShowPriceReminderUseCase(get()) }
    factory { SavePriceReminderShownUseCase(get()) }
    factory { GetPriceReminderIntervalUseCase(get()) }

    // ── Asset Transaction ─────────────────────────────────────────────────────
    factory { GetTransactionsByAssetUseCase(get()) }
    factory { GetTransactionsByAssetDescUseCase(get()) }
    factory { GetTransactionsByAccountUseCase(get()) }
    factory { SaveAssetTransactionUseCase(get()) }
    factory { UpdateAssetTransactionUseCase(get()) }
    factory { DeleteAssetTransactionUseCase(get()) }
    factory { SyncAssetTransactionToLedgerUseCase(get()) }
    factory { ExecuteFundTransferUseCase(get()) }

    // ── Asset Category ────────────────────────────────────────────────────────
    factory { GetAssetCategoriesUseCase(get()) }
    factory { GetAllAssetCategoriesIncludingArchivedUseCase(get()) }
    factory { SaveAssetCategoryUseCase(get()) }
    factory { RenameAssetCategoryUseCase(get()) }
    factory { ArchiveAssetCategoryUseCase(get()) }

    // ── Asset Tag ─────────────────────────────────────────────────────────────
    factory { GetAssetTagsUseCase(get()) }
    factory { GetAssetTagsByCategoryUseCase(get()) }
    factory { SaveAssetTagUseCase(get()) }
    factory { RenameAssetTagUseCase(get()) }
    factory { ArchiveAssetTagUseCase(get()) }
    factory { GetAssetTagAssignmentsUseCase(get()) }
    factory { UpsertAssetTagAssignmentUseCase(get()) }
    factory { RemoveAssetTagAssignmentUseCase(get()) }

    // ── Platform ──────────────────────────────────────────────────────────────
    factory { GetPlatformsUseCase(get()) }
    factory { GetAllPlatformsIncludingArchivedUseCase(get()) }
    factory { SavePlatformUseCase(get()) }
    factory { RenamePlatformUseCase(get()) }
    factory { ArchivePlatformUseCase(get()) }

    // ── Category (solo gastos) ────────────────────────────────────────────────
    factory { GetCategoriesByTypeUseCase(get()) }
    factory { GetAllCategoriesIncludingArchivedUseCase(get()) }

    // ── Issuer (entidades emisoras de ingresos) ───────────────────────────────
    factory { GetIssuersUseCase(get()) }
    factory { GetAllIssuersIncludingArchivedUseCase(get()) }
    factory { SaveIssuerUseCase(get()) }
    factory { RenameIssuerUseCase(get()) }
    factory { ArchiveIssuerUseCase(get()) }

    // ── Reconciliation ─────────────────────────────────────────────────────────
    factory { ReconcileBalanceUseCase(get(), get()) }
    factory { ShouldShowReconciliationReminderUseCase(get()) }
    factory { GetReconciliationReminderIntervalUseCase(get()) }

    // ── Fiscal ────────────────────────────────────────────────────────────────
    factory {
        GetFiscalReportDataUseCase(
            accountRepository          = get(),
            transactionRepository      = get(),
            debtRepository             = get(),
            assetRepository            = get(),
            assetTransactionRepository = get(),
            assetCategoryRepository    = get()
        )
    }

    // ── ViewModels ────────────────────────────────────────────────────────────
    viewModel {
        AccountViewModel(
            getAccounts       = get(),
            saveAccount       = get(),
            updateAccount     = get(),
            deleteAccount     = get(),
            setInitialBalance = get(),
            session           = get()
        )
    }
    viewModel {
        HomeViewModel(
            getHomeBalance            = get(),
            getCategoriesByType       = get(),
            setInitialBalance         = get(),
            session                   = get(),
            shouldShowPriceReminder   = get(),
            getOutdatedAssets         = get(),
            updateAssetCurrentPrice   = get(),
            savePriceReminderShown    = get()
        )
    }
    viewModel {
        AddTransactionViewModel(
            saveTransaction     = get(),
            updateTransaction   = get(),
            getCategoriesByType = get(),
            getIssuers          = get(),
            session             = get()
        )
    }
    viewModel {
        TransactionViewModel(
            getTransactionsByMonth            = get(),
            getMonthlyTotals                  = get(),
            deleteTransactionUseCase          = get(),
            getAllCategoriesIncludingArchived  = get(),
            getOldestDate                     = get(),
            session                           = get()
        )
    }
    viewModel { DebtViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ReconciliationViewModel(get(), get(), get(), get()) }
    viewModel {
        AnnualViewModel(
            getAnnualSummary    = get(),
            getMonthlyBreakdown = get(),
            getOldestDate       = get(),
            session             = get()
        )
    }
    viewModel {
        PortfolioViewModel(
            getAssetsByAccount                  = get(),
            saveAsset                           = get(),
            updateAsset                         = get(),
            updateAssetCurrentPrice             = get(),
            archiveAsset                        = get(),
            getAssetCategoriesIncludingArchived = get(),
            getAccountById                      = get(),
            getTransactionsByAccount            = get(),
            getPlatforms                        = get(),
            saveAssetTransaction                = get(),
            syncToLedger                        = get(),
            session                             = get()
        )
    }
    viewModel {
        AssetCatalogViewModel(
            getAssetsByAccount                  = get(),
            saveAsset                           = get(),
            updateAsset                         = get(),
            archiveAsset                        = get(),
            unarchiveAsset                      = get(),
            assetTransactionRepository          = get(),
            getAssetCategoriesIncludingArchived = get(),
            assetPlatformRepository             = get(),
            session                             = get()
        )
    }
    viewModel {
        AssetCategoryViewModel(
            getCategories   = get(),
            saveCategory    = get(),
            renameCategory  = get(),
            archiveCategory = get()
        )
    }
    viewModel {
        PlatformViewModel(
            getPlatforms    = get(),
            savePlatform    = get(),
            renamePlatform  = get(),
            archivePlatform = get()
        )
    }
    viewModel { (assetId: String) ->
        AssetHistoryViewModel(
            assetId                  = assetId,
            getAssetById             = get(),
            getTransactionsByAsset   = get(),
            getPlatforms             = get(),
            platformCategoryRepository = get(),
            assetPlatformRepository  = get(),
            getAccountById           = get(),
            saveAssetTransaction     = get(),
            updateAssetTransaction   = get(),
            deleteAssetTransaction   = get(),
            updateAssetCurrentPrice  = get(),
            syncToLedger             = get(),
            transactionRepository    = get(),
            executeFundTransfer      = get(),
            assetRepository          = get()
        )
    }
    viewModel { (categoryId: String) ->
        AssetCategoryDetailViewModel(
            categoryId                          = categoryId,
            assetRepository                     = get(),
            assetTransactionRepository          = get(),
            assetPlatformRepository             = get(),
            getAssetCategoriesIncludingArchived = get(),
            getPlatforms                        = get(),
            platformCategoryRepository          = get(),
            platformRepository                  = get(),
            saveAsset                           = get(),
            updateAsset                         = get(),
            archiveAsset                        = get(),
            unarchiveAsset                      = get(),
            session                             = get()
        )
    }
    viewModel { (assetId: String) ->
        AssetDetailViewModel(
            assetId                 = assetId,
            assetRepository         = get(),
            assetPlatformRepository = get(),
            getPlatforms            = get(),
            savePlatform            = get()
        )
    }
    viewModel { BackupViewModel(get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel {
        IssuerViewModel(
            getIssuers    = get(),
            saveIssuer    = get(),
            renameIssuer  = get(),
            archiveIssuer = get(),
            session       = get()
        )
    }

    viewModel {
        FiscalReportViewModel(
            getFiscalReportData = get(),
            pdfGenerator        = get(),
            session             = get()
        )
    }
}
