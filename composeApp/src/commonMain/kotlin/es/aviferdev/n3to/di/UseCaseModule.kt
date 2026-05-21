package es.aviferdev.n3to.di

import es.aviferdev.n3to.core.VersionManager
import es.aviferdev.n3to.domain.usecase.account.DeleteAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.GetAccountByIdUseCase
import es.aviferdev.n3to.domain.usecase.account.GetAccountsUseCase
import es.aviferdev.n3to.domain.usecase.account.SaveAccountUseCase
import es.aviferdev.n3to.domain.usecase.account.SetInitialBalanceUseCase
import es.aviferdev.n3to.domain.usecase.account.UpdateAccountUseCase
import es.aviferdev.n3to.domain.usecase.asset.AppStartupRefreshUseCase
import es.aviferdev.n3to.domain.usecase.asset.ArchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.CheckAssetArchivableUseCase
import es.aviferdev.n3to.domain.usecase.asset.ConvertPriceToEurUseCase
import es.aviferdev.n3to.domain.usecase.asset.DetectPriceAnomalyUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAllAssetsIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAssetEditMetadataUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetAssetsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetOutdatedAssetsUseCase
import es.aviferdev.n3to.domain.usecase.asset.GetPriceReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.asset.RefreshExchangeRateUseCase
import es.aviferdev.n3to.domain.usecase.asset.RefreshPortfolioPricesUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.SaveAssetWithMetadataUseCase
import es.aviferdev.n3to.domain.usecase.asset.SavePriceReminderShownUseCase
import es.aviferdev.n3to.domain.usecase.asset.ShouldRefreshTodayUseCase
import es.aviferdev.n3to.domain.usecase.asset.ShouldShowPriceReminderUseCase
import es.aviferdev.n3to.domain.usecase.asset.UnarchiveAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetCurrentPriceUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetUseCase
import es.aviferdev.n3to.domain.usecase.asset.UpdateAssetWithMetadataUseCase
import es.aviferdev.n3to.domain.usecase.asset.ValidateAssetIdentifierUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.ArchiveAssetCategoryUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAllAssetCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.GetAssetCategoriesUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.RenameAssetCategoryUseCase
import es.aviferdev.n3to.domain.usecase.assetcategory.SaveAssetCategoryUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteRegionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.DeleteSectorUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetRegionsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.GetSectorsUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveRegionUseCase
import es.aviferdev.n3to.domain.usecase.assetmetadata.SaveSectorUseCase
import es.aviferdev.n3to.domain.usecase.assetpricehistory.SaveAssetPriceHistoryUseCase
import es.aviferdev.n3to.domain.usecase.assettag.ArchiveAssetTagUseCase
import es.aviferdev.n3to.domain.usecase.assettag.GetAssetTagAssignmentsUseCase
import es.aviferdev.n3to.domain.usecase.assettag.GetAssetTagsByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.assettag.GetAssetTagsUseCase
import es.aviferdev.n3to.domain.usecase.assettag.RemoveAssetTagAssignmentUseCase
import es.aviferdev.n3to.domain.usecase.assettag.RenameAssetTagUseCase
import es.aviferdev.n3to.domain.usecase.assettag.SaveAssetTagUseCase
import es.aviferdev.n3to.domain.usecase.assettag.UpsertAssetTagAssignmentUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.DeleteAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.ExecuteFundTransferUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetMonthlyInvestmentsUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetMonthlyNetInvestmentsUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAccountUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAssetDescUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.GetTransactionsByAssetUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SaveAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.SyncAssetTransactionToLedgerUseCase
import es.aviferdev.n3to.domain.usecase.assettransaction.UpdateAssetTransactionUseCase
import es.aviferdev.n3to.domain.usecase.backup.GetBackupReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.backup.GetLastBackupDateUseCase
import es.aviferdev.n3to.domain.usecase.backup.SaveBackupReminderDismissedUseCase
import es.aviferdev.n3to.domain.usecase.backup.SaveBackupReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.backup.SaveLastBackupDateUseCase
import es.aviferdev.n3to.domain.usecase.backup.ShouldShowBackupReminderUseCase
import es.aviferdev.n3to.domain.usecase.budget.GetCategoryBudgetStatusUseCase
import es.aviferdev.n3to.domain.usecase.category.GetAllCategoriesIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.category.GetCategoriesByTypeUseCase
import es.aviferdev.n3to.domain.usecase.category.SeedDefaultCategoriesUseCase
import es.aviferdev.n3to.domain.usecase.debt.DeleteDebtUseCase
import es.aviferdev.n3to.domain.usecase.debt.GetActiveDebtsUseCase
import es.aviferdev.n3to.domain.usecase.debt.MarkDebtAsPaidUseCase
import es.aviferdev.n3to.domain.usecase.debt.SaveDebtUseCase
import es.aviferdev.n3to.domain.usecase.debt.UpdateDebtUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.GetEmergencyFundStatusUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.GetEmergencyFundUseCase
import es.aviferdev.n3to.domain.usecase.emergencyfund.SaveEmergencyFundUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.CalculateIrpfUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.CalculateNetIncomeUseCase
import es.aviferdev.n3to.domain.usecase.fiscal.GetFiscalReportDataUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.ArchiveFixedIncomePositionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.CloseFixedIncomeUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.CreateFixedIncomePositionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.CreateLedgerTransactionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.DeleteFixedIncomeEventUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.DeleteLinkedTransactionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetCouponScheduleUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetFixedIncomePositionDetailUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetFixedIncomeRowsByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetFixedIncomeSummaryUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.GetNearMaturityPositionsUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.RecordIncomeTransactionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.RecordSettlementTransactionUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.RegisterCouponUseCase
import es.aviferdev.n3to.domain.usecase.fixedincome.UpdateFixedIncomePositionUseCase
import es.aviferdev.n3to.domain.usecase.goal.GetCurrentMonthProgressUseCase
import es.aviferdev.n3to.domain.usecase.goal.GetMonthlyGoalsUseCase
import es.aviferdev.n3to.domain.usecase.goal.GetYearlyGoalProgressUseCase
import es.aviferdev.n3to.domain.usecase.goal.SaveMonthlyGoalUseCase
import es.aviferdev.n3to.domain.usecase.goal.SaveMonthlyGoalsUseCase
import es.aviferdev.n3to.domain.usecase.home.GetHomeBalanceUseCase
import es.aviferdev.n3to.domain.usecase.issuer.ArchiveIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.CreateIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetAllIssuersIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.issuer.GetIssuersUseCase
import es.aviferdev.n3to.domain.usecase.issuer.RenameIssuerUseCase
import es.aviferdev.n3to.domain.usecase.issuer.SaveIssuerUseCase
import es.aviferdev.n3to.domain.usecase.loan.ArchiveLoanUseCase
import es.aviferdev.n3to.domain.usecase.loan.GetAmortizationScheduleUseCase
import es.aviferdev.n3to.domain.usecase.loan.GetLoansByAccountUseCase
import es.aviferdev.n3to.domain.usecase.loan.SaveLoanUseCase
import es.aviferdev.n3to.domain.usecase.loan.UpdateLoanRateUseCase
import es.aviferdev.n3to.domain.usecase.loan.UpdateLoanUseCase
import es.aviferdev.n3to.domain.usecase.networth.GetNetWorthDataUseCase
import es.aviferdev.n3to.domain.usecase.networth.GetNetWorthHistoryUseCase
import es.aviferdev.n3to.domain.usecase.platform.ArchivePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.CreateAndLinkPlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetAllPlatformsIncludingArchivedUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.platform.GetPlatformsUseCase
import es.aviferdev.n3to.domain.usecase.platform.LinkPlatformToCategoryUseCase
import es.aviferdev.n3to.domain.usecase.platform.RenamePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.SavePlatformUseCase
import es.aviferdev.n3to.domain.usecase.platform.UnlinkPlatformFromCategoryUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.DeletePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfolioValueHistoryUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.GetPortfoliosByAccountUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.SavePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.portfolio.UpdatePortfolioUseCase
import es.aviferdev.n3to.domain.usecase.realestate.ArchivePropertyUseCase
import es.aviferdev.n3to.domain.usecase.realestate.ChangeRentalStatusUseCase
import es.aviferdev.n3to.domain.usecase.realestate.DismissMortgageReminderUseCase
import es.aviferdev.n3to.domain.usecase.realestate.GetPropertiesByAccountUseCase
import es.aviferdev.n3to.domain.usecase.realestate.GetPropertyFinancialSummaryUseCase
import es.aviferdev.n3to.domain.usecase.realestate.GetRentalPeriodsUseCase
import es.aviferdev.n3to.domain.usecase.realestate.GetTransactionsByPropertyUseCase
import es.aviferdev.n3to.domain.usecase.realestate.LinkLoanUseCase
import es.aviferdev.n3to.domain.usecase.realestate.SavePropertyUseCase
import es.aviferdev.n3to.domain.usecase.realestate.SellPropertyUseCase
import es.aviferdev.n3to.domain.usecase.realestate.UpdatePropertyValueUseCase
import es.aviferdev.n3to.domain.usecase.reconciliation.GetReconciliationReminderIntervalUseCase
import es.aviferdev.n3to.domain.usecase.reconciliation.ReconcileBalanceUseCase
import es.aviferdev.n3to.domain.usecase.reconciliation.ShouldShowReconciliationReminderUseCase
import es.aviferdev.n3to.domain.usecase.savingsrates.GetSavingsRatesUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.DeleteTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetActiveTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.GetAllTaxProfileSnapshotsUseCase
import es.aviferdev.n3to.domain.usecase.taxprofile.SaveTaxProfileSnapshotUseCase
import es.aviferdev.n3to.domain.usecase.transaction.DeleteTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetAnnualSummaryUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetDividendsByAssetIdsUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetExpensesByCategoryByMonthUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetExpensesByCategoryUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetIncomeByTypeByMonthUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetIncomeByTypeUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyBreakdownUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetMonthlyTotalsUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetOldestTransactionDateUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetTransactionByIdUseCase
import es.aviferdev.n3to.domain.usecase.transaction.GetTransactionsByMonthUseCase
import es.aviferdev.n3to.domain.usecase.transaction.SaveTransactionUseCase
import es.aviferdev.n3to.domain.usecase.transaction.UpdateTransactionUseCase
import es.aviferdev.n3to.domain.usecase.valuable.DeleteValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.GetAllValuablesByAccountUseCase
import es.aviferdev.n3to.domain.usecase.valuable.GetValuableDetailUseCase
import es.aviferdev.n3to.domain.usecase.valuable.GetValuablesByAccountUseCase
import es.aviferdev.n3to.domain.usecase.valuable.LinkLoanToValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.SaveValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.SellValuableUseCase
import es.aviferdev.n3to.domain.usecase.valuable.UpdateValuableEstimatedValueUseCase
import es.aviferdev.n3to.domain.usecase.version.DismissVersionBannerUseCase
import es.aviferdev.n3to.domain.usecase.version.GetVersionInfoUseCase
import es.aviferdev.n3to.ui.account.AccountSession
import es.aviferdev.n3to.ui.account.AccountViewModel
import es.aviferdev.n3to.ui.annual.AnnualViewModel
import es.aviferdev.n3to.ui.security.LockViewModel
import es.aviferdev.n3to.ui.settings.AboutViewModel
import es.aviferdev.n3to.ui.debt.DebtViewModel
import es.aviferdev.n3to.ui.fiscal.FiscalReportViewModel
import es.aviferdev.n3to.ui.fixedincome.FixedIncomeDetailViewModel
import es.aviferdev.n3to.ui.home.viewmodel.AddTransactionViewModel
import es.aviferdev.n3to.ui.home.viewmodel.CategoryPickerViewModel
import es.aviferdev.n3to.ui.home.viewmodel.HomeViewModel
import es.aviferdev.n3to.ui.loan.LoanDetailViewModel
import es.aviferdev.n3to.ui.networth.NetWorthViewModel
import es.aviferdev.n3to.ui.portfolio.AssetCatalogViewModel
import es.aviferdev.n3to.ui.portfolio.AssetCategoryDetailViewModel
import es.aviferdev.n3to.ui.portfolio.AssetCategoryViewModel
import es.aviferdev.n3to.ui.portfolio.AssetDetailViewModel
import es.aviferdev.n3to.ui.portfolio.AssetHistoryViewModel
import es.aviferdev.n3to.ui.portfolio.PlatformViewModel
import es.aviferdev.n3to.ui.portfolio.PortfolioStateBuilder
import es.aviferdev.n3to.ui.portfolio.home.PortfolioViewModel
import es.aviferdev.n3to.ui.portfolio.settings.PortfolioSettingsViewModel
import es.aviferdev.n3to.ui.realestate.RealEstateDetailViewModel
import es.aviferdev.n3to.ui.reconciliation.ReconciliationViewModel
import es.aviferdev.n3to.ui.savingsrates.SavingsRatesViewModel
import es.aviferdev.n3to.ui.settings.AccountConfigViewModel
import es.aviferdev.n3to.ui.settings.CategoryViewModel
import es.aviferdev.n3to.ui.settings.IssuerViewModel
import es.aviferdev.n3to.ui.settings.backup.BackupViewModel
import es.aviferdev.n3to.ui.settings.emergencyfund.EmergencyFundSettingsViewModel
import es.aviferdev.n3to.ui.settings.feedback.FeedbackViewModel
import es.aviferdev.n3to.ui.settings.goal.GoalSettingsViewModel
import es.aviferdev.n3to.ui.settings.taxprofile.TaxProfileSettingsViewModel
import es.aviferdev.n3to.ui.transaction.TransactionDetailViewModel
import es.aviferdev.n3to.ui.transaction.TransactionViewModel
import es.aviferdev.n3to.ui.valuable.ValuableDetailViewModel
import es.aviferdev.n3to.ui.valuable.ValuableListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val useCaseModule = module {
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
    factory { GetExpensesByCategoryByMonthUseCase(get()) }
    factory { GetIncomeByTypeUseCase(get()) }
    factory { GetIncomeByTypeByMonthUseCase(get()) }
    factory { GetDividendsByAssetIdsUseCase(get()) }
    // ── Asset Price History ───────────────────────────────────────────────────
    factory { SaveAssetPriceHistoryUseCase(get()) }

    // ── Asset Transaction (inversiones) ─────────────────────────────────────
    factory { GetMonthlyInvestmentsUseCase(get()) }
    factory { GetMonthlyNetInvestmentsUseCase(get()) }
    // ── Goals ──────────────────────────────────────────────────────────────────
    factory { GetMonthlyGoalsUseCase(get()) }
    factory { SaveMonthlyGoalUseCase(get()) }
    factory { SaveMonthlyGoalsUseCase(get()) }
    factory { GetCurrentMonthProgressUseCase(get(), get(), get()) }
    factory { GetYearlyGoalProgressUseCase(get(), get(), get()) }
    // ── Emergency Fund ─────────────────────────────────────────────────────────
    factory { GetEmergencyFundUseCase(get()) }
    factory { SaveEmergencyFundUseCase(get()) }
    factory { GetEmergencyFundStatusUseCase(get(), get(), get()) }
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
    factory { GetAllAssetsIncludingArchivedUseCase(get()) }
    factory { SaveAssetUseCase(get(), get()) }
    factory { SaveAssetWithMetadataUseCase(get(), get(), get()) }
    factory { UpdateAssetUseCase(get()) }
    factory { UpdateAssetWithMetadataUseCase(get(), get(), get()) }
    factory { UpdateAssetCurrentPriceUseCase(get(), get()) }
    factory { ArchiveAssetUseCase(get()) }
    factory { UnarchiveAssetUseCase(get()) }
    factory { CheckAssetArchivableUseCase(get()) }
    factory { GetAssetEditMetadataUseCase(get(), get()) }
    factory { GetOutdatedAssetsUseCase(get(), get()) }
    factory { ShouldShowPriceReminderUseCase(get()) }
    factory { SavePriceReminderShownUseCase(get()) }
    factory { GetPriceReminderIntervalUseCase(get()) }

    // ── Precios automáticos e ISIN ────────────────────────────────────────────
    factory { ShouldRefreshTodayUseCase(get()) }
    factory { ConvertPriceToEurUseCase(get()) }
    factory { DetectPriceAnomalyUseCase() }
    factory { ValidateAssetIdentifierUseCase(get()) }
    factory { RefreshExchangeRateUseCase(get(), get()) }
    factory { RefreshPortfolioPricesUseCase(get(), get(), get(), get(), get(), get()) }
    factory { AppStartupRefreshUseCase(get(), get()) }
    // ── Portfolio ────────────────────────────────────────────────────────────────
    factory { GetPortfoliosByAccountUseCase(get()) }
    factory { SavePortfolioUseCase(get()) }
    factory { UpdatePortfolioUseCase(get()) }
    factory { DeletePortfolioUseCase(get()) }
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
    factory { GetPlatformsByCategoryUseCase(get()) }
    factory { SavePlatformUseCase(get()) }
    factory { RenamePlatformUseCase(get()) }
    factory { ArchivePlatformUseCase(get()) }
    factory { LinkPlatformToCategoryUseCase(get()) }
    factory { UnlinkPlatformFromCategoryUseCase(get()) }
    factory { CreateAndLinkPlatformUseCase(get(), get()) }
    // ── Category (solo gastos) ────────────────────────────────────────────────
    factory { GetCategoriesByTypeUseCase(get()) }
    factory { GetAllCategoriesIncludingArchivedUseCase(get()) }
    factory { SeedDefaultCategoriesUseCase(get()) }
    // ── Budget / Presupuestos ──────────────────────────────────────────────────
    factory { GetCategoryBudgetStatusUseCase(get(), get(), get()) }
    // ── Issuer ────────────────────────────────────────────────────────────────
    factory { GetIssuersUseCase(get()) }
    factory { GetAllIssuersIncludingArchivedUseCase(get()) }
    factory { SaveIssuerUseCase(get()) }
    factory { CreateIssuerUseCase(get()) }
    factory { RenameIssuerUseCase(get()) }
    factory { ArchiveIssuerUseCase(get()) }
    // ── Backup ──────────────────────────────────────────────────────────────────
    factory { SaveLastBackupDateUseCase(get()) }
    factory { GetLastBackupDateUseCase(get()) }
    factory { GetBackupReminderIntervalUseCase(get()) }
    factory { SaveBackupReminderIntervalUseCase(get()) }
    factory { SaveBackupReminderDismissedUseCase(get()) }
    factory { ShouldShowBackupReminderUseCase(get()) }
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
    factory { GetFixedIncomeRowsByCategoryUseCase(get(), get()) }
    factory { DeleteLinkedTransactionUseCase(get()) }
    factory { DeleteFixedIncomeEventUseCase(get(), get()) }
    // ── Fiscal ────────────────────────────────────────────────────────────────
    factory { GetFiscalReportDataUseCase(get(), get(), get(), get(), get(), get()) }
    factory { CalculateIrpfUseCase() }
    factory { CalculateNetIncomeUseCase(get()) }

    // ── Loan ─────────────────────────────────────────────────────────────────────
    factory { SaveLoanUseCase(get()) }
    factory { UpdateLoanUseCase(get()) }
    factory { UpdateLoanRateUseCase(get(), get()) }
    factory { GetLoansByAccountUseCase(get()) }
    factory { GetAmortizationScheduleUseCase(get(), get()) }
    factory { ArchiveLoanUseCase(get()) }

    // ── Real Estate ──────────────────────────────────────────────────────────────
    factory { SavePropertyUseCase(get(), get()) }
    factory { UpdatePropertyValueUseCase(get()) }
    factory { ArchivePropertyUseCase(get()) }
    factory { GetPropertiesByAccountUseCase(get()) }
    factory { DismissMortgageReminderUseCase(get()) }
    factory { ChangeRentalStatusUseCase(get(), get()) }
    factory { GetRentalPeriodsUseCase(get()) }
    factory { GetTransactionsByPropertyUseCase(get()) }
    factory { GetPropertyFinancialSummaryUseCase(get()) }
    factory { LinkLoanUseCase(get()) }
    factory { SellPropertyUseCase(get(), get()) }

    // ── Valuable (Bienes) ────────────────────────────────────────────────────────
    factory { SaveValuableUseCase(get(), get()) }
    factory { SellValuableUseCase(get(), get()) }
    factory { GetValuablesByAccountUseCase(get()) }
    factory { GetAllValuablesByAccountUseCase(get()) }
    factory { GetValuableDetailUseCase(get(), get(), get()) }
    factory { UpdateValuableEstimatedValueUseCase(get()) }
    factory { LinkLoanToValuableUseCase(get()) }
    factory { DeleteValuableUseCase(get(), get()) }

    // ── Net Worth ────────────────────────────────────────────────────────────────
    factory { GetNetWorthDataUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { GetPortfolioValueHistoryUseCase(get(), get(), get(), get()) }
    factory { GetNetWorthHistoryUseCase(get(), get(), get(), get(), get(), get()) }

    // ── Tax Profile ───────────────────────────────────────────────────────────────
    factory { GetAllTaxProfileSnapshotsUseCase(get()) }
    factory { GetActiveTaxProfileSnapshotUseCase(get()) }
    factory { SaveTaxProfileSnapshotUseCase(get()) }
    factory { DeleteTaxProfileSnapshotUseCase(get()) }

    // ── Version (Remote Config) ──────────────────────────────────────────────────
    factory { GetVersionInfoUseCase(get()) }
    factory { DismissVersionBannerUseCase(get()) }
    single { VersionManager(get(), get(), get(named("appVersion"))) }

    // ── ViewModels ────────────────────────────────────────────────────────────
    viewModel { (accountId: String) ->
        AccountConfigViewModel(
            accountId = accountId,
            accountRepository = get(),
            getReminderInterval = get()
        )
    }
    viewModel {
        AccountViewModel(
            getAccounts = get(),
            saveAccount = get(),
            updateAccount = get(),
            deleteAccount = get(),
            setInitialBalance = get(),
            seedCategories = get(),
            session = get(),
            premiumManager = get()
        )
    }
    viewModel {
        HomeViewModel(
            getHomeBalance = get(),
            getCategoriesByType = get(),
            setInitialBalance = get(),
            session = get(),
            shouldShowPriceReminder = get(),
            getOutdatedAssets = get(),
            updateAssetCurrentPrice = get(),
            savePriceReminderShown = get(),
            getNearMaturityPositions = get(),
            loadingManager = get(),
            getPortfolioValueHistory = get(),
            versionManager = get(),
            getCurrentMonthProgress = get(),
            getEmergencyFundStatus = get(),
            getCategoryBudgetStatus = get(),
            categoryBudgetRepository = get()
        )
    }
    viewModel {
        AddTransactionViewModel(
            saveTransaction = get(),
            updateTransaction = get(),
            getCategoriesByType = get(),
            getIssuers = get(),
            getActiveTaxProfile = get(),
            calculateIrpf = get(),
            calculateNetIncome = get(),
            session = get()
        )
    }
    viewModel {
        TransactionViewModel(
            getTransactionsByMonth = get(),
            getMonthlyTotals = get(),
            deleteTransactionUseCase = get(),
            getAllCategoriesIncludingArchived = get(),
            getOldestDate = get(),
            session = get()
        )
    }
    viewModel { (transactionId: String) ->
        TransactionDetailViewModel(
            transactionId = transactionId,
            getTransactionById = get(),
            getAccountById = get(),
            getAllCategoriesIncludingArchived = get(),
            deleteTransactionUseCase = get()
        )
    }
    viewModel { DebtViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ReconciliationViewModel(get(), get(), get(), get()) }
    viewModel {
        AnnualViewModel(
            getAnnualSummary = get(),
            getMonthlyBreakdown = get(),
            getMonthlyTotals = get(),
            getOldestDate = get(),
            getExpensesByCategory = get(),
            getExpensesByCategoryByMonth = get(),
            getIncomeByType = get(),
            getIncomeByTypeByMonth = get(),
            getMonthlyInvestments = get(),
            getYearlyGoalProgress = get(),
            getCategoryBudgetStatus = get(),
            session = get()
        )
    }
    factory { PortfolioStateBuilder() }
    viewModel {
        PortfolioViewModel(
            getAssetsByAccount = get(),
            updateAsset = get(),
            updateAssetCurrentPrice = get(),
            detectAnomaly = get(),
            getAssetCategoriesIncludingArchived = get(),
            getAccountById = get(),
            getTransactionsByAccount = get(),
            getPlatforms = get(),
            saveAssetTransaction = get(),
            syncToLedger = get(),
            saveAssetPriceHistory = get(),
            assetPlatformRepository = get(),
            assetMetadataRepository = get(),
            session = get(),
            getFixedIncomeSummary = get(),
            getNearMaturityPositions = get(),
            createFixedIncomePosition = get(),
            getBondIssuers = get(),
            createIssuer = get(),
            getPortfolioValueHistory = get(),
            registerCoupon = get(),
            getPortfoliosByAccount = get(),
            getDividendsByAssetIds = get(),
            stateBuilder = get(),
        )
    }
    viewModel {
        PortfolioSettingsViewModel(
            session = get(),
            getPortfoliosByAccount = get(),
        )
    }
    viewModel {
        AssetCatalogViewModel(
            getAssetsByAccount = get(),
            saveAsset = get(),
            updateAsset = get(),
            archiveAsset = get(),
            unarchiveAsset = get(),
            assetTransactionRepository = get(),
            getAssetCategoriesIncludingArchived = get(),
            assetPlatformRepository = get(),
            assetMetadataRepository = get(),
            session = get(),
            validateAssetIdentifier = get()
        )
    }
    viewModel {
        AssetCategoryViewModel(
            getCategories = get(),
            saveCategory = get(),
            renameCategory = get(),
            archiveCategory = get()
        )
    }
    viewModel {
        PlatformViewModel(
            getPlatforms = get(),
            savePlatform = get(),
            renamePlatform = get(),
            archivePlatform = get()
        )
    }
    viewModel { (assetId: String) ->
        AssetHistoryViewModel(
            assetId = assetId,
            getAssetById = get(),
            getTransactionsByAsset = get(),
            getPlatforms = get(),
            platformCategoryRepository = get(),
            assetPlatformRepository = get(),
            getAccountById = get(),
            getAssetCategoriesIncludingArchived = get(),
            saveAssetTransaction = get(),
            updateAssetTransaction = get(),
            deleteAssetTransaction = get(),
            updateAssetCurrentPrice = get(),
            assetPriceHistoryRepository = get(),
            syncToLedger = get(),
            transactionRepository = get(),
            executeFundTransfer = get(),
            assetRepository = get()
        )
    }
    viewModel { (categoryId: String) ->
        AssetCategoryDetailViewModel(
            categoryId = categoryId,
            getAllAssetsIncludingArchived = get(),
            getAssetCategoriesIncludingArchived = get(),
            getPlatforms = get(),
            getPlatformsByCategory = get(),
            saveAssetWithMetadata = get(),
            updateAssetWithMetadata = get(),
            getAssetEditMetadata = get(),
            archiveAsset = get(),
            unarchiveAsset = get(),
            checkAssetArchivable = get(),
            getFixedIncomeRowsByCategory = get(),
            linkPlatformToCategory = get(),
            unlinkPlatformFromCategory = get(),
            createAndLinkPlatform = get(),
            session = get(),
            validateAssetIdentifier = get()
        )
    }
    viewModel { (assetId: String) ->
        AssetDetailViewModel(
            assetId = assetId,
            assetRepository = get(),
            assetPlatformRepository = get(),
            getPlatforms = get(),
            savePlatform = get()
        )
    }
    viewModel {
        BackupViewModel(
            backupManager = get(),
            saveLastBackupDate = get(),
            shouldShowBackupReminder = get(),
            getLastBackupDate = get(),
            getBackupReminderInterval = get(),
            saveBackupReminderInterval = get(),
            saveBackupReminderDismissed = get()
        )
    }
    viewModel { CategoryViewModel(get(), get(), get()) }
    viewModel {
        IssuerViewModel(
            getIssuers = get(),
            saveIssuer = get(),
            renameIssuer = get(),
            archiveIssuer = get(),
            session = get()
        )
    }

    viewModel {
        FiscalReportViewModel(
            getFiscalReportData = get(),
            getActiveTaxProfile = get(),
            pdfGenerator = get(),
            session = get()
        )
    }
    viewModel { (positionId: String) ->
        FixedIncomeDetailViewModel(
            positionId = positionId,
            getPositionDetail = get(),
            getCouponSchedule = get(),
            registerCoupon = get(),
            closeFixedIncome = get(),
            deleteFixedIncomeEvent = get(),
            updatePosition = get(),
            archivePosition = get(),
            transactionRepository = get()
        )
    }

    // ── NetWorth ViewModel ─────────────────────────────────────────────────────
    viewModel {
        NetWorthViewModel(
            getNetWorthData = get(),
            getLoansByAccount = get(),
            getNetWorthHistory = get(),
            session = get(),
            loadingManager = get()
        )
    }

    // ── Loan ViewModel ─────────────────────────────────────────────────────────
    viewModel { (loanId: String) ->
        LoanDetailViewModel(
            loanId = loanId,
            loanRepository = get(),
            getAmortizationSchedule = get(),
            updateLoanRate = get(),
            updateLoan = get(),
            archiveLoan = get(),
            rateChangeRepository = get()
        )
    }

    // ── Real Estate ViewModel ──────────────────────────────────────────────────
    viewModel { (propertyId: String) ->
        RealEstateDetailViewModel(
            propertyId = propertyId,
            propertyRepository = get(),
            savePropertyUseCase = get(),
            updatePropertyValue = get(),
            archiveProperty = get(),
            getRentalPeriods = get(),
            getTransactionsByProperty = get(),
            getFinancialSummary = get(),
            changeRentalStatus = get(),
            dismissMortgageReminder = get(),
            linkLoanUseCase = get(),
            getLoan = get(),
            sellPropertyUseCase = get()
        )
    }

    viewModel { (initialTypeName: String) ->
        CategoryPickerViewModel(
            initialTypeName,
            get(),
            get()
        )
    }

    viewModel { FeedbackViewModel(get()) }

    // ── Goal Settings ────────────────────────────────────────────────────────────
    viewModel {
        GoalSettingsViewModel(
            goalRepository = get(),
            session = get()
        )
    }

    // ── Emergency Fund Settings ──────────────────────────────────────────────────
    viewModel {
        EmergencyFundSettingsViewModel(
            getEmergencyFund = get(),
            saveEmergencyFund = get(),
            getCategoriesByType = get(),
            session = get()
        )
    }

    // ── Tax Profile Settings ──────────────────────────────────────────────────────
    viewModel {
        TaxProfileSettingsViewModel(
            getAll = get(),
            save = get(),
            delete = get()
        )
    }

    // ── Valuable ViewModels ──────────────────────────────────────────────────────
    viewModel { (valuableId: String) ->
        ValuableDetailViewModel(
            valuableId = valuableId,
            getDetail = get(),
            saveValuableUseCase = get(),
            sellValuableUseCase = get(),
            deleteValuableUseCase = get(),
            updateEstimatedValue = get(),
            linkLoanToValuable = get(),
            loanRepository = get()
        )
    }

    viewModel {
        ValuableListViewModel(
            getAllValuables = get(),
            deleteValuable = get(),
            saveValuable = get(),
            session = get()
        )
    }

    // ── Savings Rates ────────────────────────────────────────────────────────────
    factory { GetSavingsRatesUseCase(get()) }
    viewModel { SavingsRatesViewModel(get()) }

    // ── Security / Lock ──────────────────────────────────────────────────────────
    viewModel { LockViewModel(get()) }

    // ── About ─────────────────────────────────────────────────────────────────────
    viewModel { AboutViewModel(get()) }
}
