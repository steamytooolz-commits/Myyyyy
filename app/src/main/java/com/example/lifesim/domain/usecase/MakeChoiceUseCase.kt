package com.example.lifesim.domain.usecase

import com.example.lifesim.data.local.dao.CharacterDao
import com.example.lifesim.data.local.dao.MemoryDao
import com.example.lifesim.data.local.entity.MemoryEntity
import com.example.lifesim.data.local.entity.MemoryEventType
import com.example.lifesim.domain.ai.AIEventResult
import com.example.lifesim.domain.engine.ConsequenceAndMemoryEngine
import com.example.lifesim.domain.engine.StatAndAttributeEngine
import com.example.lifesim.domain.ai.AIManager
import com.example.lifesim.domain.ai.ContextBuilder
import kotlin.math.abs
import javax.inject.Inject

data class ChoiceResult(val characterId: String, val message: String, val newMemoryId: String? = null)

class MakeChoiceUseCase @Inject constructor(
    private val characterDao: CharacterDao,
    private val memoryDao: MemoryDao,
    private val consequenceEngine: ConsequenceAndMemoryEngine,
    private val statEngine: StatAndAttributeEngine,
    private val aiManager: AIManager,
    private val contextBuilder: ContextBuilder
) {
    suspend operator fun invoke(characterId: String, choiceIndex: Int, event: AIEventResult, currentYear: Int): ChoiceResult {
        val character = characterDao.getCharacterById(characterId) ?: return ChoiceResult(characterId, "Character not found")
        val choice = event.choices.getOrNull(choiceIndex) ?: return ChoiceResult(characterId, "Invalid choice")
        
        var updatedChar = character
        val characterSummary = contextBuilder.buildCharacterSummary(character, currentYear)
        
        // Let the AI evaluate the outcome of the choice if possible
        val outcome = aiManager.evaluateChoiceOutcome(characterSummary, event.title, event.description, choice.text)
        
        val statChanges = outcome?.stat_changes ?: choice.statChanges
        val message = outcome?.outcome_narrative ?: "You chose: ${choice.text}"

        statChanges.forEach { (stat, change) ->
            val numChange = change.toDouble()
            updatedChar = when (stat.lowercase()) {
                "health" -> updatedChar.copy(health = (updatedChar.health + numChange).coerceIn(0.0, 100.0))
                "happiness" -> updatedChar.copy(happiness = (updatedChar.happiness + numChange).coerceIn(0.0, 100.0))
                "smarts" -> updatedChar.copy(smarts = (updatedChar.smarts + numChange).coerceIn(0.0, 100.0))
                "stress" -> updatedChar.copy(stress = (updatedChar.stress + numChange).coerceIn(0.0, 100.0))
                "money", "cash" -> updatedChar.copy(cash = updatedChar.cash + numChange)
                "reputation" -> updatedChar.copy(reputation = (updatedChar.reputation + numChange.toInt()).coerceIn(0, 100))
                "karma" -> updatedChar.copy(karma = (updatedChar.karma + numChange.toInt()).coerceIn(0, 100))
                "energy" -> updatedChar.copy(energy = (updatedChar.energy + numChange).coerceIn(0.0, 100.0))
                else -> updatedChar
            }
        }
        
        val totalImpact = statChanges.values.sumOf { it.toDouble() }
        val memory = MemoryEntity(
            characterId = characterId,
            yearOccurred = currentYear,
            ageOccurred = currentYear - (character.dateOfBirth / 31557600000L).toInt(),
            tickOccurred = currentYear.toLong(),
            eventType = if (totalImpact > 0) MemoryEventType.TRIUMPH 
                       else if (totalImpact <= -10) MemoryEventType.TRAUMA 
                       else MemoryEventType.MUNDANE,
            description = "${event.title}: $message",
            emotionalValence = (totalImpact / 50.0).coerceIn(-1.0, 1.0),
            emotionalIntensity = (abs(totalImpact) / 30.0).coerceIn(0.1, 1.0),
            tags = choice.tags + event.category,
            currentIntensity = (abs(totalImpact) / 30.0).coerceIn(0.1, 1.0)
        )
        
        characterDao.updateCharacter(updatedChar)
        memoryDao.insertMemory(memory)
        
        return ChoiceResult(characterId, message, memory.memoryId)
    }
}
