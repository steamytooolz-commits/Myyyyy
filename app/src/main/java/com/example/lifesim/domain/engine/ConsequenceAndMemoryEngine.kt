package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.MemoryEntity
import com.example.lifesim.data.local.entity.MemoryEventType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.exp
import kotlin.math.max
import kotlin.random.Random

data class Consequence(val description: String, val statChanges: Map<String, Double>, val severity: Int)

@Singleton
class ConsequenceAndMemoryEngine @Inject constructor() {

    fun evaluateConsequences(character: CharacterEntity, memories: List<MemoryEntity>, year: Int): Pair<CharacterEntity, List<Consequence>> {
        var c = character
        val consequences = mutableListOf<Consequence>()

        val unresolvedTraumas = memories.filter { it.eventType == MemoryEventType.TRAUMA && !it.isResolved && !it.isRepressed }
        if (unresolvedTraumas.isNotEmpty()) {
            val totalIntensity = unresolvedTraumas.sumOf { it.currentIntensity }
            val paranoiaBoost = (totalIntensity * 0.1).toInt().coerceIn(0, 30)
            val happinessDrop = (totalIntensity * 0.05).toInt().coerceIn(0, 20)
            c = c.copy(paranoia = (c.paranoia + paranoiaBoost).coerceIn(0.0, 100.0), happiness = (c.happiness - happinessDrop).coerceIn(0.0, 100.0))
            if (totalIntensity > 5.0 && Random.nextFloat() < 0.15f) {
                consequences.add(Consequence("A traumatic memory resurfaces, causing a panic attack.", mapOf("stress" to 15.0, "happiness" to -10.0), 3))
            }
        }

        val highStress = memories.filter { it.tags.contains("stress") && it.currentIntensity > 3.0 }
        if (highStress.isNotEmpty() && c.stress > 60.0 && c.health < 40.0) {
            consequences.add(Consequence("Your stress has become physically overwhelming. Health is deteriorating!", mapOf("health" to -10.0, "stress" to -5.0), 4))
        }

        val traumaTriggers = unresolvedTraumas.filter { it.tags.any { tag -> tag in listOf("trigger", "relapse", "panic") } }
        traumaTriggers.forEach { memory ->
            if (memory.currentIntensity > 3.0 && Random.nextFloat() < 0.1f) {
                consequences.add(Consequence("Memories of: ${memory.description.take(50)}... flood back, unsettling you.", mapOf("stress" to 8.0, "happiness" to -5.0), 2))
            }
        }

        val triumphs = memories.filter { it.eventType == MemoryEventType.TRIUMPH && !it.isRepressed }
        if (triumphs.isNotEmpty() && Random.nextFloat() < 0.2f) {
            val triumph = triumphs.random()
            consequences.add(Consequence("You remember your triumph: ${triumph.description.take(50)}", mapOf("happiness" to 8.0, "charisma" to 3.0), 1))
            c = c.copy(happiness = (c.happiness + 5.0).coerceIn(0.0, 100.0))
        }

        val crimeMemories = memories.filter { it.eventType == MemoryEventType.CRIME && !it.isRepressed }
        if (crimeMemories.isNotEmpty()) {
            val totalNotoriety = crimeMemories.size * 3
            c = c.copy(notoriety = (c.notoriety + totalNotoriety).coerceIn(0, 100))
            if (c.notoriety > 50 && Random.nextFloat() < 0.1f) {
                consequences.add(Consequence("Your criminal past catches up! A background check reveals your record.", mapOf("reputation" to -15.0, "stress" to 10.0), 3))
            }
        }

        return c to consequences
    }

    fun decayMemories(memories: List<MemoryEntity>, ticksPassed: Int = 1): List<MemoryEntity> {
        return memories.map { memory ->
            val decayAmount = memory.decayRate * ticksPassed
            val newIntensity = max(0.0, memory.currentIntensity - decayAmount)
            val newRepressed = memory.isRepressed || (memory.eventType == MemoryEventType.TRAUMA && memory.isResolved && Random.nextFloat() < 0.05f)
            val newLastRecalled = if (memory.lastRecalledTick < System.currentTimeMillis() - 100000) memory.lastRecalledTick else System.currentTimeMillis()
            memory.copy(
                currentIntensity = newIntensity,
                isRepressed = newRepressed,
                lastRecalledTick = if (Random.nextFloat() < 0.01f) System.currentTimeMillis() else memory.lastRecalledTick
            )
        }.filter { it.currentIntensity > 0.1 }
    }

    fun processMemoryTherapy(character: CharacterEntity, memory: MemoryEntity, year: Int): Pair<CharacterEntity, MemoryEntity> {
        var c = character
        val resolved = memory.copy(isResolved = true, resolutionYear = year, resolutionMethod = "therapy")
        c = c.copy(stress = (c.stress - 10.0).coerceIn(0.0, 100.0), sanity = (c.sanity + 5.0).coerceIn(0.0, 100.0))
        return c to resolved
    }

    fun calculateEventProbabilities(character: CharacterEntity, memories: List<MemoryEntity>): Map<String, Float> {
        val probs = mutableMapOf<String, Float>()
        probs["legal"] = (character.notoriety * 0.005f) + if (character.karma < -30) 0.15f else 0f
        probs["health_crisis"] = if (character.health < 30) 0.2f else if (character.stress > 70) 0.15f else 0.05f
        probs["social"] = if (character.reputation < 20) 0.15f else if (character.charisma > 70) 0.1f else 0.05f
        probs["romance"] = if (character.spouseId == null && character.looks > 50) 0.1f else 0.03f
        probs["career"] = if (character.currentCareerId != null) 0.15f else 0.05f
        probs["addiction"] = if (character.isAddicted) 0.2f else 0f
        val traumaCount = memories.count { it.eventType == MemoryEventType.TRAUMA && !it.isResolved }
        probs["relapse"] = if (character.isAddicted && traumaCount > 0) 0.15f else 0f
        return probs
    }
}
