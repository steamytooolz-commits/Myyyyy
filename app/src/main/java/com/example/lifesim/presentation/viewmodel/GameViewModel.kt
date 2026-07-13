package com.example.lifesim.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifesim.data.local.dao.CharacterDao
import com.example.lifesim.data.local.dao.MemoryDao
import com.example.lifesim.data.local.dao.RelationshipDao
import com.example.lifesim.data.local.entity.*
import com.example.lifesim.data.local.AppSettingsManager
import com.example.lifesim.data.remote.LLMApiService
import com.example.lifesim.domain.ai.AIEventResult
import com.example.lifesim.domain.engine.Consequence
import com.example.lifesim.domain.engine.WorldEvent
import com.example.lifesim.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Intent {

    data object StartNewGame : Intent()
    data object AgeUp : Intent()
    data class MakeChoice(val choiceIndex: Int, val event: AIEventResult) : Intent()
    data class PerformActivity(val activityType: String) : Intent()
    data object DismissEvent : Intent()
    data class ChangeScreen(val screen: Screen) : Intent()
    data class PrisonAction(val action: String) : Intent()
    data class MilitaryAction(val action: String) : Intent()
    data class PoliticalAction(val action: String) : Intent()
    data class EducationAction(val action: String) : Intent()
    data class RealEstateAction(val action: String) : Intent()
    data class InvestmentAction(val action: String) : Intent()
    data class RelationshipInteraction(val relationshipId: String, val interactionType: com.example.lifesim.domain.engine.InteractionType) : Intent()
}

enum class Screen {
    DASHBOARD, ACTIVITIES, RELATIONSHIPS, CAREER, DYNASTY, ASSETS,
    PRISON, MILITARY, POLITICAL, EDUCATION, REAL_ESTATE, INVESTMENT, HOBBIES, PETS, MEDICAL, HISTORY, SETTINGS
}

