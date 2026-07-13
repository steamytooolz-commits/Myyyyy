package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(tableName = "dynasties")
@TypeConverters(Converters::class)
data class DynastyEntity(
    @PrimaryKey val dynastyId: String = java.util.UUID.randomUUID().toString(),
    val familyName: String,
    val foundingCharacterId: String,
    val foundingYear: Int,
    val totalWealth: Double = 0.0,
    val familyReputation: Int = 0, // -1000 to 1000
    val legacyPoints: Int = 0,
    val activeMemberIds: List<String> = emptyList(),
    val deceasedMemberIds: List<String> = emptyList(),
    val heirApparentId: String? = null,
    val familyTraits: List<String> = emptyList(),
    val familySecrets: List<String> = emptyList(),
    val currentGeneration: Int = 1,
    val totalMembersEver: Int = 1,
    val familyCrestUrl: String? = null,
    val familyMotto: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)
