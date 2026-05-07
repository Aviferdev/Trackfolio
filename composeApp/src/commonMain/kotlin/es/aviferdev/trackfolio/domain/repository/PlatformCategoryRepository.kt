package es.aviferdev.trackfolio.domain.repository

import es.aviferdev.trackfolio.domain.model.Platform
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para la relación N:M entre plataformas y categorías de activo.
 */
interface PlatformCategoryRepository {
    fun getByCategory(assetCategoryId: String): Flow<List<Platform>>
    fun getByCategoryIncludingArchived(assetCategoryId: String): Flow<List<Platform>>
    fun getCategoriesByPlatform(platformId: String): Flow<List<String>>

    suspend fun link(platformId: String, assetCategoryId: String): Result<Unit>
    suspend fun unlink(platformId: String, assetCategoryId: String): Result<Unit>
    suspend fun unlinkAllByCategory(assetCategoryId: String): Result<Unit>
}
