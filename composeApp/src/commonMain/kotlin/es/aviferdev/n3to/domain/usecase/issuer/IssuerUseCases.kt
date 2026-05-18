package es.aviferdev.n3to.domain.usecase.issuer

import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import es.aviferdev.n3to.domain.repository.IssuerRepository
import kotlinx.coroutines.flow.Flow

class GetIssuersUseCase(private val repository: IssuerRepository) {
    operator fun invoke(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        repository.getByAccount(accountId, type)
}

class GetAllIssuersIncludingArchivedUseCase(private val repository: IssuerRepository) {
    operator fun invoke(accountId: String, type: IssuerType): Flow<List<Issuer>> =
        repository.getAllByAccount(accountId, type)
}

class SaveIssuerUseCase(private val repository: IssuerRepository) {
    suspend operator fun invoke(issuer: Issuer): Result<Unit> =
        repository.save(issuer)
}

class RenameIssuerUseCase(private val repository: IssuerRepository) {
    suspend operator fun invoke(id: String, name: String, icon: String, type: IssuerType): Result<Unit> =
        repository.updateName(id, name, icon, type)
}

class ArchiveIssuerUseCase(private val repository: IssuerRepository) {
    suspend operator fun invoke(id: String, type: IssuerType): Result<Unit> =
        repository.archive(id, type)
}

class CreateIssuerUseCase(private val repository: IssuerRepository) {
    suspend operator fun invoke(
        accountId: String,
        name: String,
        icon: String,
        type: IssuerType,
        createdAt: Long
    ): Result<Unit> {
        val prefix = when (type) {
            IssuerType.BANK -> "bk"
            else -> "bi"
        }
        val issuer = Issuer(
            id        = "${prefix}_$createdAt",
            accountId = accountId,
            name      = name,
            type      = type,
            icon      = icon,
            archived  = false,
            createdAt = createdAt
        )
        return repository.save(issuer)
    }
}
