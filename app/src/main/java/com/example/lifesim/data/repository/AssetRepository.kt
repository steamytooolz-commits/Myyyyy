// =========================================
// File: data/repository/AssetRepository.kt
// =========================================
package com.example.lifesim.data.repository

import com.example.lifesim.data.local.dao.AssetDao
import com.example.lifesim.data.local.entity.AssetEntity
import com.example.lifesim.data.local.entity.AssetType
import com.example.lifesim.domain.usecase.NetWorthResult
import javax.inject.Inject
import javax.inject.Singleton

interface AssetRepository {
    suspend fun getNetWorth(characterId: String): NetWorthResult
    suspend fun addAsset(asset: AssetEntity)
    suspend fun removeAsset(assetId: String)
    suspend fun getAssetsByType(characterId: String, type: AssetType): List<AssetEntity>
}

@Singleton
class AssetRepositoryImpl @Inject constructor(
    private val assetDao: AssetDao
) : AssetRepository {

    override suspend fun getNetWorth(characterId: String): NetWorthResult {
        val totalAssets = assetDao.getTotalAssetValue(characterId) ?: 0.0
        val liquidCash = assetDao.getTotalLiquidValue(characterId) ?: 0.0
        return NetWorthResult(totalNetWorth = liquidCash + totalAssets, liquidCash = liquidCash, totalAssetsValue = totalAssets)
    }

    override suspend fun addAsset(asset: AssetEntity) = assetDao.insertAsset(asset)

    override suspend fun removeAsset(assetId: String) {
        val asset = assetDao.getAssetById(assetId) ?: return
        assetDao.deleteAsset(asset)
    }

    override suspend fun getAssetsByType(characterId: String, type: AssetType) =
        assetDao.getAssetsByType(characterId, type)
}
