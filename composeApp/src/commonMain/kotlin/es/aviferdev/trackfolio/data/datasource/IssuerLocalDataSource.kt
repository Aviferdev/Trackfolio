package es.aviferdev.trackfolio.data.datasource

import es.aviferdev.trackfolio.domain.model.Issuer
import es.aviferdev.trackfolio.domain.model.IssuerType
import kotlinx.coroutines.flow.Flow

/**
 * DataSource unificado para las 5 tablas de entidades emisoras.
 * Despacha las operaciones a la tabla correcta según [IssuerType].
 */
interface IssuerLocalDataSource {
    fun getByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>>
    fun getAllByAccount(accountId: String, type: IssuerType): Flow<List<Issuer>>
    fun getById(id: String, type: IssuerType): Flow<Issuer?>
    suspend fun insert(issuer: Issuer): Result<Unit>
    suspend fun updateName(id: String, name: String, icon: String, type: IssuerType): Result<Unit>
    suspend fun archive(id: String, type: IssuerType): Result<Unit>
}
