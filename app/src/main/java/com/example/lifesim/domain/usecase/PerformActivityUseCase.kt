package com.example.lifesim.domain.usecase

import com.example.lifesim.data.local.dao.CharacterDao
import com.example.lifesim.data.local.dao.CrimeRecordDao
import com.example.lifesim.data.local.dao.MemoryDao
import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.MemoryEntity
import com.example.lifesim.data.local.entity.MemoryEventType
import com.example.lifesim.data.local.entity.CrimeType
import com.example.lifesim.domain.engine.*
import javax.inject.Inject

class PerformActivityUseCase @Inject constructor(
    private val characterDao: CharacterDao, private val memoryDao: MemoryDao, private val crimeRecordDao: CrimeRecordDao,
    private val relationshipDao: com.example.lifesim.data.local.dao.RelationshipDao,
    private val statEngine: StatAndAttributeEngine, private val crimeEngine: CrimeAndLegalEngine,
    private val addictionEngine: AddictionEngine, private val prisonEngine: PrisonEngine,
    private val militaryEngine: MilitaryEngine, private val politicalEngine: PoliticalEngine,
    private val educationEngine: EducationEngine, private val investmentEngine: InvestmentEngine,
    private val hobbyEngine: HobbyEngine,
    private val petEngine: PetEngine,
    private val medicalEngine: MedicalEngine
) {
    data class ActivityResult(val success: Boolean, val message: String, val character: CharacterEntity? = null, val memory: MemoryEntity? = null)

    suspend operator fun invoke(characterId: String, activityType: String, currentYear: Int): ActivityResult {
        val character = characterDao.getCharacterById(characterId) ?: return ActivityResult(false, "Character not found")

        val birthYear = (character.dateOfBirth / 31557600000L).toInt()
        val age = (currentYear - birthYear).coerceAtLeast(0)

        // Age-based activity checks
        val restrictedDailyActivitiesForBabies = listOf(
            "gym", "study", "meditate", "work", "party", "travel", "therapy",
            "crime", "drink", "gamble", "smoke", "rehab"
        )
        val restrictedDailyActivitiesForKids = listOf(
            "work", "travel", "drink", "gamble", "smoke", "rehab", "crime"
        )
        val restrictedDailyActivitiesForTeens = listOf(
            "work", "travel", "gamble", "rehab"
        )

        if (age < 5) {
            if (activityType in restrictedDailyActivitiesForBabies || 
                activityType.startsWith("hobby_") || 
                activityType.startsWith("pet_") || 
                activityType.startsWith("medical_") ||
                activityType == "invest_stocks" || activityType == "invest_retirement" || activityType == "invest_crypto" ||
                activityType == "military_enlist" || activityType == "political_run" || activityType == "education_enroll"
            ) {
                return ActivityResult(false, "You are only a baby! You can't do that yet.")
            }
        } else if (age < 12) {
            if (activityType in restrictedDailyActivitiesForKids ||
                activityType == "invest_stocks" || activityType == "invest_retirement" || activityType == "invest_crypto" ||
                activityType == "military_enlist" || activityType == "political_run" || activityType == "education_enroll"
            ) {
                return ActivityResult(false, "You are too young for this activity.")
            }
        } else if (age < 18) {
            if (activityType in restrictedDailyActivitiesForTeens ||
                activityType == "invest_stocks" || activityType == "invest_retirement" || activityType == "invest_crypto" ||
                activityType == "military_enlist" || activityType == "political_run" || activityType == "education_enroll"
            ) {
                return ActivityResult(false, "You must be 18 or older to perform adult activities like full-time work, investments, or military enlistment.")
            }
        }

        // SPECIAL ACTIONS that bypass stat engine
        when {
            activityType.startsWith("prison_") -> return handlePrisonAction(activityType, character, currentYear)
            activityType == "military_enlist" -> { val r = militaryEngine.enlist(character, false); characterDao.updateCharacter(r.character); return ActivityResult(r.success, r.message, r.character) }
            activityType == "military_discharge" -> { val r = militaryEngine.discharge(character, true); characterDao.updateCharacter(r.character); return ActivityResult(r.success, r.message, r.character) }
            activityType == "political_run" -> {
                val nextOffice = politicalEngine.offices.getOrNull(character.politicalOfficeTier) ?: politicalEngine.offices.first()
                val r = politicalEngine.runCampaign(character, nextOffice)
                characterDao.updateCharacter(r.character); return ActivityResult(r.success, r.message, r.character)
            }
            activityType == "royalty_grant" -> { val r = politicalEngine.grantRoyalTitle(character); characterDao.updateCharacter(r.character); return ActivityResult(r.success, r.message, r.character) }
            activityType == "education_enroll" -> {
                if (!character.degrees.contains("High School Diploma")) {
                    return ActivityResult(false, "You need a High School Diploma to enroll in University.")
                }
                val school = educationEngine.universities.random(); val major = listOf("Computer Science", "Business", "Engineering", "Psychology", "Biology", "Political Science").random()
                val r = educationEngine.applyToSchool(character, school, major); characterDao.updateCharacter(r.character); return ActivityResult(r.success, r.message, r.character)
            }
            activityType == "education_graduate" -> { val r = educationEngine.graduate(character); characterDao.updateCharacter(r.character); return ActivityResult(r.success, r.message, r.character) }
            activityType == "invest_stocks" -> {
                if (character.cash < 1000.0) return ActivityResult(false, "Need $1,000 to invest.")
                val amount = (character.cash * 0.3).coerceAtMost(10000.0)
                val inv = investmentEngine.createInvestment(character, InvestmentType.STOCK, amount, currentYear)
                if (inv != null) { val c = character.copy(cash = character.cash - amount, stockPortfolioValue = character.stockPortfolioValue + amount); characterDao.updateCharacter(c); return ActivityResult(true, "Invested $${"%.0f".format(amount)} in stocks.", c) }
                return ActivityResult(false, "Investment failed.")
            }
            activityType == "invest_retirement" -> {
                val amount = (character.cash * 0.2).coerceAtMost(15000.0)
                val c = character.copy(cash = character.cash - amount, retirementSavings = character.retirementSavings + amount)
                characterDao.updateCharacter(c); return ActivityResult(true, "Contributed $${"%.0f".format(amount)} to retirement.", c)
            }
            activityType == "invest_crypto" -> {
                if (character.cash < 500.0) return ActivityResult(false, "Need $500 to buy crypto.")
                val amount = (character.cash * 0.15).coerceAtMost(5000.0)
                val inv = investmentEngine.createInvestment(character, InvestmentType.CRYPTO, amount, currentYear)
                if (inv != null) { val c = character.copy(cash = character.cash - amount, stockPortfolioValue = character.stockPortfolioValue + amount); characterDao.updateCharacter(c); return ActivityResult(true, "Bought $${"%.0f".format(amount)} in crypto.", c) }
                return ActivityResult(false, "Crypto purchase failed.")
            }
            // HOBBY actions
            activityType.startsWith("hobby_start_") -> {
                val hobbyId = activityType.removePrefix("hobby_start_")
                val c = hobbyEngine.startHobby(character, hobbyId)
                if (c != null) { characterDao.updateCharacter(c); return ActivityResult(true, "Started new hobby: ${hobbyEngine.hobbies.find { it.id == hobbyId }?.name ?: hobbyId}", c) }
                return ActivityResult(false, "Already doing that hobby or it doesn't exist.")
            }
            activityType.startsWith("hobby_practice_") -> {
                val hobbyId = activityType.removePrefix("hobby_practice_")
                val (c, msg) = hobbyEngine.practiceHobby(character, hobbyId)
                characterDao.updateCharacter(c)
                return ActivityResult(true, msg, c)
            }
            activityType.startsWith("hobby_sell_") -> {
                val hobbyId = activityType.removePrefix("hobby_sell_")
                val (c, msg) = hobbyEngine.monetizeHobby(character, hobbyId)
                characterDao.updateCharacter(c)
                return ActivityResult(true, msg, c)
            }
            // PET actions
            activityType.startsWith("pet_adopt_") -> {
                val parts = activityType.removePrefix("pet_adopt_").split("__")
                if (parts.size == 2) {
                    val breedName = parts[0]; val petName = parts[1]
                    val breed = PetBreed.entries.find { it.displayName == breedName }
                    if (breed != null) {
                        val c = petEngine.adoptPet(character, breed, petName, currentYear)
                        if (c != null) { characterDao.updateCharacter(c); return ActivityResult(true, "Adopted $petName the $breedName! ", c) }
                        return ActivityResult(false, "Can't afford or pet limit reached.")
                    }
                }
                return ActivityResult(false, "Invalid pet breed.")
            }
            activityType.startsWith("pet_feed_") -> {
                val petId = activityType.removePrefix("pet_feed_")
                val (c, msg) = petEngine.feedPet(character, petId, currentYear)
                characterDao.updateCharacter(c); return ActivityResult(true, msg, c)
            }
            activityType.startsWith("pet_play_") -> {
                val petId = activityType.removePrefix("pet_play_")
                val (c, msg) = petEngine.playWithPet(character, petId)
                characterDao.updateCharacter(c); return ActivityResult(true, msg, c)
            }
            activityType.startsWith("pet_vet_") -> {
                val petId = activityType.removePrefix("pet_vet_")
                val (c, msg) = petEngine.vetVisit(character, petId)
                characterDao.updateCharacter(c); return ActivityResult(true, msg, c)
            }
            activityType.startsWith("pet_train_") -> {
                val petId = activityType.removePrefix("pet_train_")
                val (c, msg) = petEngine.trainPet(character, petId)
                characterDao.updateCharacter(c); return ActivityResult(true, msg, c)
            }
            activityType.startsWith("pet_walk_") -> {
                val petId = activityType.removePrefix("pet_walk_")
                val (c, msg) = petEngine.walkPet(character, petId)
                characterDao.updateCharacter(c); return ActivityResult(true, msg, c)
            }
            // MEDICAL actions
            activityType.startsWith("medical_treat_") -> {
                val condId = activityType.removePrefix("medical_treat_")
                val (c, msg) = medicalEngine.treatCondition(character, condId, currentYear)
                characterDao.updateCharacter(c); return ActivityResult(true, msg, c)
            }
            activityType.startsWith("medical_insurance_") -> {
                val planName = activityType.removePrefix("medical_insurance_")
                val plan = InsurancePlan.entries.find { it.name == planName }
                if (plan != null) {
                    val c = medicalEngine.setInsurance(character, plan)
                    characterDao.updateCharacter(c); return ActivityResult(true, "Changed to ${plan.label} insurance.", c)
                }
                return ActivityResult(false, "Invalid insurance plan.")
            }
            activityType == "medical_checkup" -> {
                if (character.cash < 200.0) return ActivityResult(false, "Checkup costs $200.")
                val conditions = medicalEngine.getActiveConditions(character)
                val msg = if (conditions.isEmpty()) "You're healthy! No conditions detected." else "You have ${conditions.size} active condition(s)."
                return ActivityResult(true, msg, character.copy(cash = character.cash - 200.0))
            }
        }

        var statChanges = statEngine.calculateStatChange(character, activityType).toMutableMap()
        if (statChanges.isEmpty()) return ActivityResult(false, "Unknown activity: $activityType")

        if (age < 18) {
            val cashCost = -(statChanges["cash"] ?: 0.0)
            if (cashCost > 0) {
                 statChanges["cash"] = 0.0 // Free for kids (parents/state pay)
            }
        }

        val energyCost = -(statChanges["energy"] ?: 0.0)
        if (energyCost > 0 && character.energy < energyCost) {
            return ActivityResult(false, "You are too tired! Get some rest or Age Up.")
        }
        val cashCost = -(statChanges["cash"] ?: 0.0)
        if (cashCost > 0 && character.cash < cashCost) {
            return ActivityResult(false, "You can't afford this activity.")
        }

        var updatedChar = character
        statChanges.forEach { (stat, change) ->
            updatedChar = when { stat.startsWith("addiction_") && change > 0 -> addictionEngine.applySubstance(updatedChar, stat.removePrefix("addiction_"))
                else -> applyChange(updatedChar, stat, change) }
        }

        // Crime special handling
        if (activityType == "crime") {
            val crimeResult = crimeEngine.attemptCrime(character, CrimeType.THEFT)
            val record = crimeEngine.generateCrimeRecord(character, crimeResult, CrimeType.THEFT, currentYear)
            crimeRecordDao.insertCrimeRecord(record)
            updatedChar = if (crimeResult.caught) updatedChar.copy(isInPrison = true) else updatedChar
            val message = if (crimeResult.success && !crimeResult.caught) "Crime successful!" else if (crimeResult.caught) "Caught! Bail: $${crimeResult.bailAmount}" else "Failed but escaped."
            return ActivityResult(success = true, message = message, character = updatedChar,
                memory = MemoryEntity(characterId = characterId, yearOccurred = currentYear, ageOccurred = currentYear - (character.dateOfBirth / 31557600000L).toInt(), tickOccurred = currentYear.toLong(), eventType = MemoryEventType.CRIME, description = message, emotionalValence = if (crimeResult.success) 0.5 else -0.5, emotionalIntensity = 0.7, tags = listOf("crime")))
        }

        // Romance special handling
        if (activityType == "find_date" || activityType == "dating_app") {
            if (kotlin.random.Random.nextDouble() < 0.6) {
                val npcGender = if (character.sexualOrientation == com.example.lifesim.data.local.entity.SexualOrientation.GAY) character.gender else if (character.sexualOrientation == com.example.lifesim.data.local.entity.SexualOrientation.STRAIGHT) (if (character.gender == com.example.lifesim.data.local.entity.Gender.MALE) com.example.lifesim.data.local.entity.Gender.FEMALE else com.example.lifesim.data.local.entity.Gender.MALE) else com.example.lifesim.data.local.entity.Gender.values().random()
                val npc = CharacterEntity(
                    firstName = listOf("Taylor", "Jordan", "Alex", "Sam", "Casey", "Riley", "Jamie", "Morgan", "Avery", "Harper").random(),
                    lastName = listOf("Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez").random(),
                    dateOfBirth = character.dateOfBirth + ((kotlin.random.Random.nextInt(-5, 6)) * 31557600000L),
                    gender = npcGender,
                    health = 60.0 + kotlin.random.Random.nextDouble() * 40.0,
                    looks = 40.0 + kotlin.random.Random.nextDouble() * 60.0,
                    smarts = 40.0 + kotlin.random.Random.nextDouble() * 60.0,
                    cash = 1000.0 + kotlin.random.Random.nextDouble() * 50000.0
                )
                characterDao.insertCharacter(npc)
                val relation = com.example.lifesim.data.local.entity.RelationshipEntity(
                    ownerCharacterId = characterId, targetCharacterId = npc.characterId,
                    relationType = com.example.lifesim.data.local.entity.RelationType.PARTNER,
                    affection = 60, trust = 50, respect = 50, lust = 70
                )
                relationshipDao.insertRelationship(relation)
                characterDao.updateCharacter(updatedChar)
                return ActivityResult(success = true, message = "You went on a date and hit it off with ${npc.firstName} ${npc.lastName}!", character = updatedChar,
                    memory = MemoryEntity(characterId = characterId, yearOccurred = currentYear, ageOccurred = currentYear - (character.dateOfBirth / 31557600000L).toInt(), tickOccurred = currentYear.toLong(), eventType = MemoryEventType.MUNDANE, description = "Started dating ${npc.firstName}", emotionalValence = 0.8, emotionalIntensity = 0.6, tags = listOf("romance")))
            } else {
                characterDao.updateCharacter(updatedChar)
                return ActivityResult(success = true, message = "You didn't find anyone you clicked with this time.", character = updatedChar)
            }
        }

        // Lottery special handling
        if (activityType == "lottery") {
            if (kotlin.random.Random.nextDouble() < 0.005) { // 0.5% chance
                val winnings = 10000.0 + kotlin.random.Random.nextDouble() * 990000.0
                updatedChar = updatedChar.copy(cash = updatedChar.cash + winnings, happiness = (updatedChar.happiness + 50.0).coerceIn(0.0, 100.0))
                characterDao.updateCharacter(updatedChar)
                return ActivityResult(success = true, message = "JACKPOT! You won $${"%,.0f".format(winnings)} in the lottery!", character = updatedChar,
                    memory = MemoryEntity(characterId = characterId, yearOccurred = currentYear, ageOccurred = currentYear - (character.dateOfBirth / 31557600000L).toInt(), tickOccurred = currentYear.toLong(), eventType = MemoryEventType.MUNDANE, description = "Won the lottery", emotionalValence = 1.0, emotionalIntensity = 1.0, tags = listOf("lottery", "wealth")))
            } else {
                characterDao.updateCharacter(updatedChar)
                return ActivityResult(success = true, message = "You bought a lottery ticket, but didn't win anything.", character = updatedChar)
            }
        }

        // Family special handling
        if (activityType == "adopt_child") {
            if (updatedChar.cash < 25000) {
                return ActivityResult(false, "You don't have enough money to adopt a child.", character = updatedChar)
            }
            val npcGender = com.example.lifesim.data.local.entity.Gender.values().random()
            val childAge = kotlin.random.Random.nextInt(0, 10)
            val npc = CharacterEntity(
                firstName = listOf("Taylor", "Jordan", "Alex", "Sam", "Casey", "Riley", "Jamie", "Morgan", "Avery", "Harper", "Leo", "Mia").random(),
                lastName = character.lastName,
                dateOfBirth = character.dateOfBirth + ((age - childAge) * 31557600000L),
                gender = npcGender,
                health = 70.0 + kotlin.random.Random.nextDouble() * 30.0,
                parentsIds = listOf(characterId)
            )
            characterDao.insertCharacter(npc)
            val relation = com.example.lifesim.data.local.entity.RelationshipEntity(
                ownerCharacterId = characterId, targetCharacterId = npc.characterId,
                relationType = com.example.lifesim.data.local.entity.RelationType.CHILD,
                affection = 80, trust = 60, respect = 50
            )
            relationshipDao.insertRelationship(relation)
            characterDao.updateCharacter(updatedChar)
            return ActivityResult(success = true, message = "You successfully adopted a child named ${npc.firstName}!", character = updatedChar,
                memory = MemoryEntity(characterId = characterId, yearOccurred = currentYear, ageOccurred = currentYear - (character.dateOfBirth / 31557600000L).toInt(), tickOccurred = currentYear.toLong(), eventType = MemoryEventType.MILESTONE, description = "Adopted a child named ${npc.firstName}", emotionalValence = 0.9, emotionalIntensity = 0.8, tags = listOf("family", "adoption")))
        }

        if (activityType == "spend_time_family") {
            val rels = relationshipDao.getActiveRelationships(characterId)
            val familyRels = rels.filter { it.relationType == com.example.lifesim.data.local.entity.RelationType.CHILD || it.relationType == com.example.lifesim.data.local.entity.RelationType.PARENT || it.relationType == com.example.lifesim.data.local.entity.RelationType.SIBLING || it.relationType == com.example.lifesim.data.local.entity.RelationType.SPOUSE || it.relationType == com.example.lifesim.data.local.entity.RelationType.PARTNER }
            if (familyRels.isEmpty()) {
                return ActivityResult(false, "You don't have any family to spend time with.", character = updatedChar)
            }
            familyRels.forEach { rel ->
                relationshipDao.updateRelationship(rel.copy(affection = (rel.affection + 5).coerceIn(0, 100), trust = (rel.trust + 2).coerceIn(0, 100)))
            }
            characterDao.updateCharacter(updatedChar)
            return ActivityResult(success = true, message = "You spent quality time with your family.", character = updatedChar)
        }

        val moodlet = statEngine.createMoodletForEvent(activityType)
        if (moodlet != null && moodlet.modifier > 0) updatedChar = when (moodlet.stat.lowercase()) { "happiness" -> updatedChar.copy(happiness = (updatedChar.happiness + moodlet.modifier).coerceIn(0.0, 100.0)); "stress" -> updatedChar.copy(stress = (updatedChar.stress + moodlet.modifier).coerceIn(0.0, 100.0)); else -> updatedChar }
        val memory = if (statChanges.any { it.value > 5 }) MemoryEntity(characterId = characterId, yearOccurred = currentYear, ageOccurred = currentYear - (character.dateOfBirth / 31557600000L).toInt(), tickOccurred = currentYear.toLong(), eventType = MemoryEventType.MUNDANE, description = "Performed: $activityType", emotionalValence = if (statChanges.values.sum() > 0) 0.3 else -0.3, emotionalIntensity = 0.3, tags = listOf(activityType)) else null
        characterDao.updateCharacter(updatedChar); return ActivityResult(success = true, message = "Activity completed: $activityType", character = updatedChar, memory = memory)
    }

    private suspend fun handlePrisonAction(action: String, character: CharacterEntity, year: Int): ActivityResult {
        val result = when (action) { "prison_join_gang" -> prisonEngine.joinGang(character); "prison_take_job" -> prisonEngine.takePrisonJob(character); "prison_escape" -> prisonEngine.attemptEscape(character); "prison_parole" -> { val records = crimeRecordDao.getActiveSentences(character.characterId); prisonEngine.requestParole(character, records) }; "prison_contraband" -> prisonEngine.acquireContraband(character); else -> return ActivityResult(false, "Unknown prison action") }
        characterDao.updateCharacter(result.character); return ActivityResult(result.success, result.message, result.character)
    }

    private fun applyChange(c: CharacterEntity, stat: String, delta: Double): CharacterEntity = when (stat.lowercase()) {
        "health" -> c.copy(health = (c.health + delta).coerceIn(0.0, 100.0)); "happiness" -> c.copy(happiness = (c.happiness + delta).coerceIn(0.0, 100.0))
        "smarts" -> c.copy(smarts = (c.smarts + delta).coerceIn(0.0, 100.0)); "looks" -> c.copy(looks = (c.looks + delta).coerceIn(0.0, 100.0))
        "stress" -> c.copy(stress = (c.stress + delta).coerceIn(0.0, 100.0)); "energy" -> c.copy(energy = (c.energy + delta).coerceIn(0.0, 100.0))
        "hunger" -> c.copy(hunger = (c.hunger + delta).coerceIn(0.0, 100.0)); "hygiene" -> c.copy(hygiene = (c.hygiene + delta).coerceIn(0.0, 100.0))
        "athleticism" -> c.copy(athleticism = (c.athleticism + delta).coerceIn(0.0, 100.0)); "charisma" -> c.copy(charisma = (c.charisma + delta).coerceIn(0.0, 100.0))
        "creativity" -> c.copy(creativity = (c.creativity + delta).coerceIn(0.0, 100.0)); "cash" -> c.copy(cash = c.cash + delta)
        "reputation" -> c.copy(reputation = (c.reputation + delta.toInt()).coerceIn(0, 100)); "karma" -> c.copy(karma = (c.karma + delta.toInt()).coerceIn(0, 100))
        "notoriety" -> c.copy(notoriety = (c.notoriety + delta.toInt()).coerceIn(0, 100)); "sanity" -> c.copy(sanity = (c.sanity + delta).coerceIn(0.0, 100.0))
        else -> c
    }
}
