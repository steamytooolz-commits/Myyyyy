package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.DynastyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DynastyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDynasty(dynasty: DynastyEntity)

    @Update
    suspend fun updateDynasty(dynasty: DynastyEntity)

    @Delete
    suspend fun deleteDynasty(dynasty: DynastyEntity)

    @Query("SELECT * FROM dynasties WHERE dynastyId = :dynastyId")
    suspend fun getDynastyById(dynastyId: String): DynastyEntity?

    @Query("SELECT * FROM dynasties WHERE dynastyId = :dynastyId")
    fun observeDynasty(dynastyId: String): Flow<DynastyEntity?>

    @Query("SELECT * FROM dynasties WHERE foundingCharacterId = :characterId")
    suspend fun getDynastyByFounder(characterId: String): DynastyEntity?

    @Query("SELECT * FROM dynasties")
    fun observeAllDynasties(): Flow<List<DynastyEntity>>

    @Query("UPDATE dynasties SET heirApparentId = :heirId WHERE dynastyId = :dynastyId")
    suspend fun updateHeir(dynastyId: String, heirId: String)

    @Query("UPDATE dynasties SET totalWealth = totalWealth + :amount WHERE dynastyId = :dynastyId")
    suspend fun adjustWealth(dynastyId: String, amount: Double)

    @Query("UPDATE dynasties SET familyReputation = familyReputation + :delta WHERE dynastyId = :dynastyId")
    suspend fun adjustReputation(dynastyId: String, delta: Int)

    @Query("UPDATE dynasties SET legacyPoints = legacyPoints + :points WHERE dynastyId = :dynastyId")
    suspend fun addLegacyPoints(dynastyId: String, points: Int)

    @Query("SELECT * FROM dynasties ORDER BY totalWealth DESC")
    fun observeDynastiesByWealth(): Flow<List<DynastyEntity>>

    @Query("SELECT * FROM dynasties ORDER BY familyReputation DESC")
    fun observeDynastiesByReputation(): Flow<List<DynastyEntity>>
}
