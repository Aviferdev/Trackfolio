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
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.DeleteRegionUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.DeleteSectorUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.SaveRegionUseCase
import es.aviferdev.trackfolio.domain.usecase.assetmetadata.SaveSectorUseCase
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
import es.aviferdev.trackfolio.domain.usecase.fixedincome.CreateFixedIncomePositionUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.CreateLedgerTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.CloseFixedIncomeUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.DeleteFixedIncomeEventUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.DeleteLinkedTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.loan.ArchiveLoanUseCase
import es.aviferdev.trackfolio.domain.usecase.loan.GetAmortizationScheduleUseCase
import es.aviferdev.trackfolio.domain.usecase.loan.GetLoansByAccountUseCase
import es.aviferdev.trackfolio.domain.usecase.loan.SaveLoanUseCase
import es.aviferdev.trackfolio.domain.usecase.loan.UpdateLoanRateUseCase
import es.aviferdev.trackfolio.domain.usecase.loan.UpdateLoanUseCase
import es.aviferdev.trackfolio.domain.usecase.networth.GetNetWorthDataUseCase
import es.aviferdev.trackfolio.domain.usecase.networth.GetNetWorthHistoryUseCase
import es.aviferdev.trackfolio.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.GetCouponScheduleUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.GetFixedIncomePositionDetailUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.GetFixedIncomeSummaryUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.ArchiveFixedIncomePositionUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.UpdateFixedIncomePositionUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.RegisterCouponUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.RecordIncomeTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.fixedincome.RecordSettlementTransactionUseCase
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
import es.aviferdev.trackfolio.domain.usecase.transaction.GetExpensesByCategoryUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetIncomeByTypeUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyBreakdownUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetTransactionByIdUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.trackfolio.domain.usecase.assettransaction.GetMonthlyInvestmentsUseCase
import es.aviferdev.trackfolio.ui.account.AccountSession
import es.aviferdev.trackfolio.ui.account.AccountViewModel
import es.aviferdev.trackfolio.ui.annual.AnnualViewModel
import es.aviferdev.trackfolio.ui.debt.DebtViewModel
import es.aviferdev.trackfolio.ui.fiscal.FiscalReportViewModel
import es.aviferdev.trackfolio.ui.fixedincome.FixedIncomeDetailViewModel
import es.aviferdev.trackfolio.ui.loan.LoanDetailViewModel
import es.aviferdev.trackfolio.ui.networth.NetWorthViewModel
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
import es.aviferdev.trackfolio.ui.transaction.TransactionDetailViewModel
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
    factory { GetTransactionByIdUseCase(get()) }
    factory { SaveTransactionUseCase(get()) }
    factory { UpdateTransactionUseCase(get()) }
    factory { DeleteTransactionUseCase(get()) }
    factory { GetAnnualSummaryUseCase(get()) }
    factory { GetMonthlyBreakdownUseCase(get()) }
    factory { GetOldestTransactionDateUseCase(get()) }
    factory { GetExpensesByCategoryUseCase(get()) }
    factory { GetIncomeByTypeUseCase(get()) }

    // ── Asset Transaction (inversiones) ─────────────────────────────────────
    factory { GetMonthlyInvestmentsUseCase(get()) }

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

    // ── Asset Metadata (Sectors & Regions) ─────────────────────────────────────
    factory { GetSectorsUseCase(get()) }
    factory { SaveSectorUseCase(get()) }
    factory { DeleteSectorUseCase(get()) }
    factory { GetRegionsUseCase(get()) }
    factory { SaveRegionUseCase(get()) }
    factory { DeleteRegionUseCase(get()) }

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

    // ── Reconciliation ──────────────────────────────────────────────────────────
    factory { ReconcileBalanceUseCase(get(), get()) }
    factory { ShouldShowReconciliationReminderUseCase(get()) }
    factory { GetReconciliationReminderIntervalUseCase(get()) }

    // ── Fixed Income ────────────────────────────────────────────────────────────
    factory { CreateLedgerTransactionUseCase(get()) }
    factory { RecordIncomeTransactionUseCase(get()) }
    factory { RecordSettlementTransactionUseCase(get()) }
    factory { CreateFixedIncomePositionUseCase(get(), get(), get()) }
    factory { RegisterCouponUseCase(get(), get()) }
    factory { CloseFixedIncomeUseCase(get(), get(), get()) }
    factory { GetFixedIncomeSummaryUseCase(get(), get()) }
    factory { GetFixedIncomePositionDetailUseCase(get(), get()) }
    factory { GetNearMaturityPositionsUseCase(get()) }
    factory { GetCouponScheduleUseCase() }
    factory { UpdateFixedIncomePositionUseCase(get()) }
    factory { ArchiveFixedIncomePositionUseCase(get()) }
    factory { DeleteLinkedTransactionUseCase(get()) }
    factory { DeleteFixedIncomeEventUseCase(get(), get()) }

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
            savePriceReminderShown    = get(),
            getNearMaturityPositions  = get(),
            loadingManager            = get()
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
    viewModel { (transactionId: String) ->
        TransactionDetailViewModel(
            transactionId                    = transactionId,
            getTransactionById               = get(),
            getAccountById                   = get(),
            getAllCategoriesIncludingArchived = get(),
            deleteTransactionUseCase         = get()
        )
    }
    viewModel { DebtViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ReconciliationViewModel(get(), get(), get(), get()) }
    viewModel {
        AnnualViewModel(
            getAnnualSummary       = get(),
            getMonthlyBreakdown    = get(),
            getOldestDate          = get(),
            getExpensesByCategory  = get(),
            getIncomeByType        = get(),
            getMonthlyInvestments  = get(),
            session                = get()
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
            assetPlatformRepository             = get(),
            assetMetadataRepository            = get(),
            session                             = get(),
            getFixedIncomeSummary               = get(),
            getNearMaturityPositions            = get(),
            createFixedIncomePosition           = get(),
            getBondIssuers                      = get(),
            saveBondIssuer                      = get(),
            getPortfolioValueHistory            = get()
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
            assetMetadataRepository            = get(),
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
            getAssetCategoriesIncludingArchived = get(),
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
            assetMetadataRepository            = get(),
            getAssetCategoriesIncludingArchived = get(),
            getPlatforms                        = get(),
            platformCategoryRepository          = get(),
            platformRepository                  = get(),
            saveAsset                           = get(),
            updateAsset                         = get(),
            archiveAsset                        = get(),
            unarchiveAsset                      = get(),
            fixedIncomeRepository               = get(),
            fixedIncomeEventRepository          = get(),
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
    viewModel { (positionId: String) ->
        FixedIncomeDetailViewModel(
            positionId                    = positionId,
            getPositionDetail             = get(),
            getCouponSchedule             = get(),
            registerCoupon                = get(),
            closeFixedIncome              = get(),
            deleteFixedIncomeEvent        = get(),
            updatePosition                = get(),
            archivePosition               = get(),
            transactionRepository          = get()
        )
    }

    // ── Loan ─────────────────────────────────────────────────────────────────────
    factory { SaveLoanUseCase(get()) }
    factory { UpdateLoanUseCase(get()) }
    factory { UpdateLoanRateUseCase(get(), get()) }
    factory { GetLoansByAccountUseCase(get()) }
    factory { GetAmortizationScheduleUseCase(get(), get()) }
    factory { ArchiveLoanUseCase(get()) }

    // ── Net Worth ────────────────────────────────────────────────────────────────
    factory { GetNetWorthDataUseCase(get(), get(), get(), get(), get(), get()) }
    factory { GetPortfolioValueHistoryUseCase(get(), get(), get(), get()) }
    factory { GetNetWorthHistoryUseCase(get(), get(), get(), get(), get()) }

    // ── ViewModels (Loan / NetWorth) ─────────────────────────────────────────────
    viewModel {
        NetWorthViewModel(
            getNetWorthData      = get(),
            getLoansByAccount    = get(),
            getNetWorthHistory   = get(),
            session              = get(),
            loadingManager       = get()
        )
    }
    viewModel { (loanId: String) ->
        LoanDetailViewModel(
            loanId                    = loanId,
            loanRepository            = get(),
            getAmortizationSchedule   = get(),
            updateLoanRate            = get(),
            updateLoan                = get(),
            archiveLoan               = get(),
            rateChangeRepository      = get()
        )
    }
}
