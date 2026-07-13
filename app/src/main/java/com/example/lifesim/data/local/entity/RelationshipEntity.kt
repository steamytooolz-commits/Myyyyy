package com.example.lifesim.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.lifesim.data.local.Converters

@Entity(
    tableName = "relationships",
    foreignKeys = [
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["characterId"],
            childColumns = ["ownerCharacterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CharacterEntity::class,
            parentColumns = ["characterId"],
            childColumns = ["targetCharacterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerCharacterId"), Index("targetCharacterId")]
)
@TypeConverters(Converters::class)
data class RelationshipEntity(
    @PrimaryKey val relationshipId: String = java.util.UUID.randomUUID().toString(),
    val ownerCharacterId: String,
    val targetCharacterId: String,

    // Type
    val relationType: RelationType,

    // Dynamic Stats (0-100)
    val affection: Int = 50,
    val trust: Int = 50,
    val respect: Int = 50,
    val fear: Int = 10,
    val jealousy: Int = 10,
    val lust: Int = 10,
    val forgiveness: Int = 50,

    // Status
    val relationshipStatus: RelationshipStatus = RelationshipStatus.ACTIVE,

    // History
    val sharedMemoryIds: List<String> = emptyList(),
    val interactionCount: Int = 0,
    val lastInteractionTick: Long = 0,
    val firstMetTick: Long = 0,

    // Favor tracking
    val favorsOwed: Int = 0,
    val favorsGiven: Int = 0,

    // Emotional debt
    val emotionalDebt: Double = 0.0,

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class RelationType {
    PARENT, CHILD, SIBLING, SPOUSE, PARTNER, FRIEND, BEST_FRIEND,
    ACQUAINTANCE, ENEMY, RIVAL, COLLEAGUE, BOSS, EMPLOYEE,
    MENTOR, STUDENT, NEIGHBOR, EX_PARTNER, EX_SPOUSE, STRANGER,
    FAMILY_EXTENDED, BUSINESS_PARTNER, CLIENT
}

enum class RelationshipStatus {
    ACTIVE, ESTRANGED, DIVORCED, DECEASED, SEPARATED, DORMANT
}
