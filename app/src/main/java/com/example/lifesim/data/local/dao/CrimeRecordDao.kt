package com.example.lifesim.data.local.dao

import androidx.room.*
import com.example.lifesim.data.local.entity.CrimeRecordEntity
import com.example.lifesim.data.local.entity.CrimeType
import kotlinx.coroutines.flow.Flow

@Dao
interface CrimeRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrimeRecord(record: CrimeRecordEntity)

    @Update
    suspend fun updateCrimeRecord(record: CrimeRecordEntity)

    @Delete
    suspend fun deleteCrimeRecord(record: CrimeRecordEntity)

    @Query("SELECT * FROM crime_records WHERE recordId = :recordId")
    suspend fun getCrimeRecordById(recordId: String): CrimeRecordEntity?

    @Query("SELECT * FROM crime_records WHERE characterId = :characterId")
    fun observeCrimeRecords(characterId: String): Flow<List<CrimeRecordEntity>>

    @Query("SELECT * FROM crime_records WHERE characterId = :characterId AND isConvicted = 1")
    suspend fun getConvictions(characterId: String): List<CrimeRecordEntity>

    @Query("SELECT * FROM crime_records WHERE characterId = :characterId AND isColdCase = 0 AND isExpunged = 0")
    suspend fun getActiveRecords(characterId: String): List<CrimeRecordEntity>

    @Query("SELECT * FROM crime_records WHERE characterId = :characterId AND isConvicted = 1 AND isParoled = 0")
    suspend fun getActiveSentences(characterId: String): List<CrimeRecordEntity>

    @Query("UPDATE crime_records SET yearsServed = yearsServed + 1 WHERE recordId = :recordId")
    suspend fun incrementYearsServed(recordId: String)

    @Query("UPDATE crime_records SET isParoled = 1 WHERE recordId = :recordId")
    suspend fun grantParole(recordId: String)

    @Query("UPDATE crime_records SET isExpunged = 1 WHERE recordId = :recordId")
    suspend fun expungeRecord(recordId: String)

    @Query("SELECT SUM(bailAmount - finesPaid) FROM crime_records WHERE characterId = :characterId AND isConvicted = 0")
    suspend fun getOutstandingBail(characterId: String): Double?

    @Query("SELECT SUM(prisonSentenceYears - yearsServed) FROM crime_records WHERE characterId = :characterId AND isConvicted = 1 AND isParoled = 0")
    suspend fun getRemainingSentence(characterId: String): Int?
}
