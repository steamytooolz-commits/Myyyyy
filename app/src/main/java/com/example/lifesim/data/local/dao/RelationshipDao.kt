package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.RelationshipEntity
import com.example.lifesim.data.local.entity.RelationType
import com.example.lifesim.data.local.entity.RelationshipStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationshipDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: RelationshipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationships(relationships: List<RelationshipEntity>)

    @Update
    suspend fun updateRelationship(relationship: RelationshipEntity)

    @Delete
    suspend fun deleteRelationship(relationship: RelationshipEntity)

    @Query("SELECT * FROM relationships WHERE relationshipId = :relationshipId")
    suspend fun getRelationshipById(relationshipId: String): RelationshipEntity?

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND relationshipStatus = 'ACTIVE'")
    fun observeActiveRelationships(characterId: String): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND relationshipStatus = 'ACTIVE'")
    suspend fun getActiveRelationships(characterId: String): List<RelationshipEntity>

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND targetCharacterId = :targetId")
    suspend fun getRelationshipBetween(characterId: String, targetId: String): RelationshipEntity?

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND relationType = :relationType AND relationshipStatus = 'ACTIVE'")
    suspend fun getRelationshipsByType(characterId: String, relationType: RelationType): List<RelationshipEntity>

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND relationshipStatus = 'ACTIVE' ORDER BY affection DESC")
    fun observeRelationshipsByAffection(characterId: String): Flow<List<RelationshipEntity>>

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND relationshipStatus = 'ACTIVE' AND affection > :threshold")
    suspend fun getCloseRelationships(characterId: String, threshold: Int = 70): List<RelationshipEntity>

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND relationshipStatus = 'ACTIVE' AND fear > :threshold")
    suspend fun getFearedRelationships(characterId: String, threshold: Int = 60): List<RelationshipEntity>

    @Query("UPDATE relationships SET affection = :affection, trust = :trust, jealousy = :jealousy WHERE relationshipId = :relationshipId")
    suspend fun updateDynamics(relationshipId: String, affection: Int, trust: Int, jealousy: Int)

    @Query("UPDATE relationships SET relationshipStatus = :status WHERE relationshipId = :relationshipId")
    suspend fun updateStatus(relationshipId: String, status: RelationshipStatus)

    @Query("UPDATE relationships SET interactionCount = interactionCount + 1, lastInteractionTick = :tick WHERE relationshipId = :relationshipId")
    suspend fun recordInteraction(relationshipId: String, tick: Long)

    @Query("UPDATE relationships SET favorsOwed = favorsOwed + :amount WHERE relationshipId = :relationshipId")
    suspend fun adjustFavorsOwed(relationshipId: String, amount: Int)

    @Query("UPDATE relationships SET favorsGiven = favorsGiven + :amount WHERE relationshipId = :relationshipId")
    suspend fun adjustFavorsGiven(relationshipId: String, amount: Int)

    @Query("SELECT COUNT(*) FROM relationships WHERE ownerCharacterId = :characterId AND relationshipStatus = 'ACTIVE'")
    suspend fun getActiveRelationshipCount(characterId: String): Int

    @Query("SELECT * FROM relationships WHERE ownerCharacterId = :characterId AND (affection < :lowAffection OR jealousy > :highJealousy) AND relationshipStatus = 'ACTIVE'")
    suspend fun getToxicRelationships(characterId: String, lowAffection: Int = 30, highJealousy: Int = 70): List<RelationshipEntity>
}
