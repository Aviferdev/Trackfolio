package es.aviferdev.n3to.domain.usecase.taxprofile

import es.aviferdev.n3to.domain.model.TaxProfileSnapshot
import es.aviferdev.n3to.domain.repository.TaxProfileSnapshotRepository
import kotlinx.coroutines.flow.Flow

class GetAllTaxProfileSnapshotsUseCase(private val repository: TaxProfileSnapshotRepository) {
    operator fun invoke(): Flow<List<TaxProfileSnapshot>> = repository.getAll()
}
