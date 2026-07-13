package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(
    tableName = "assets",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["characterId"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("characterId")]
)
@TypeConverters(Converters::class)
data class AssetEntity(
    @PrimaryKey val assetId: String = java.util.UUID.randomUUID().toString(),
    val characterId: String,
    val assetType: AssetType,
    val name: String,
    val currentValue: Double,
    val purchasePrice: Double,
    val purchaseYear: Int,
    val depreciationRate: Double = 0.0,
    val maintenanceCost: Double = 0.0,
    val isInsured: Boolean = false,
    val mortgageBalance: Double = 0.0,
    val incomeGenerated: Double = 0.0,
    val locationId: String? = null,
    val conditionScore: Int = 100, // 0-100 for physical assets
    val isLiquid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AssetType {
    CASH, BANK_ACCOUNT, STOCK, CRYPTO, REAL_ESTATE, VEHICLE,
    BUSINESS, ART, JEWELRY, COLLECTIBLE, INTELLECTUAL_PROPERTY,
    RETIREMENT_ACCOUNT, BOND, PRECIOUS_METALS
}
