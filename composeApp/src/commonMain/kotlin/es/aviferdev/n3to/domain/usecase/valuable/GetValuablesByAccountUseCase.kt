package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.Flow

class GetValuablesByAccountUseCase(
    private val repository: ValuableRepository
) {
    /** Obtiene solo los bienes activos (no vendidos) de una cuenta. */
    operator fun invoke(accountId: String): Flow<List<Valuable>> =
        repository.getActiveByAccount(accountId)
}
