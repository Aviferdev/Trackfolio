package es.aviferdev.n3to.ui.home.viewmodel

import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.IncomeType
import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.TransactionType
import kotlinx.coroutines.flow.StateFlow

interface IAddTransactionForm {
    val formUiState: StateFlow<AddTransactionUiState>
    val isEditing: Boolean
    val type: TransactionType
    val amount: String
    val categories: List<Category>
    val selectedCategoryId: String
    val selectedIncomeType: IncomeType?
    val calculatedNet: Double?
    val incomeInputMode: IncomeInputMode
    val netAmount: String
    val grossAmount: String
    val irpfPercent: String
    val irpfFixedAmount: String
    val irpfInputMode: IrpfInputMode
    val socialSecurityAmount: String
    val commissionAmount: String
    val withholdingTaxLabel: String
    val socialContributionLabel: String
    val showWithholdingField: Boolean
    val showSocialContributionField: Boolean
    val issuers: List<Issuer>
    val selectedIssuerId: String?
    val notes: String
    val dateMillis: Long
    val isValid: Boolean

    fun clear()
    fun onTypeChange(newType: TransactionType)
    fun onAmountChange(value: String)
    fun onCategoryChange(categoryId: String)
    fun onIncomeTypeChange(incomeType: IncomeType)
    fun onIncomeModeChange(mode: IncomeInputMode)
    fun onNetAmountChange(value: String)
    fun onGrossAmountChange(value: String)
    fun onSocialSecurityChange(value: String)
    fun onIrpfInputModeChange(mode: IrpfInputMode)
    fun onIrpfPercentChange(value: String)
    fun onIrpfFixedAmountChange(value: String)
    fun onCommissionChange(value: String)
    fun onIssuerSelected(issuerId: String)
    fun onNotesChange(value: String)
    fun onDateChange(millis: Long)
    fun save()
}
