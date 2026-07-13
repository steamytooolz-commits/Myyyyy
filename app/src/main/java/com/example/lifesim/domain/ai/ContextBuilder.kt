package com.example.lifesim.domain.ai

import com.example.lifesim.data.local.entity.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContextBuilder @Inject constructor() {

    fun buildCharacterSummary(character: CharacterEntity, currentYear: Int = 2024): String {
        val birthYear = (character.dateOfBirth / 31557600000L).toInt()
        val age = (currentYear - birthYear).coerceAtLeast(0)
        return "${character.firstName} ${character.lastName}, age $age (Generation ${character.generationNumber}), " +
            "Health:${character.health.toInt()} Happiness:${character.happiness.toInt()} Smarts:${character.smarts.toInt()} " +
            "Looks:${character.looks.toInt()} Karma:${character.karma} Stress:${character.stress.toInt()} " +
            "Reputation:${character.reputation} Money:$${String.format("%.0f", character.cash)}"
    }

    fun buildRecentMemorySummary(memories: List<MemoryEntity>, maxTokens: Int = 10): String {
        return memories.sortedByDescending { it.currentIntensity }.take(maxTokens).joinToString("\n") {
            "[${it.yearOccurred}] ${it.eventType.name}: ${it.description.take(80)} (intensity: ${"%.1f".format(it.currentIntensity)})"
        }
    }

    fun buildSocialGraphSummary(relationships: List<RelationshipEntity>): String {
        return relationships.filter { it.relationshipStatus == RelationshipStatus.ACTIVE }
            .sortedByDescending { it.affection }
            .take(5).joinToString("\n") {
                "${it.relationType.name} - Affection:${it.affection} Trust:${it.trust} Fear:${it.fear}"
            }
    }

    fun buildCurrentSituation(character: CharacterEntity, career: CareerEntity?): String {
        val parts = mutableListOf<String>()
        if (character.isInPrison) parts.add("Currently incarcerated.")
        if (character.isInHospital) parts.add("Currently hospitalized.")
        if (character.isAddicted) parts.add("Addicted to ${character.addictionType} (severity: ${character.addictionSeverity}).")
        if (career != null) parts.add("Working as ${career.jobTitle} at ${career.companyName}.")
        if (character.spouseId != null) parts.add("Married.")
        if (character.childIds.isNotEmpty()) parts.add("Has ${character.childIds.size} children.")
        return if (parts.isEmpty()) "Currently unoccupied." else parts.joinToString(" ")
    }

    fun buildFullContext(character: CharacterEntity, memories: List<MemoryEntity>, relationships: List<RelationshipEntity>, career: CareerEntity?): String {
        return """CHARACTER: ${buildCharacterSummary(character)}
SITUATION: ${buildCurrentSituation(character, career)}
RECENT MEMORIES: ${buildRecentMemorySummary(memories)}
RELATIONSHIPS: ${buildSocialGraphSummary(relationships)}
STRESS: ${character.stress.toInt()} PARANOIA: ${character.paranoia.toInt()} SANITY: ${character.sanity.toInt()}""""
    }
}
