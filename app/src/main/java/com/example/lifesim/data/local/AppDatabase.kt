package com.example.lifesim.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lifesim.data.local.dao.*
import com.example.lifesim.data.local.entity.*

@Database(
    entities = [
        CharacterEntity::class,
        MemoryEntity::class,
        RelationshipEntity::class,
        CareerEntity::class,
        AssetEntity::class,
        DynastyEntity::class,
        CrimeRecordEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun memoryDao(): MemoryDao
    abstract fun relationshipDao(): RelationshipDao
    abstract fun careerDao(): CareerDao
    abstract fun assetDao(): AssetDao
    abstract fun dynastyDao(): DynastyDao
    abstract fun crimeRecordDao(): CrimeRecordDao
}
