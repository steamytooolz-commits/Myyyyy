package com.example.lifesim.di

import android.content.Context
import androidx.room.Room
import com.example.lifesim.data.local.AppDatabase
import com.example.lifesim.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "aeterna_life_db")
            .fallbackToDestructiveMigration().build()
    }

    @Provides fun provideCharacterDao(db: AppDatabase): CharacterDao = db.characterDao()
    @Provides fun provideMemoryDao(db: AppDatabase): MemoryDao = db.memoryDao()
    @Provides fun provideRelationshipDao(db: AppDatabase): RelationshipDao = db.relationshipDao()
    @Provides fun provideCareerDao(db: AppDatabase): CareerDao = db.careerDao()
    @Provides fun provideAssetDao(db: AppDatabase): AssetDao = db.assetDao()
    @Provides fun provideDynastyDao(db: AppDatabase): DynastyDao = db.dynastyDao()
    @Provides fun provideCrimeRecordDao(db: AppDatabase): CrimeRecordDao = db.crimeRecordDao()
}
