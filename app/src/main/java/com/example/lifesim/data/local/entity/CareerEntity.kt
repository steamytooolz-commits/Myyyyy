package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(
    tableName = "careers",
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
data class CareerEntity(
    @PrimaryKey val careerId: String = java.util.UUID.randomUUID().toString(),
    val characterId: String,
    val companyName: String,
    val industry: String,
    val jobTitle: String,
    val jobTier: Int, // 1-10
    val baseSalary: Double,
    val currentSalary: Double = baseSalary,
    val performanceScore: Int = 70, // 0-100
    val officePoliticsAlignment: OfficePoliticsAlignment = OfficePoliticsAlignment.NEUTRAL,
    val skillsAcquired: List<String> = emptyList(),
    val yearsEmployed: Int = 0,
    val promotionProgress: Int = 0, // 0-100
    val firedCount: Int = 0,
    val isActive: Boolean = true,
    val startYear: Int,
    val endYear: Int? = null,
    val reasonForLeaving: String? = null,
    val startTick: Long = System.currentTimeMillis()
)

enum class OfficePoliticsAlignment {
    NEUTRAL, BOSS_FAVORITE, UNION_LEADER, REBEL, LONE_WOLF, TEAM_PLAYER, SOCIAL_BUTTERFLY
}
