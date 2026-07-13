package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.MemoryEntity
import com.example.lifesim.data.local.entity.MemoryEventType
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("SELECT * FROM memories WHERE memoryId = :memoryId")
    suspend fun getMemoryById(memoryId: String): MemoryEntity?

    @Query("SELECT * FROM memories WHERE characterId = :characterId")
    fun observeMemories(characterId: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE characterId = :characterId ORDER BY emotionalIntensity DESC LIMIT :limit")
    suspend fun getMostIntenseMemories(characterId: String, limit: Int = 10): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE characterId = :characterId AND isRepressed = 0 ORDER BY yearOccurred DESC")
    fun observeActiveMemories(characterId: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE characterId = :characterId AND eventType = :eventType")
    suspend fun getMemoriesByType(characterId: String, eventType: MemoryEventType): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE characterId = :characterId AND isResolved = 0 AND isRepressed = 0")
    suspend fun getUnresolvedMemories(characterId: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE characterId = :characterId AND tags LIKE '%' || :tag || '%'")
    suspend fun getMemoriesByTag(characterId: String, tag: String): List<MemoryEntity>

    @Query("SELECT * FROM memories WHERE characterId = :characterId ORDER BY yearOccurred DESC LIMIT :limit")
    suspend fun getRecentMemories(characterId: String, limit: Int = 5): List<MemoryEntity>

    @Query("UPDATE memories SET isResolved = 1, resolutionYear = :year, resolutionMethod = :method WHERE memoryId = :memoryId")
    suspend fun resolveMemory(memoryId: String, year: Int, method: String)

    @Query("UPDATE memories SET currentIntensity = currentIntensity - :decayAmount, lastRecalledTick = :currentTick WHERE memoryId = :memoryId")
    suspend fun decayMemory(memoryId: String, decayAmount: Double, currentTick: Long)

    @Query("SELECT COUNT(*) FROM memories WHERE characterId = :characterId AND eventType = 'TRAUMA' AND isResolved = 0")
    suspend fun countUnresolvedTraumas(characterId: String): Int

    @Query("DELETE FROM memories WHERE characterId = :characterId AND currentIntensity <= 0")
    suspend fun purgeFadedMemories(characterId: String)
}
