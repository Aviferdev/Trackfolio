package es.aviferdev.n3to.domain.usecase.transaction

import es.aviferdev.n3to.domain.model.Transaction
import es.aviferdev.n3to.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetDividendsByAssetIdsUseCase(
    private val repository: TransactionRepository
) {
    operator fun invoke(assetIds: List<String>): Flow<Map<String, List<Transaction>>> {
        if (assetIds.isEmpty()) return flowOf(emptyMap())
        val flows = assetIds.map { assetId ->
            repository.getDividendsByAsset(assetId).map { dividends -> assetId to dividends }
        }
        return combine(flows) { pairs -> pairs.toMap() }
    }
}
