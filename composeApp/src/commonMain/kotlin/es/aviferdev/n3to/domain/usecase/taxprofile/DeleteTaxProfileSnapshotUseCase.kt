package es.aviferdev.n3to.domain.usecase.taxprofile

import es.aviferdev.n3to.domain.repository.TaxProfileSnapshotRepository

class DeleteTaxProfileSnapshotUseCase(private val repository: TaxProfileSnapshotRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.delete(id)
}