data class UiState(
    val character: CharacterEntity? = null,
    val currentEvent: AIEventResult? = null,
    val currentScreen: Screen = Screen.DASHBOARD,
    val showEvent: Boolean = false,
    val consequences: List<Consequence> = emptyList(),
    val activeWorldEvents: List<WorldEvent> = emptyList(),
    val isDead: Boolean = false,
    val deathMessage: String? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val currentYear: Int = 2024,
    val netWorth: NetWorthResult? = null,
    val relationships: List<com.example.lifesim.data.local.entity.RelationshipEntity> = emptyList(),
    val socialCharacters: Map<String, CharacterEntity> = emptyMap(),
    val memories: List<MemoryEntity> = emptyList()
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val characterDao: CharacterDao,
    private val memoryDao: MemoryDao,
    private val relationshipDao: RelationshipDao,
    private val ageUpUseCase: AgeUpUseCase,
    private val performActivityUseCase: PerformActivityUseCase,
    private val makeChoiceUseCase: MakeChoiceUseCase,
    private val calculateNetWorth: CalculateNetWorthUseCase,
    private val generateHeirUseCase: GenerateHeirUseCase,
    private val socialEngine: com.example.lifesim.domain.engine.RelationshipAndSocialEngine,
    private val geneticsEngine: com.example.lifesim.domain.engine.DynastyAndGeneticsEngine,
    val settingsManager: AppSettingsManager,
    val apiService: LLMApiService,
    private val aiManager: com.example.lifesim.domain.ai.AIManager
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var characterId: String? = null
    private var year = 2024

    companion object {
        private const val MILLIS_PER_YEAR = 31557600000L
    }

    init {
        loadLatestCharacter()
    }

    private var relationshipsJob: kotlinx.coroutines.Job? = null

    private fun observeRelationships(charId: String) {
        relationshipsJob?.cancel()
        relationshipsJob = viewModelScope.launch {
            launch {
                relationshipDao.observeActiveRelationships(charId).collect { rels ->
                    val charactersMap = mutableMapOf<String, CharacterEntity>()
                    rels.forEach { rel ->
                        characterDao.getCharacterById(rel.targetCharacterId)?.let { target ->
                            charactersMap[rel.targetCharacterId] = target
                        }
                    }
                    _state.update { it.copy(relationships = rels, socialCharacters = charactersMap) }
                }
            }
            launch {
                memoryDao.observeActiveMemories(charId).collect { memories ->
                    _state.update { it.copy(memories = memories) }
                }
            }
        }
    }

    private fun loadLatestCharacter() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val characters = characterDao.getAllLivingCharacters()
            val character = characters.firstOrNull()
            if (character != null) {
                characterId = character.characterId
                val birthYear = (character.dateOfBirth / MILLIS_PER_YEAR).toInt()
                val recentMemories = memoryDao.getRecentMemories(character.characterId, 1)
                year = recentMemories.firstOrNull()?.yearOccurred ?: birthYear
                _state.update { it.copy(character = character, currentYear = year, isLoading = false) }
                observeRelationships(character.characterId)
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun processIntent(intent: Intent) {
        when (intent) {
            Intent.StartNewGame -> startNewGame()
            Intent.AgeUp -> ageUp()
            is Intent.MakeChoice -> makeChoice(intent.choiceIndex, intent.event)
            is Intent.PerformActivity -> performActivity(intent.activityType)
            Intent.DismissEvent -> _state.update { it.copy(showEvent = false, currentEvent = null) }
            is Intent.ChangeScreen -> _state.update { it.copy(currentScreen = intent.screen) }
            is Intent.PrisonAction -> handlePrisonAction(intent.action)
            is Intent.MilitaryAction -> handleMilitaryAction(intent.action)
            is Intent.PoliticalAction -> handlePoliticalAction(intent.action)
            is Intent.EducationAction -> handleEducationAction(intent.action)
            is Intent.RealEstateAction -> handleRealEstateAction(intent.action)
            is Intent.InvestmentAction -> handleInvestmentAction(intent.action)
            is Intent.RelationshipInteraction -> handleRelationshipInteraction(intent.relationshipId, intent.interactionType)
        }
    }

    private fun handlePrisonAction(action: String) {
        viewModelScope.launch { performActivity(action) }
    }

    private fun handleMilitaryAction(action: String) {
        viewModelScope.launch { performActivity(action) }
    }

    private fun handlePoliticalAction(action: String) {
        viewModelScope.launch { performActivity(action) }
    }

    private fun handleEducationAction(action: String) {
        viewModelScope.launch { performActivity(action) }
    }

    private fun handleRealEstateAction(action: String) {
        viewModelScope.launch { performActivity(action) }
    }

    private fun handleInvestmentAction(action: String) {
        viewModelScope.launch { performActivity(action) }
    }

    private fun startNewGame() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val living = characterDao.getAllLivingCharacters()
            living.forEach {
                characterDao.markAsDeceased(it.characterId)
            }
            year = 2024
            
            // Create parents
            val mother = CharacterEntity(
                firstName = listOf("Mary", "Jane", "Elizabeth", "Sarah", "Patricia", "Linda", "Barbara", "Margaret").random(),
                lastName = "River",
                dateOfBirth = ((year - 25 - kotlin.random.Random.nextInt(15)) * MILLIS_PER_YEAR),
                gender = Gender.FEMALE,
                isAlive = true,
                cash = 15000.0 + kotlin.random.Random.nextInt(50000),
                health = 60.0 + kotlin.random.Random.nextDouble() * 30.0,
                happiness = 60.0 + kotlin.random.Random.nextDouble() * 30.0,
                smarts = 40.0 + kotlin.random.Random.nextDouble() * 50.0,
                looks = 40.0 + kotlin.random.Random.nextDouble() * 50.0
            )
            val father = CharacterEntity(
                firstName = listOf("John", "Robert", "William", "David", "Richard", "Joseph", "Thomas", "Charles").random(),
                lastName = "River",
                dateOfBirth = ((year - 27 - kotlin.random.Random.nextInt(15)) * MILLIS_PER_YEAR),
                gender = Gender.MALE,
                isAlive = true,
                cash = 15000.0 + kotlin.random.Random.nextInt(50000),
                health = 60.0 + kotlin.random.Random.nextDouble() * 30.0,
                happiness = 60.0 + kotlin.random.Random.nextDouble() * 30.0,
                smarts = 40.0 + kotlin.random.Random.nextDouble() * 50.0,
                looks = 40.0 + kotlin.random.Random.nextDouble() * 50.0
            )
            characterDao.insertCharacter(mother)
            characterDao.insertCharacter(father)

            val character = CharacterEntity(
                firstName = "Alex", lastName = "River",
                dateOfBirth = (year * MILLIS_PER_YEAR),
                gender = Gender.MALE,
                health = 70.0, happiness = 60.0, smarts = 50.0, looks = 50.0,
                athleticism = 50.0, charisma = 50.0, stress = 30.0,
                energy = 80.0, hunger = 70.0, hygiene = 70.0,
                karma = 50, reputation = 50, cash = 5000.0, totalNetWorth = 5000.0,
                parentsIds = listOf(mother.characterId, father.characterId)
            )
            characterDao.insertCharacter(character)
            characterId = character.characterId

            val motherRel = com.example.lifesim.data.local.entity.RelationshipEntity(
                ownerCharacterId = character.characterId,
                targetCharacterId = mother.characterId,
                relationType = com.example.lifesim.data.local.entity.RelationType.PARENT,
                affection = 80,
                trust = 90,
                respect = 80
            )
            val fatherRel = com.example.lifesim.data.local.entity.RelationshipEntity(
                ownerCharacterId = character.characterId,
                targetCharacterId = father.characterId,
                relationType = com.example.lifesim.data.local.entity.RelationType.PARENT,
                affection = 75,
                trust = 85,
                respect = 80
            )
            relationshipDao.insertRelationship(motherRel)
            relationshipDao.insertRelationship(fatherRel)

            _state.update { it.copy(character = character, isLoading = false, currentYear = year, isDead = false, deathMessage = null) }
            observeRelationships(character.characterId)
        }
    }

    private fun ageUp() {
        viewModelScope.launch {
            val id = characterId ?: return@launch
            _state.update { it.copy(isLoading = true) }
            year++
            val result = ageUpUseCase(id, year)
            characterId = result.character.characterId
            characterDao.updateCharacter(result.character)
            val netWorth = calculateNetWorth(id)
            _state.update { it.copy(character = result.character, currentYear = year, isLoading = false, netWorth = netWorth,
                activeWorldEvents = result.activeWorldEvents) }

            if (result.event != null) {
                _state.update { it.copy(currentEvent = result.event, showEvent = true) }
            }
            if (result.consequences.isNotEmpty()) {
                _state.update { it.copy(consequences = result.consequences) }
            }
            if (result.isDead) {
                _state.update {
                    it.copy(isDead = true, deathMessage = result.deathCause ?: "Unknown",
                        showEvent = true, currentEvent = AIEventResult("Death", result.deathCause ?: "Your journey has ended.", emptyList(), "death", false))
                }
            }
        }
    }

    private fun makeChoice(choiceIndex: Int, event: AIEventResult) {
        viewModelScope.launch {
            val id = characterId ?: return@launch
            _state.update { it.copy(isLoading = true) }
            val result = makeChoiceUseCase(id, choiceIndex, event, year)
            _state.update { it.copy(showEvent = false, currentEvent = null, isLoading = false) }
            val character = characterDao.getCharacterById(id)
            val netWorth = refreshNetWorth(id)
            _state.update { it.copy(character = character, message = result.message, netWorth = netWorth) }
        }
    }

    private fun performActivity(activityType: String) {
        viewModelScope.launch {
            val id = characterId ?: return@launch
            val result = performActivityUseCase(id, activityType, year)
            if (result.success && result.character != null) {
                characterDao.updateCharacter(result.character)
                result.memory?.let { memoryDao.insertMemory(it) }
                val netWorth = refreshNetWorth(id)
                _state.update { it.copy(character = result.character, message = result.message, netWorth = netWorth) }
            } else {
                _state.update { it.copy(message = result.message) }
            }
        }
    }

    private suspend fun refreshNetWorth(id: String): NetWorthResult? {
        return try { this.calculateNetWorth(id) } catch (e: Exception) { null }
    }

    fun clearMessage() { _state.update { it.copy(message = null) } }

    private fun handleRelationshipInteraction(relationshipId: String, interactionType: com.example.lifesim.domain.engine.InteractionType) {
        viewModelScope.launch {
            val char = _state.value.character ?: return@launch
            val birthYear = (char.dateOfBirth / MILLIS_PER_YEAR).toInt()
            val age = (year - birthYear).coerceAtLeast(0)
            
            val rel = relationshipDao.getRelationshipById(relationshipId) ?: return@launch
            val target = characterDao.getCharacterById(rel.targetCharacterId) ?: return@launch
            
            val targetBirthYear = (target.dateOfBirth / MILLIS_PER_YEAR).toInt()
            val targetAge = (year - targetBirthYear).coerceAtLeast(0)

            var updatedChar = char
            var updatedRel = rel
            var msg = ""

            when (interactionType) {
                com.example.lifesim.domain.engine.InteractionType.CHAT -> {
                    _state.update { it.copy(isLoading = true) }
                    val npcName = "${target.firstName} ${target.lastName}"
                    val npcRel = "${rel.relationType.name} with ${rel.affection}% affection."
                    val response = if (settingsManager.getApiKey().isNullOrBlank()) {
                        "Hello there!"
                    } else {
                        aiManager.generateDialogue(npcName, npcRel, "Hey, just wanted to chat!")
                    }
                    updatedRel = rel.copy(affection = (rel.affection + 3).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                    msg = "You chatted with ${target.firstName}: \"$response\""
                    _state.update { it.copy(isLoading = false) }
                }
                com.example.lifesim.domain.engine.InteractionType.SPEND_TIME -> {
                    updatedRel = rel.copy(affection = (rel.affection + 12).coerceIn(0, 100), trust = (rel.trust + 6).coerceIn(0, 100))
                    msg = "You spent quality time with ${target.firstName}. Affection increased."
                }
                com.example.lifesim.domain.engine.InteractionType.DATE -> {
                    if (age < 12 || targetAge < 12) {
                        _state.update { it.copy(message = "Too young to date!") }
                        return@launch
                    }
                    if (rel.relationType == com.example.lifesim.data.local.entity.RelationType.SPOUSE || rel.relationType == com.example.lifesim.data.local.entity.RelationType.PARTNER) {
                        _state.update { it.copy(message = "You are already dating or married to this person.") }
                        return@launch
                    }
                    if (rel.affection > 60) {
                        updatedRel = rel.copy(relationType = com.example.lifesim.data.local.entity.RelationType.PARTNER, affection = (rel.affection + 20).coerceIn(0, 100), trust = (rel.trust + 15).coerceIn(0, 100))
                        msg = "You asked ${target.firstName} out, and they said YES! You are now partners."
                    } else {
                        updatedRel = rel.copy(affection = (rel.affection - 15).coerceIn(0, 100))
                        msg = "${target.firstName} rejected your romantic advances. Awkward."
                    }
                }
                com.example.lifesim.domain.engine.InteractionType.PROPOSE -> {
                    if (age < 18 || targetAge < 18) {
                        _state.update { it.copy(message = "You must be 18 or older to propose marriage.") }
                        return@launch
                    }
                    if (rel.relationType != com.example.lifesim.data.local.entity.RelationType.PARTNER) {
                        _state.update { it.copy(message = "You can only propose to your partner.") }
                        return@launch
                    }
                    if (rel.affection > 75 && rel.trust > 60) {
                        updatedRel = rel.copy(relationType = com.example.lifesim.data.local.entity.RelationType.SPOUSE, affection = (rel.affection + 25).coerceIn(0, 100), trust = (rel.trust + 25).coerceIn(0, 100))
                        updatedChar = char.copy(spouseId = target.characterId)
                        msg = "You proposed to ${target.firstName}, and they happily accepted! You are now married!"
                    } else {
                        updatedRel = rel.copy(affection = (rel.affection - 20).coerceIn(0, 100))
                        msg = "You proposed to ${target.firstName}, but they felt it was too soon and rejected the proposal."
                    }
                }
                com.example.lifesim.domain.engine.InteractionType.BREAK_UP -> {
                    if (rel.relationType != com.example.lifesim.data.local.entity.RelationType.PARTNER) {
                        _state.update { it.copy(message = "You can only break up with a partner.") }
                        return@launch
                    }
                    updatedRel = rel.copy(relationType = com.example.lifesim.data.local.entity.RelationType.EX_PARTNER, affection = (rel.affection - 40).coerceIn(0, 100))
                    msg = "You broke up with ${target.firstName}. It's over."
                }
                com.example.lifesim.domain.engine.InteractionType.DIVORCE -> {
                    if (rel.relationType != com.example.lifesim.data.local.entity.RelationType.SPOUSE) {
                        _state.update { it.copy(message = "You can only divorce your spouse.") }
                        return@launch
                    }
                    updatedRel = rel.copy(relationType = com.example.lifesim.data.local.entity.RelationType.EX_SPOUSE, affection = (rel.affection - 50).coerceIn(0, 100))
                    val settlement = char.cash * 0.3
                    updatedChar = char.copy(spouseId = null, cash = (char.cash - settlement).coerceAtLeast(0.0))
                    msg = "You divorced ${target.firstName}. Alimony and legal fees cost you $${"%.0f".format(settlement)}."
                }
                com.example.lifesim.domain.engine.InteractionType.HAVE_KID -> {
                    if (rel.relationType != com.example.lifesim.data.local.entity.RelationType.SPOUSE && rel.relationType != com.example.lifesim.data.local.entity.RelationType.PARTNER) {
                        _state.update { it.copy(message = "You must be in a romantic relationship to try for a baby.") }
                        return@launch
                    }
                    if (age < 18 || age > 45 || targetAge < 18 || targetAge > 50) {
                        _state.update { it.copy(message = "Conception is biologically unlikely at this age.") }
                        return@launch
                    }
                    if (kotlin.random.Random.nextDouble() < 0.5) {
                        val (mother, father) = if (char.gender == Gender.FEMALE) char to target else target to char
                        var child = geneticsEngine.createChild(mother, father, year)
                        child = child.copy(
                            firstName = listOf("Emily", "Emma", "Olivia", "Sophia", "Isabella", "James", "Oliver", "William", "Lucas", "Benjamin").random(),
                            lastName = char.lastName,
                            dateOfBirth = (year * MILLIS_PER_YEAR)
                        )
                        characterDao.insertCharacter(child)
                        val updatedChildIds = char.childIds.toMutableList()
                        updatedChildIds.add(child.characterId)
                        updatedChar = char.copy(childIds = updatedChildIds)
                        
                        val parentToChildRel = RelationshipEntity(
                            ownerCharacterId = char.characterId, targetCharacterId = child.characterId,
                            relationType = com.example.lifesim.data.local.entity.RelationType.CHILD, affection = 90, trust = 80
                        )
                        val childToParentRel = RelationshipEntity(
                            ownerCharacterId = child.characterId, targetCharacterId = char.characterId,
                            relationType = com.example.lifesim.data.local.entity.RelationType.PARENT, affection = 95, trust = 90
                        )
                        relationshipDao.insertRelationship(parentToChildRel)
                        relationshipDao.insertRelationship(childToParentRel)
                        msg = "Conception successful! Your child ${child.firstName} was born!"
                    } else {
                        msg = "You tried to conceive a baby with ${target.firstName}, but it was unsuccessful. Try again!"
                    }
                }
                else -> {
                    val (uc, ur) = socialEngine.interact(char, rel, interactionType)
                    updatedChar = uc
                    updatedRel = ur
                    msg = "You performed ${interactionType.name.lowercase()} with ${target.firstName}."
                }
            }

            characterDao.updateCharacter(updatedChar)
            relationshipDao.updateRelationship(updatedRel)
            relationshipDao.recordInteraction(relationshipId, year.toLong())
            _state.update { it.copy(character = updatedChar, message = msg) }
        }
    }
}
