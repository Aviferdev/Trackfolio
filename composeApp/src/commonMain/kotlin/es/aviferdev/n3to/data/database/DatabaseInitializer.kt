package es.aviferdev.n3to.data.database

import es.aviferdev.n3to.data.datasource.asset.AssetCategoryLocalDataSource
import es.aviferdev.n3to.data.datasource.assetmetadata.AssetMetadataLocalDataSource
import es.aviferdev.n3to.domain.model.AssetCategory
import es.aviferdev.n3to.domain.model.AssetRegion
import es.aviferdev.n3to.domain.model.AssetSector
import es.aviferdev.n3to.domain.model.Category
import es.aviferdev.n3to.domain.model.TransactionType
import kotlinx.coroutines.flow.firstOrNull

class DatabaseInitializer(
    private val assetCategoryDataSource: AssetCategoryLocalDataSource,
    private val assetMetadataDataSource: AssetMetadataLocalDataSource? = null,
    private val migrationHelper: DatabaseMigrationHelper? = null,
) {
    companion object {

        val DEFAULT_ASSET_CATEGORIES = listOf(
            AssetCategory(
                id = "fixed_cat_stocks",
                name = "Acciones",
                icon = "📊",
                sortOrder = 0,
                isQuotable = true,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_etfs",
                name = "ETFs",
                icon = "📈",
                sortOrder = 1,
                isQuotable = true,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_funds",
                name = "Fondos de inversión",
                icon = "💼",
                sortOrder = 2,
                isQuotable = true,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_crypto",
                name = "Criptomonedas",
                icon = "₿",
                sortOrder = 3,
                isQuotable = true,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_pensions",
                name = "Planes de pensiones",
                icon = "🛡",
                sortOrder = 4,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_fixedincome",
                name = "Renta fija",
                icon = "🏦",
                sortOrder = 5,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_commodities",
                name = "Materias primas",
                icon = "🪙",
                sortOrder = 6,
                isQuotable = true,
                createdAt = 0
            ),
            AssetCategory(
                id = "fixed_cat_crowdlending",
                name = "Crowdlending",
                icon = "🤝",
                sortOrder = 7,
                createdAt = 0
            ),
        )

        val DEFAULT_ASSET_SECTORS = listOf(
            AssetSector(id = "sector_tech", name = "Tecnología", icon = "💻", createdAt = 0),
            AssetSector(id = "sector_health", name = "Salud", icon = "🏥", createdAt = 0),
            AssetSector(id = "sector_energy", name = "Energía", icon = "⚡", createdAt = 0),
            AssetSector(id = "sector_finance", name = "Financiero", icon = "🏦", createdAt = 0),
            AssetSector(id = "sector_consumer", name = "Consumo", icon = "🛒", createdAt = 0),
            AssetSector(id = "sector_industrial", name = "Industrial", icon = "🏭", createdAt = 0),
            AssetSector(id = "sector_realestate", name = "Inmobiliario", icon = "🏠", createdAt = 0),
            AssetSector(
                id = "sector_telecom",
                name = "Telecomunicaciones",
                icon = "📡",
                createdAt = 0
            ),
            AssetSector(
                id = "sector_materials",
                name = "Materias Primas",
                icon = "🪙",
                createdAt = 0
            ),
            AssetSector(
                id = "sector_utilities",
                name = "Servicios Públicos",
                icon = "💡",
                createdAt = 0
            ),
        )

        val DEFAULT_ASSET_REGIONS = listOf(
            AssetRegion(id = "region_usa", name = "EE.UU.", createdAt = 0),
            AssetRegion(id = "region_europe", name = "Europa", createdAt = 0),
            AssetRegion(id = "region_asia", name = "Asia", createdAt = 0),
            AssetRegion(id = "region_latam", name = "Latinoamérica", createdAt = 0),
            AssetRegion(id = "region_emerging", name = "Mercados Emergentes", createdAt = 0),
            AssetRegion(id = "region_global", name = "Global", createdAt = 0),
            AssetRegion(id = "region_spain", name = "España", createdAt = 0),
        )

        fun defaultExpenseCategories(accountId: String): List<Category> = listOf(
            Category(
                id = "cat_exp_01",
                accountId = accountId,
                name = "Alimentación",
                type = TransactionType.EXPENSE,
                isDefault = true,
                archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_02",
                accountId = accountId,
                name = "Transporte",
                type = TransactionType.EXPENSE,
                isDefault = true,
                archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_03", accountId = accountId,
                name = "Hogar", type = TransactionType.EXPENSE, isDefault = true, archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_04", accountId = accountId,
                name = "Salud", type = TransactionType.EXPENSE, isDefault = true, archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_05", accountId = accountId,
                name = "Ocio", type = TransactionType.EXPENSE, isDefault = true, archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_06", accountId = accountId,
                name = "Ropa", type = TransactionType.EXPENSE, isDefault = true, archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_07",
                accountId = accountId,
                name = "Educación",
                type = TransactionType.EXPENSE,
                isDefault = true,
                archived = false,
                createdAt = 0L
            ),
            Category(
                id = "cat_exp_08", accountId = accountId,
                name = "Otros", type = TransactionType.EXPENSE, isDefault = true, archived = false,
                createdAt = 0L
            ),
        )

    }

    suspend fun initializeIfNeeded() {
        if (assetCategoryDataSource.count().firstOrNull() == 0L) {
            DEFAULT_ASSET_CATEGORIES.forEach { assetCategoryDataSource.insert(it) }
        }

        assetMetadataDataSource?.let { metadata ->
            if (metadata.countSectors() == 0L) {
                DEFAULT_ASSET_SECTORS.forEach { metadata.insertSector(it) }
            }
            if (metadata.countRegions() == 0L) {
                DEFAULT_ASSET_REGIONS.forEach { metadata.insertRegion(it) }
            }
        }

        // Registrar la versión del esquema como completada
        migrationHelper?.markMigrationComplete()
    }

}
