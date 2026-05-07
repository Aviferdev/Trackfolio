package es.aviferdev.trackfolio.domain.usecase.transaction

import es.aviferdev.trackfolio.domain.repository.TransactionRepository
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
