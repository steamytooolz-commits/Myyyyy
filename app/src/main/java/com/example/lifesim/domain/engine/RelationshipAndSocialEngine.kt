package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

@Singleton
class RelationshipAndSocialEngine @Inject constructor() {

    fun decayRelationships(relationships: List<RelationshipEntity>, ticksSinceLastInteraction: Int? = null): List<RelationshipEntity> {
        return relationships.map { rel ->
            val ticksSince = ticksSinceLastInteraction ?: 12
            val decayFactor = (ticksSince / 10f).coerceAtLeast(1f)
            val spouseMultiplier = if (rel.relationType == RelationType.SPOUSE || rel.relationType == RelationType.PARTNER) 0.3f else 1f
            val affectionDecay = (rel.affection * 0.02f * decayFactor * spouseMultiplier).toInt().coerceIn(0, 5)
            val trustDecay = (rel.trust * 0.01f * decayFactor * spouseMultiplier).toInt().coerceIn(0, 3)
            rel.copy(
                affection = (rel.affection - affectionDecay).coerceIn(0, 100),
                trust = (rel.trust - trustDecay).coerceIn(0, 100),
                jealousy = (rel.jealousy + (decayFactor / 2).toInt()).coerceIn(0, 100)
            )
        }
    }

    fun calculateSocialFriction(relationship: RelationshipEntity): TriggerEvent? {
        if (relationship.jealousy > 60 && relationship.trust < 30 && Random.nextFloat() < 0.3f) {
            return TriggerEvent("Jealousy Argument", "Your jealousy is damaging this relationship.",
                mapOf("affection" to -10.0, "trust" to -8.0, "happiness" to -5.0), "relationship_fight")
        }
        if (relationship.fear > 50 && relationship.affection < 30 && Random.nextFloat() < 0.15f) {
            return TriggerEvent("Fearful Distance", "This relationship is strained by fear.",
                mapOf("affection" to -5.0, "trust" to -5.0), "relationship_strain")
        }
        if (relationship.affection > 70 && relationship.lust > 60 && relationship.relationType == RelationType.FRIEND && Random.nextFloat() < 0.2f) {
            return TriggerEvent("Romantic Tension", "Your friendship is developing romantic undertones.",
                mapOf("affection" to 5.0, "lust" to 10.0, "happiness" to 3.0), "relationship_romance")
        }
        if (relationship.favorsOwed > 5 && Random.nextFloat() < 0.1f) {
            return TriggerEvent("Favor Called In", "They're asking you to repay a favor.",
                mapOf("reputation" to 3.0, "stress" to 5.0), "relationship_favor")
        }
        return null
    }

    fun calculateNetworkEffect(character: CharacterEntity, relationships: List<RelationshipEntity>): NetworkEffect {
        val totalRespect = relationships.sumOf { it.respect }
        val totalAffection = relationships.sumOf { it.affection }
        val highStatusCount = relationships.count { it.respect > 70 && it.relationType != RelationType.ENEMY }
        val enemyCount = relationships.count { it.relationType == RelationType.ENEMY || it.affection < 20 }
        val socialScore = ((totalRespect + totalAffection) / (relationships.size.coerceAtLeast(1) * 2f)).toInt().coerceIn(0, 100)
        return NetworkEffect(socialScore, highStatusCount, enemyCount)
    }

