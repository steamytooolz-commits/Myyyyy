package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.CareerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCareer(career: CareerEntity)

    @Update
    suspend fun updateCareer(career: CareerEntity)

    @Delete
    suspend fun deleteCareer(career: CareerEntity)

    @Query("SELECT * FROM careers WHERE careerId = :careerId")
    suspend fun getCareerById(careerId: String): CareerEntity?

    @Query("SELECT * FROM careers WHERE characterId = :characterId AND isActive = 1")
    suspend fun getActiveCareer(characterId: String): CareerEntity?

    @Query("SELECT * FROM careers WHERE characterId = :characterId")
    fun observeCareers(characterId: String): Flow<List<CareerEntity>>

    @Query("SELECT * FROM careers WHERE characterId = :characterId AND isActive = 1")
    fun observeActiveCareer(characterId: String): Flow<CareerEntity?>

    @Query("UPDATE careers SET performanceScore = :score WHERE careerId = :careerId")
    suspend fun updatePerformance(careerId: String, score: Int)

    @Query("UPDATE careers SET currentSalary = :salary WHERE careerId = :careerId")
    suspend fun updateSalary(careerId: String, salary: Double)

    @Query("UPDATE careers SET isActive = 0, endYear = :endYear, reasonForLeaving = :reason WHERE careerId = :careerId")
    suspend fun endCareer(careerId: String, endYear: Int, reason: String)

    @Query("SELECT * FROM careers WHERE isActive = 1")
    suspend fun getAllActiveCareers(): List<CareerEntity>

    @Query("UPDATE careers SET promotionProgress = :progress WHERE careerId = :careerId")
    suspend fun updatePromotionProgress(careerId: String, progress: Int)
}
