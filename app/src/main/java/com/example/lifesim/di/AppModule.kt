package com.example.lifesim.di

import com.example.lifesim.domain.ai.AIManager
import com.example.lifesim.domain.ai.ContextBuilder
import com.example.lifesim.domain.engine.*
import com.example.lifesim.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun provideTimeAndAgingEngine(): TimeAndAgingEngine = TimeAndAgingEngine()
    @Provides @Singleton fun provideStatAndAttributeEngine(): StatAndAttributeEngine = StatAndAttributeEngine()
    @Provides @Singleton fun provideConsequenceAndMemoryEngine(): ConsequenceAndMemoryEngine = ConsequenceAndMemoryEngine()
    @Provides @Singleton fun provideRelationshipAndSocialEngine(): RelationshipAndSocialEngine = RelationshipAndSocialEngine()
    @Provides @Singleton fun provideCareerAndEconomyEngine(): CareerAndEconomyEngine = CareerAndEconomyEngine()
    @Provides @Singleton fun provideDynastyAndGeneticsEngine(): DynastyAndGeneticsEngine = DynastyAndGeneticsEngine()
    @Provides @Singleton fun provideCrimeAndLegalEngine(): CrimeAndLegalEngine = CrimeAndLegalEngine()
    @Provides @Singleton fun provideAddictionEngine(): AddictionEngine = AddictionEngine()
    @Provides @Singleton fun providePrisonEngine(): PrisonEngine = PrisonEngine()
    @Provides @Singleton fun provideRealEstateEngine(): RealEstateEngine = RealEstateEngine()
    @Provides @Singleton fun provideMilitaryEngine(): MilitaryEngine = MilitaryEngine()
    @Provides @Singleton fun providePoliticalEngine(): PoliticalEngine = PoliticalEngine()
    @Provides @Singleton fun provideEducationEngine(): EducationEngine = EducationEngine()
    @Provides @Singleton fun provideInvestmentEngine(): InvestmentEngine = InvestmentEngine()
    @Provides @Singleton fun provideWorldEventEngine(): WorldEventEngine = WorldEventEngine()
    @Provides @Singleton fun provideHobbyEngine(): HobbyEngine = HobbyEngine()
    @Provides @Singleton fun providePetEngine(): PetEngine = PetEngine()
    @Provides @Singleton fun provideMedicalEngine(): MedicalEngine = MedicalEngine()
    @Provides @Singleton fun provideContextBuilder(): ContextBuilder = ContextBuilder()

    @Provides @Singleton
    fun provideAIManager(
        apiService: com.example.lifesim.data.remote.LLMApiService,
        contextBuilder: ContextBuilder,
        settingsManager: com.example.lifesim.data.local.AppSettingsManager
    ): AIManager = AIManager(apiService, contextBuilder, settingsManager)

    @Provides @Singleton
    fun provideAgeUpUseCase(
        characterDao: com.example.lifesim.data.local.dao.CharacterDao, memoryDao: com.example.lifesim.data.local.dao.MemoryDao,
        relationshipDao: com.example.lifesim.data.local.dao.RelationshipDao, careerDao: com.example.lifesim.data.local.dao.CareerDao,
        crimeRecordDao: com.example.lifesim.data.local.dao.CrimeRecordDao,
        timeEngine: TimeAndAgingEngine, statEngine: StatAndAttributeEngine, consequenceEngine: ConsequenceAndMemoryEngine,
        socialEngine: RelationshipAndSocialEngine, careerEngine: CareerAndEconomyEngine, crimeEngine: CrimeAndLegalEngine,
        addictionEngine: AddictionEngine, prisonEngine: PrisonEngine, militaryEngine: MilitaryEngine,
        politicalEngine: PoliticalEngine, educationEngine: EducationEngine, investmentEngine: InvestmentEngine,
        worldEventEngine: WorldEventEngine, hobbyEngine: HobbyEngine, petEngine: PetEngine,
        medicalEngine: MedicalEngine, geneticsEngine: DynastyAndGeneticsEngine,
        contextBuilder: ContextBuilder, aiManager: AIManager
    ): AgeUpUseCase = AgeUpUseCase(characterDao, memoryDao, relationshipDao, careerDao, crimeRecordDao,
        timeEngine, statEngine, consequenceEngine, socialEngine, careerEngine, crimeEngine,
        addictionEngine, prisonEngine, militaryEngine, politicalEngine, educationEngine, investmentEngine,
        worldEventEngine, hobbyEngine, petEngine, medicalEngine, geneticsEngine, contextBuilder, aiManager)

    @Provides @Singleton
    fun providePerformActivityUseCase(
        characterDao: com.example.lifesim.data.local.dao.CharacterDao, memoryDao: com.example.lifesim.data.local.dao.MemoryDao,
        crimeRecordDao: com.example.lifesim.data.local.dao.CrimeRecordDao,
        statEngine: StatAndAttributeEngine, crimeEngine: CrimeAndLegalEngine, addictionEngine: AddictionEngine,
        prisonEngine: PrisonEngine, militaryEngine: MilitaryEngine, politicalEngine: PoliticalEngine,
        educationEngine: EducationEngine, investmentEngine: InvestmentEngine,
        hobbyEngine: HobbyEngine, petEngine: PetEngine, medicalEngine: MedicalEngine
    ): PerformActivityUseCase = PerformActivityUseCase(characterDao, memoryDao, crimeRecordDao, statEngine, crimeEngine, addictionEngine,
        prisonEngine, militaryEngine, politicalEngine, educationEngine, investmentEngine, hobbyEngine, petEngine, medicalEngine)

    @Provides @Singleton
    fun provideMakeChoiceUseCase(
        characterDao: com.example.lifesim.data.local.dao.CharacterDao, memoryDao: com.example.lifesim.data.local.dao.MemoryDao,
        consequenceEngine: ConsequenceAndMemoryEngine, statEngine: StatAndAttributeEngine,
        aiManager: com.example.lifesim.domain.ai.AIManager, contextBuilder: com.example.lifesim.domain.ai.ContextBuilder
    ): MakeChoiceUseCase = MakeChoiceUseCase(characterDao, memoryDao, consequenceEngine, statEngine, aiManager, contextBuilder)

    @Provides @Singleton
    fun provideCalculateNetWorthUseCase(
        characterDao: com.example.lifesim.data.local.dao.CharacterDao, assetDao: com.example.lifesim.data.local.dao.AssetDao
    ): CalculateNetWorthUseCase = CalculateNetWorthUseCase(characterDao, assetDao)

    @Provides @Singleton
    fun provideGenerateHeirUseCase(
        characterDao: com.example.lifesim.data.local.dao.CharacterDao, memoryDao: com.example.lifesim.data.local.dao.MemoryDao,
        relationshipDao: com.example.lifesim.data.local.dao.RelationshipDao, geneticsEngine: DynastyAndGeneticsEngine
    ): GenerateHeirUseCase = GenerateHeirUseCase(characterDao, memoryDao, relationshipDao, geneticsEngine)
}
