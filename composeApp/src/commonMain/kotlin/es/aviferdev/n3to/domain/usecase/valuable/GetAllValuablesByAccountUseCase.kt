package es.aviferdev.n3to.domain.usecase.valuable

import es.aviferdev.n3to.domain.model.Valuable
import es.aviferdev.n3to.domain.repository.ValuableRepository
import kotlinx.coroutines.flow.Flow

class GetAllValuablesByAccountUseCase(
    private val repository: ValuableRepository
) {
    /** Obtiene todos los bienes (activos y vendidos) de una cuenta. */
    operator fun invoke(accountId: String): Flow<List<Valuable>> =
        repository.getByAccount(accountId)
}
