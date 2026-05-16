package es.aviferdev.n3to.domain.repository

import es.aviferdev.n3to.domain.model.Issuer
import es.aviferdev.n3to.domain.model.IssuerType
import kotlinx.coroutines.flow.Flow

interface IssuerRepository {
    fun getByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>>
    fun getAllByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>>
    fun getById(id: String, type: IssuerType): Flow<Issuer?>
    suspend fun save(issuer: Issuer): Result<Unit>
    suspend fun updateName(id: String, name: String, icon: String, type: IssuerType): Result<Unit>
    suspend fun archive(id: String, type: IssuerType): Result<Unit>
}
