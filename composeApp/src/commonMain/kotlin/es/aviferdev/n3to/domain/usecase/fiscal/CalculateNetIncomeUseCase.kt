package es.aviferdev.n3to.domain.usecase.fiscal

import es.aviferdev.n3to.domain.model.IncomeType

class CalculateNetIncomeUseCase(private val calculateIrpf: CalculateIrpfUseCase) {
    fun calculate(
        incomeType: IncomeType,
        gross: Double,
        ssAmount: Double,
        commission: Double,
        isIrpfPercentMode: Boolean,
        irpfPercent: String,
        irpfFixed: String
    ): Double = when {
        incomeType.hasSocialContribution && incomeType != IncomeType.BOND_DEPOSIT -> {
            val irpf = calculateIrpf.resolveAmount(gross, ssAmount, isIrpfPercentMode, irpfPercent, irpfFixed)
            gross - ssAmount - irpf
        }
        incomeType == IncomeType.BOND_DEPOSIT -> {
            val irpf = calculateIrpf.resolveAmount(gross, 0.0, isIrpfPercentMode, irpfPercent, irpfFixed)
            gross - irpf - commission
        }
        incomeType == IncomeType.EXEMPT_INCOME -> gross
        else -> {
            val irpf = calculateIrpf.resolveAmount(gross, 0.0, isIrpfPercentMode, irpfPercent, irpfFixed)
            gross - irpf
        }
    }
}
