package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.AssetEntity
import com.example.lifesim.data.local.entity.AssetType
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("SELECT * FROM assets WHERE assetId = :assetId")
    suspend fun getAssetById(assetId: String): AssetEntity?

    @Query("SELECT * FROM assets WHERE characterId = :characterId")
    fun observeAssets(characterId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE characterId = :characterId AND assetType = :assetType")
    suspend fun getAssetsByType(characterId: String, assetType: AssetType): List<AssetEntity>

    @Query("SELECT SUM(currentValue) FROM assets WHERE characterId = :characterId")
    suspend fun getTotalAssetValue(characterId: String): Double?

    @Query("SELECT SUM(currentValue) FROM assets WHERE characterId = :characterId AND isLiquid = 1")
    suspend fun getTotalLiquidValue(characterId: String): Double?

    @Query("SELECT * FROM assets WHERE characterId = :characterId AND mortgageBalance > 0")
    suspend fun getMortgagedAssets(characterId: String): List<AssetEntity>

    @Query("UPDATE assets SET currentValue = currentValue + :change WHERE assetId = :assetId")
    suspend fun adjustValue(assetId: String, change: Double)

    @Query("UPDATE assets SET conditionScore = :condition WHERE assetId = :assetId")
    suspend fun updateCondition(assetId: String, condition: Int)

    @Query("DELETE FROM assets WHERE characterId = :characterId AND currentValue <= 0")
    suspend fun purgeWorthlessAssets(characterId: String)
}
