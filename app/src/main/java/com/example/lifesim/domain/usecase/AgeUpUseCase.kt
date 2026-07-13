package com.example.lifesim.domain.usecase

import com.example.lifesim.data.local.dao.*
import com.example.lifesim.data.local.entity.*
import com.example.lifesim.domain.ai.AIManager
import com.example.lifesim.domain.ai.ContextBuilder
import com.example.lifesim.domain.ai.AIEventResult
import com.example.lifesim.domain.engine.*
import javax.inject.Inject
import kotlin.random.Random

data class AgeUpResult(
    val character: CharacterEntity,
    val event: AIEventResult? = null,
    val consequences: List<Consequence> = emptyList(),
    val isDead: Boolean = false,
    val deathCause: String? = null,
    val newMemories: List<MemoryEntity> = emptyList(),
    val activeWorldEvents: List<WorldEvent> = emptyList()
)

class AgeUpUseCase @Inject constructor(
    private val characterDao: CharacterDao, private val memoryDao: MemoryDao,
    private val relationshipDao: RelationshipDao, private val careerDao: CareerDao,
    private val crimeRecordDao: CrimeRecordDao,
    private val timeEngine: TimeAndAgingEngine, private val statEngine: StatAndAttributeEngine,
    private val consequenceEngine: ConsequenceAndMemoryEngine, private val socialEngine: RelationshipAndSocialEngine,
    private val careerEngine: CareerAndEconomyEngine, private val crimeEngine: CrimeAndLegalEngine,
    private val addictionEngine: AddictionEngine, private val prisonEngine: PrisonEngine,
    private val militaryEngine: MilitaryEngine, private val politicalEngine: PoliticalEngine,
    private val educationEngine: EducationEngine, private val investmentEngine: InvestmentEngine,
    private val worldEventEngine: WorldEventEngine, private val hobbyEngine: HobbyEngine,
    private val petEngine: PetEngine, private val medicalEngine: MedicalEngine,
    private val geneticsEngine: DynastyAndGeneticsEngine,
    private val contextBuilder: ContextBuilder, private val aiManager: AIManager
) {
    suspend operator fun invoke(characterId: String, currentYear: Int): AgeUpResult {
        val character = characterDao.getCharacterById(characterId) ?: return AgeUpResult(
            CharacterEntity(firstName = "Dead", lastName = "", dateOfBirth = 0, gender = Gender.MALE), isDead = true, deathCause = "Character not found")
        val birthYear = (character.dateOfBirth / 31557600000L).toInt()
        var c = timeEngine.advanceYear(character, birthYear, currentYear)
        c = statEngine.applyNeedsPenalties(c)
        val memories = memoryDao.getMostIntenseMemories(characterId, 100)
        val (charAfterConsequences, initialConsequences) = consequenceEngine.evaluateConsequences(c, memories, currentYear)
        val consequences = initialConsequences.toMutableList()
        c = charAfterConsequences
        repeat(12) { c = timeEngine.advanceMonth(c) }

        val age = currentYear - birthYear
        if (age == 5) {
            c = c.copy(currentEducationId = "Elementary School", educationYearsCompleted = 0)
            consequences.add(Consequence("Started Elementary School! Time to learn and make friends.", mapOf("smarts" to 2.0), 1))
        } else if (age == 11) {
            c = c.copy(currentEducationId = "Middle School", educationYearsCompleted = 0)
            consequences.add(Consequence("Graduated Elementary School! Enrolled in Middle School.", mapOf("smarts" to 3.0), 2))
        } else if (age == 14) {
            c = c.copy(currentEducationId = "High School", educationYearsCompleted = 0)
            consequences.add(Consequence("Entered High School! Big league of youth.", mapOf("smarts" to 4.0), 2))
        } else if (age == 18) {
            val updatedDegrees = c.degrees.toMutableList()
            if (!updatedDegrees.contains("High School Diploma")) {
                updatedDegrees.add("High School Diploma")
            }
            c = c.copy(currentEducationId = null, educationYearsCompleted = 0, degrees = updatedDegrees)
            consequences.add(Consequence("Graduated High School! Received your High School Diploma. You can now apply to University or get a Job!", mapOf("smarts" to 5.0, "reputation" to 5.0, "happiness" to 8.0), 3))
        }

        val decayedMemories = consequenceEngine.decayMemories(memories)
        val newMemories = mutableListOf<MemoryEntity>()
        consequences.forEach { newMemories.add(MemoryEntity(characterId = characterId, yearOccurred = currentYear,
            ageOccurred = currentYear - birthYear, tickOccurred = currentYear.toLong(), eventType = MemoryEventType.MUNDANE,
            description = it.description, emotionalValence = -0.3, emotionalIntensity = it.severity / 10.0,
            tags = listOf("consequence"), currentIntensity = it.severity / 10.0)) }

        // WORLD EVENTS — generate annual event and apply effects
        val newEvent = worldEventEngine.generateAnnualEvent(currentYear)
        if (newEvent != null) {
            consequences.add(Consequence("World Event: ${newEvent.title} — ${newEvent.description}",
                mapOf("stress" to 2.0), 3))
            newMemories.add(MemoryEntity(characterId = characterId, yearOccurred = currentYear,
                ageOccurred = currentYear - birthYear, tickOccurred = currentYear.toLong(),
                eventType = MemoryEventType.MUNDANE,
                description = "World event: ${newEvent.title} — ${newEvent.description}",
                emotionalValence = 0.0, emotionalIntensity = newEvent.severity / 10.0,
                tags = listOf("world_event", newEvent.type.name.lowercase()),
                currentIntensity = newEvent.severity / 10.0))
        }
        val worldEffects = worldEventEngine.getCombinedEffects()
        if (worldEffects.statModifiers.isNotEmpty() || worldEffects.cashDelta != 0.0 || worldEffects.reputationDelta != 0) {
            c = statEngine.applyWorldEventEffects(c, worldEffects)
            if (worldEffects.cashDelta != 0.0) c = c.copy(cash = (c.cash + worldEffects.cashDelta).coerceAtLeast(0.0))
            if (worldEffects.reputationDelta != 0) c = c.copy(reputation = (c.reputation + worldEffects.reputationDelta).coerceIn(0, 100))
            if (worldEffects.isDeadly && Random.nextDouble() < worldEffects.deathChanceBonus) {
                memoryDao.insertMemories(decayedMemories + newMemories)
                characterDao.markAsDeceased(characterId)
                return AgeUpResult(c.copy(isAlive = false), isDead = true, deathCause = "died in a world event: ${worldEventEngine.getCurrentMajorEvent()?.title ?: "unknown"}", consequences = consequences)
            }
            consequences.addAll(worldEffects.statModifiers.entries.filter { it.value != 0.0 }.map {
                Consequence("World event modifier: ${it.key} ${if (it.value > 0) "+" else ""}${String.format("%.0f", it.value)}", mapOf(it.key to it.value), 1)
            })
        }

        // MILITARY yearly processing
        if (c.isInMilitary) {
            val milResult = militaryEngine.serveYear(c)
            c = milResult.character
            milResult.events.forEach { consequences.add(Consequence(it, mapOf("discipline" to 3.0), 2)) }
            if (!c.isAlive) { memoryDao.insertMemories(decayedMemories + newMemories); characterDao.markAsDeceased(characterId); return AgeUpResult(c, isDead = true, deathCause = "killed in action", consequences = consequences) }
        }

        // POLITICAL yearly processing
        if (c.politicalOfficeTitle != null) {
            c = c.copy(politicalTermYearsRemaining = c.politicalTermYearsRemaining - 1)
            val polResult = politicalEngine.serveTerm(c, c.politicalOfficeTitle)
            c = polResult.character
            consequences.add(Consequence(polResult.description, mapOf("stress" to 5.0), 2))
            if (c.politicalTermYearsRemaining <= 0) c = c.copy(politicalOfficeTitle = null, politicalOfficeTier = 0)
        }

        // EDUCATION yearly processing
        if (c.currentEducationId != null) {
            c = c.copy(educationYearsCompleted = c.educationYearsCompleted + 1)
            val eduResult = educationEngine.studyYear(c)
            c = eduResult.character
            eduResult.events.forEach { consequences.add(Consequence(it, mapOf("smarts" to 3.0), 1)) }
        }

        // INVESTMENT yearly processing
        if (c.retirementSavings > 0 || c.stockPortfolioValue > 0) {
            investmentEngine.simulateMarket()
            val stockReturn = investmentEngine.simulateReturn(Investment(name = "Portfolio", type = InvestmentType.STOCK, amountInvested = c.stockPortfolioValue, currentValue = c.stockPortfolioValue, riskLevel = 5, purchaseYear = currentYear))
            c = c.copy(stockPortfolioValue = (c.stockPortfolioValue * (1.0 + stockReturn)).coerceAtLeast(0.0))
            val retirementReturn = if (c.retirementSavings > 0) 0.05 + java.util.Random().nextGaussian() * 0.08 else 0.0
            c = c.copy(retirementSavings = (c.retirementSavings * (1.0 + retirementReturn)).coerceAtLeast(0.0))
            if (investmentEngine.marketCrashWarning()) consequences.add(Consequence("Market crash! Your portfolio took a hit.", mapOf("stress" to 10.0, "cash" to -c.stockPortfolioValue * 0.1), 4))
        }

        // PRISON yearly processing
        if (c.isInPrison) {
            val crimeRecords = crimeRecordDao.getActiveSentences(characterId)
            val prisonResult = prisonEngine.processPrisonTick(c, crimeRecords, currentYear)
            c = prisonResult.character; consequences.addAll(prisonResult.consequences); newMemories.addAll(prisonResult.newMemories)
            if (!c.isAlive) { memoryDao.insertMemories(decayedMemories + newMemories); characterDao.markAsDeceased(characterId); return AgeUpResult(c, isDead = true, deathCause = prisonResult.deathCause ?: "died in prison", consequences = consequences) }
            crimeRecords.forEach { crimeRecordDao.incrementYearsServed(it.recordId) }
        }

        // MEDICAL yearly processing — disease progression, recovery, death
        c = medicalEngine.ageConditions(c)
        if (!c.isAlive) {
            memoryDao.insertMemories(decayedMemories + newMemories); characterDao.markAsDeceased(characterId)
            val deathCondition = medicalEngine.parseConditions(c.medicalConditions).firstOrNull()?.name
            return AgeUpResult(c, isDead = true, deathCause = deathCondition ?: "medical complications", consequences = consequences)
        }
        // Random disease roll
        val newDisease = medicalEngine.generateRandomCondition(c, currentYear)
        if (newDisease != null) {
            c = medicalEngine.contractCondition(c, newDisease, currentYear)
            consequences.add(Consequence("You contracted ${newDisease.name}!", mapOf("health" to -newDisease.healthDrainPerYear, "stress" to newDisease.severity.toDouble()), 3))
        }

        // PET yearly aging and upkeep
        c = petEngine.agePets(c)
        c = petEngine.applyPetBoosts(c)

        // HOBBY yearly decay
        c = hobbyEngine.decayHobbies(c)

        // ADDICTION yearly processing
        if (c.isAddicted) {
            val addictionResult = addictionEngine.processTick(c); c = addictionResult.character; consequences.addAll(addictionResult.consequences)
            if (addictionResult.triggeredRelapse) newMemories.add(MemoryEntity(characterId = characterId, yearOccurred = currentYear,
                ageOccurred = currentYear - birthYear, tickOccurred = currentYear.toLong(), eventType = MemoryEventType.TRAUMA,
                description = "Relapsed into ${c.addictionType ?: "addiction"}.", emotionalValence = -0.8, emotionalIntensity = 0.7,
                tags = listOf("addiction", "relapse", "trauma"), currentIntensity = 0.7))
            if (!c.isAlive) { memoryDao.insertMemories(decayedMemories + newMemories); characterDao.markAsDeceased(characterId); return AgeUpResult(c, isDead = true, deathCause = "addiction complications", consequences = consequences) }
        }

        memoryDao.insertMemories(decayedMemories + newMemories)

        // Career update
        character.currentCareerId?.let { careerId ->
            val career = careerDao.getCareerById(careerId)
            if (career?.isActive == true) {
                val perf = careerEngine.calculatePerformance(career, c); careerDao.updatePerformance(careerId, perf)
                if (careerEngine.shouldPromote(career.copy(performanceScore = perf))) { careerDao.updateCareer(careerEngine.promote(career)); consequences.add(Consequence("You got promoted!", mapOf("happiness" to 10.0), 2)) }
                if (careerEngine.shouldFire(career.copy(performanceScore = perf))) { careerDao.endCareer(careerId, currentYear, "performance"); consequences.add(Consequence("You were fired.", mapOf("happiness" to -15.0, "reputation" to -10.0), 4)) }
            }
        }
        // Social decay
        val relationships = relationshipDao.getActiveRelationships(characterId)
        if (relationships.isNotEmpty()) {
            socialEngine.decayRelationships(relationships).forEach { relationshipDao.updateRelationship(it) }
        }

        // PREGNANCY AND CHILD BIRTH IN GAME TICK
        if (age in 18..45 && Random.nextDouble() < 0.15) {
            val partnerRel = relationships.find { it.relationType == RelationType.SPOUSE || it.relationType == RelationType.PARTNER }
            if (partnerRel != null) {
                val partner = characterDao.getCharacterById(partnerRel.targetCharacterId)
                if (partner != null && partner.isAlive) {
                    val (mother, father) = if (c.gender == Gender.FEMALE) c to partner else partner to c
                    var child = geneticsEngine.createChild(mother, father, currentYear)
                    child = child.copy(
                        firstName = listOf("Emily", "Emma", "Olivia", "Sophia", "Isabella", "James", "Oliver", "William", "Lucas", "Benjamin").random(),
                        lastName = c.lastName
                    )
                    characterDao.insertCharacter(child)
                    val updatedChildIds = c.childIds.toMutableList()
                    updatedChildIds.add(child.characterId)
                    c = c.copy(childIds = updatedChildIds)
                    val parentToChildRel = RelationshipEntity(
                        ownerCharacterId = c.characterId, targetCharacterId = child.characterId,
                        relationType = RelationType.CHILD, affection = 90, trust = 80
                    )
                    val childToParentRel = RelationshipEntity(
                        ownerCharacterId = child.characterId, targetCharacterId = c.characterId,
                        relationType = RelationType.PARENT, affection = 95, trust = 90
                    )
                    relationshipDao.insertRelationship(parentToChildRel)
                    relationshipDao.insertRelationship(childToParentRel)
                    consequences.add(Consequence("Congratulations! Your child ${child.firstName} was born!", mapOf("happiness" to 20.0, "stress" to 10.0), 3))
                }
            }
        }

        // DYNAMIC RELATIONSHIP SPAWNING
        if ((age in 6..22 || c.currentCareerId != null) && Random.nextDouble() < 0.25) {
            val activeCount = relationshipDao.getActiveRelationshipCount(c.characterId)
            if (activeCount < 15) {
                val isFriend = Random.nextBoolean()
                val npcGender = if (Random.nextFloat() < 0.5) Gender.MALE else Gender.FEMALE
                val npcFirstName = if (npcGender == Gender.MALE) {
                    listOf("Liam", "Noah", "Oliver", "James", "Elijah", "William", "Henry", "Lucas").random()
                } else {
                    listOf("Olivia", "Emma", "Charlotte", "Amelia", "Sophia", "Isabella", "Mia", "Evelyn").random()
                }
                val npcLastName = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis").random()
                val npc = CharacterEntity(
                    firstName = npcFirstName, lastName = npcLastName,
                    dateOfBirth = ((currentYear - age + Random.nextInt(-2, 3)) * 31557600000L),
                    gender = npcGender, isAlive = true,
                    health = 70.0 + Random.nextDouble() * 30.0, happiness = 70.0 + Random.nextDouble() * 30.0,
                    smarts = 40.0 + Random.nextDouble() * 50.0, looks = 40.0 + Random.nextDouble() * 50.0
                )
                characterDao.insertCharacter(npc)
                val relType = if (isFriend) RelationType.FRIEND else RelationType.COLLEAGUE
                val relation = RelationshipEntity(
                    ownerCharacterId = c.characterId, targetCharacterId = npc.characterId,
                    relationType = relType, affection = 50 + Random.nextInt(20), trust = 50 + Random.nextInt(20)
                )
                relationshipDao.insertRelationship(relation)
                consequences.add(Consequence("Met someone new: ${npc.firstName} ${npc.lastName} (${relType.name.lowercase()})", mapOf("charisma" to 1.0), 1))
            }
        }

        // Death check
        if (c.health <= 0.0 || Random.nextDouble() < timeEngine.calculateDeathProbability(c, birthYear, currentYear)) {
            characterDao.markAsDeceased(characterId); return AgeUpResult(c.copy(isAlive = false), isDead = true, deathCause = "natural causes", consequences = consequences)
        }

        val activeCareer = character.currentCareerId?.let { careerDao.getCareerById(it) }
        val event = aiManager.generateLifeEvent(contextBuilder.buildCharacterSummary(c, currentYear), contextBuilder.buildCurrentSituation(c, activeCareer), contextBuilder.buildRecentMemorySummary(memories), contextBuilder.buildSocialGraphSummary(relationships))
        characterDao.updateCharacter(c); return AgeUpResult(c, event = event, consequences = consequences,
            activeWorldEvents = worldEventEngine.getActiveEvents())
    }
}
