package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(
    tableName = "memories",
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
data class MemoryEntity(
    @PrimaryKey val memoryId: String = java.util.UUID.randomUUID().toString(),
    val characterId: String,

    // Temporal info
    val yearOccurred: Int,
    val ageOccurred: Int,
    val tickOccurred: Long,

    // Event classification
    val eventType: MemoryEventType,
    val description: String,
    val category: String = "general",

    // Emotional weight
    val emotionalValence: Double, // -1.0 to 1.0
    val emotionalIntensity: Double, // 0.0 to 1.0

    // Tagging for consequence engine
    val tags: List<String> = emptyList(),

    // Memory state
    val isRepressed: Boolean = false,
    val decayRate: Double = 0.05, // how fast it fades per tick
    val currentIntensity: Double = emotionalIntensity,

    // Resolution
    val isResolved: Boolean = false,
    val resolutionYear: Int? = null,
    val resolutionMethod: String? = null,

    // Associated data
    val involvedCharacterIds: List<String> = emptyList(),
    val locationId: String? = null,

    // Memory decay tracking
    val lastRecalledTick: Long = tickOccurred,
    val recallCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class MemoryEventType {
    TRAUMA, TRIUMPH, MUNDANE, CRIME, ROMANCE, LOSS, DISCOVERY, FAILURE,
    MILESTONE, ACCIDENT, BETRAYAL, RECONCILIATION, ADVENTURE, LEARNING
}