    fun interact(character: CharacterEntity, relationship: RelationshipEntity, interactionType: InteractionType): Pair<CharacterEntity, RelationshipEntity> {
        var rel = relationship
        var c = character
        when (interactionType) {
            InteractionType.CHAT -> {
                rel = rel.copy(affection = (rel.affection + 3).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
            }
            InteractionType.GIFT -> {
                rel = rel.copy(affection = (rel.affection + 10).coerceIn(0, 100), trust = (rel.trust + 5).coerceIn(0, 100), favorsGiven = rel.favorsGiven + 1)
                c = c.copy(cash = c.cash - 100.0)
            }
            InteractionType.HELP -> {
                rel = rel.copy(affection = (rel.affection + 8).coerceIn(0, 100), trust = (rel.trust + 8).coerceIn(0, 100), favorsGiven = rel.favorsGiven + 1)
                c = c.copy(karma = (c.karma + 5).coerceIn(0, 100))
            }
            InteractionType.ARGUE -> {
                rel = rel.copy(affection = (rel.affection - 8).coerceIn(0, 100), trust = (rel.trust - 5).coerceIn(0, 100), jealousy = (rel.jealousy + 5).coerceIn(0, 100))
                c = c.copy(stress = (c.stress + 8.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FLIRT -> {
                rel = rel.copy(lust = (rel.lust + 10).coerceIn(0, 100), affection = (rel.affection + 5).coerceIn(0, 100))
            }
            InteractionType.APOLOGIZE -> {
                rel = rel.copy(forgiveness = (rel.forgiveness + 15).coerceIn(0, 100), affection = (rel.affection + 5).coerceIn(0, 100))
            }
            InteractionType.BETRAY -> {
                rel = rel.copy(trust = (rel.trust - 30).coerceIn(0, 100), affection = (rel.affection - 20).coerceIn(0, 100), jealousy = (rel.jealousy + 15).coerceIn(0, 100))
                c = c.copy(karma = (c.karma - 15).coerceIn(0, 100))
            }
            InteractionType.MAKE_AMENDS -> {
                rel = rel.copy(trust = (rel.trust + 15).coerceIn(0, 100), forgiveness = (rel.forgiveness + 20).coerceIn(0, 100), affection = (rel.affection + 10).coerceIn(0, 100))
                c = c.copy(karma = (c.karma + 10).coerceIn(0, 100))
            }
            InteractionType.SPEND_TIME, InteractionType.DATE, InteractionType.PROPOSE, InteractionType.BREAK_UP, InteractionType.DIVORCE, InteractionType.HAVE_KID -> {}
            InteractionType.FAMILY_PLAY -> {
                rel = rel.copy(affection = (rel.affection + 6).coerceIn(0, 100), trust = (rel.trust + 3).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(happiness = (c.happiness + 4.0).coerceIn(0.0, 100.0), energy = (c.energy - 5.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_DINNER -> {
                rel = rel.copy(affection = (rel.affection + 9).coerceIn(0, 100), trust = (rel.trust + 4).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(happiness = (c.happiness + 6.0).coerceIn(0.0, 100.0), cash = (c.cash - 50.0).coerceAtLeast(0.0))
            }
            InteractionType.FAMILY_READ -> {
                rel = rel.copy(affection = (rel.affection + 5).coerceIn(0, 100), trust = (rel.trust + 5).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(smarts = (c.smarts + 1.0).coerceIn(0.0, 100.0), energy = (c.energy - 2.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_MOVIE -> {
                rel = rel.copy(affection = (rel.affection + 7).coerceIn(0, 100), trust = (rel.trust + 3).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(happiness = (c.happiness + 5.0).coerceIn(0.0, 100.0), cash = (c.cash - 20.0).coerceAtLeast(0.0))
            }
            InteractionType.FAMILY_VACATION -> {
                rel = rel.copy(affection = (rel.affection + 20).coerceIn(0, 100), trust = (rel.trust + 10).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(happiness = (c.happiness + 20.0).coerceIn(0.0, 100.0), cash = (c.cash - 2000.0).coerceAtLeast(0.0), stress = (c.stress - 15.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_SPORTS -> {
                rel = rel.copy(affection = (rel.affection + 8).coerceIn(0, 100), trust = (rel.trust + 5).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(athleticism = (c.athleticism + 3.0).coerceIn(0.0, 100.0), energy = (c.energy - 10.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_CHORES -> {
                rel = rel.copy(affection = (rel.affection + 2).coerceIn(0, 100), trust = (rel.trust + 6).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(discipline = (c.discipline + 2.0).coerceIn(0.0, 100.0), energy = (c.energy - 8.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_HOMEWORK -> {
                rel = rel.copy(affection = (rel.affection + 4).coerceIn(0, 100), trust = (rel.trust + 7).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(smarts = (c.smarts + 2.0).coerceIn(0.0, 100.0), energy = (c.energy - 5.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_TEACH -> {
                rel = rel.copy(affection = (rel.affection + 5).coerceIn(0, 100), trust = (rel.trust + 8).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(smarts = (c.smarts + 3.0).coerceIn(0.0, 100.0), energy = (c.energy - 4.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_ADVICE -> {
                rel = rel.copy(affection = (rel.affection + 3).coerceIn(0, 100), trust = (rel.trust + 10).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(happiness = (c.happiness + 2.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_CONVERSATION -> {
                rel = rel.copy(affection = (rel.affection + 5).coerceIn(0, 100), trust = (rel.trust + 5).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(happiness = (c.happiness + 3.0).coerceIn(0.0, 100.0))
            }
            InteractionType.FAMILY_SURPRISE_GIFT -> {
                rel = rel.copy(affection = (rel.affection + 15).coerceIn(0, 100), trust = (rel.trust + 8).coerceIn(0, 100), interactionCount = rel.interactionCount + 1)
                c = c.copy(cash = (c.cash - 150.0).coerceAtLeast(0.0), happiness = (c.happiness + 10.0).coerceIn(0.0, 100.0))
            }
        }
        return c to rel
    }
}

enum class InteractionType { CHAT, GIFT, HELP, ARGUE, FLIRT, APOLOGIZE, BETRAY, MAKE_AMENDS, SPEND_TIME, DATE, PROPOSE, BREAK_UP, DIVORCE, HAVE_KID, FAMILY_PLAY, FAMILY_DINNER, FAMILY_READ, FAMILY_MOVIE, FAMILY_VACATION, FAMILY_SPORTS, FAMILY_CHORES, FAMILY_HOMEWORK, FAMILY_TEACH, FAMILY_ADVICE, FAMILY_CONVERSATION, FAMILY_SURPRISE_GIFT }

data class TriggerEvent(val title: String, val description: String, val statChanges: Map<String, Double>, val category: String)
data class NetworkEffect(val socialScore: Int, val highStatusConnections: Int, val enemyCount: Int)
data class EventChoice(val text: String, val statChanges: Map<String, Double> = emptyMap(), val tags: List<String> = emptyList())
