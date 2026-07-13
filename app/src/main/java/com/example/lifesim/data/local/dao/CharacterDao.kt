package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity)

    @Update
    suspend fun updateCharacter(character: CharacterEntity)

    @Delete
    suspend fun deleteCharacter(character: CharacterEntity)

    @Query("SELECT * FROM characters WHERE characterId = :characterId")
    suspend fun getCharacterById(characterId: String): CharacterEntity?

    @Query("SELECT * FROM characters WHERE characterId = :characterId")
    fun observeCharacter(characterId: String): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters WHERE isAlive = 1")
    suspend fun getAllLivingCharacters(): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE isAlive = 1")
    fun observeAllLivingCharacters(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE dynastyId = :dynastyId")
    suspend fun getCharactersByDynasty(dynastyId: String): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%'")
    suspend fun searchCharacters(query: String): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE isInPrison = 1")
    suspend fun getAllIncarceratedCharacters(): List<CharacterEntity>

    @Query("UPDATE characters SET isAlive = 0, lastUpdatedAt = :timestamp WHERE characterId = :characterId")
    suspend fun markAsDeceased(characterId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE characters SET cash = cash + :amount WHERE characterId = :characterId")
    suspend fun adjustCash(characterId: String, amount: Double)

    @Query("UPDATE characters SET stress = stress + :delta WHERE characterId = :characterId")
    suspend fun adjustStress(characterId: String, delta: Double)

    @Query("UPDATE characters SET health = health + :delta WHERE characterId = :characterId")
    suspend fun adjustHealth(characterId: String, delta: Double)

    @Query("SELECT * FROM characters ORDER BY totalNetWorth DESC")
    fun observeCharactersByWealth(): Flow<List<CharacterEntity>>
}
