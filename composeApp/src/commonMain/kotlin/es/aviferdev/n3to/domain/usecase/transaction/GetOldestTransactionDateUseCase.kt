package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Devuelve la fecha (epoch millis) de la transacción más antigua de una cuenta.
 * Incluye tanto movimientos normales como inversiones sincronizadas.
 * Retorna null si no hay transacciones.
 */
class GetOldestTransactionDateUseCase(private val repository: TransactionRepository) {
    operator fun invoke(accountId: String): Flow<Long?> =
        repository.getOldestDate(accountId)
}
