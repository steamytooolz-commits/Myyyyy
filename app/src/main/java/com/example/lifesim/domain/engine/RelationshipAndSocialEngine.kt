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
            else -> {}
        }
        return c to rel
    }
}

enum class InteractionType { CHAT, GIFT, HELP, ARGUE, FLIRT, APOLOGIZE, BETRAY, MAKE_AMENDS, SPEND_TIME, DATE, PROPOSE, BREAK_UP, DIVORCE, HAVE_KID }

data class TriggerEvent(val title: String, val description: String, val statChanges: Map<String, Double>, val category: String)
data class NetworkEffect(val socialScore: Int, val highStatusConnections: Int, val enemyCount: Int)
data class EventChoice(val text: String, val statChanges: Map<String, Double> = emptyMap(), val tags: List<String> = emptyList())
