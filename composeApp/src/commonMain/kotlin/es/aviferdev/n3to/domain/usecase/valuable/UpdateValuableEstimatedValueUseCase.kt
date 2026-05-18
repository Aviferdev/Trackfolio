package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.domain.model.ValidationError
import es.aviferdev.n3to.domain.repository.ValuableRepository

class UpdateValuableEstimatedValueUseCase(
    private val repository: ValuableRepository
) {
    suspend operator fun invoke(valuableId: String, newValue: Double): Result<Unit> {
        if (newValue <= 0) return Result.failure(ValidationError.ValuablePurchasePriceInvalid)
        return repository.updateEstimatedValue(valuableId, newValue)
    }
}
