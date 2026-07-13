package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

@Singleton
class DynastyAndGeneticsEngine @Inject constructor() {

    fun createChild(mother: CharacterEntity, father: CharacterEntity?, year: Int): CharacterEntity {
        val childHealth = blendTraits(mother.health, father?.health ?: mother.health) + java.util.Random().nextGaussian() * 5.0
        val childLooks = blendTraits(mother.looks, father?.looks ?: mother.looks) + java.util.Random().nextGaussian() * 5.0
        val childSmarts = blendTraits(mother.smarts, father?.smarts ?: mother.smarts) + java.util.Random().nextGaussian() * 8.0
        val childCharisma = blendTraits(mother.charisma, father?.charisma ?: mother.charisma) + java.util.Random().nextGaussian() * 5.0
        val childAthleticism = blendTraits(mother.athleticism, father?.athleticism ?: mother.athleticism) + java.util.Random().nextGaussian() * 5.0

        return CharacterEntity(
            firstName = "",
            lastName = father?.lastName ?: mother.lastName,
            dateOfBirth = (year * 365.25 * 86400 * 1000).toLong(),
            gender = if (Random.nextFloat() < 0.5f) Gender.MALE else Gender.FEMALE,
            health = childHealth.coerceIn(10.0, 100.0),
            looks = childLooks.coerceIn(10.0, 100.0),
            smarts = childSmarts.coerceIn(10.0, 100.0),
            charisma = childCharisma.coerceIn(10.0, 100.0),
            athleticism = childAthleticism.coerceIn(10.0, 100.0),
            happiness = 70.0,
            energy = 80.0,
            hunger = 70.0,
            hygiene = 70.0,
            cash = 0.0,
            totalNetWorth = 0.0,
            dynastyId = mother.dynastyId ?: father?.dynastyId,
            parentsIds = listOfNotNull(mother.characterId, father?.characterId),
            generationNumber = maxOf(mother.generationNumber, father?.generationNumber ?: 1) + 1
        )
    }

    private fun blendTraits(trait1: Double, trait2: Double): Double {
        val base = (trait1 + trait2) / 2.0
        val mutation = java.util.Random().nextGaussian() * 8.0
        return base + mutation
    }

    private val generationalTraits = listOf(
        "Generational Wealth", "Criminal Bloodline", "Artistic Prodigy", "Academic Legacy",
        "Military Tradition", "Political Dynasty", "Business Empire", "Medical Heritage",
        "Athletic Bloodline", "Musical Gift", "Scholarly Lineage", "Pioneering Spirit"
    )

    fun determineGenerationalTrait(parentMemories: List<MemoryEntity>, parentWealth: Double): String? {
        if (Random.nextFloat() > 0.3f) return null
        val options = mutableListOf<Pair<String, Float>>()
        if (parentWealth > 1000000) options.add("Generational Wealth" to 0.4f)
        if (parentMemories.any { it.eventType == MemoryEventType.CRIME && !it.isRepressed }) options.add("Criminal Bloodline" to 0.3f)
        if (parentMemories.any { it.tags.contains("art") || it.tags.contains("creative") }) options.add("Artistic Prodigy" to 0.25f)
        if (parentMemories.any { it.tags.contains("academic") || it.tags.contains("education") }) options.add("Academic Legacy" to 0.25f)
        return if (options.isEmpty()) generationalTraits.random() else options.weightedRandom()
    }

    fun applyGenerationalTraitsToChild(child: CharacterEntity, trait: String): CharacterEntity {
        return when (trait) {
            "Generational Wealth" -> child.copy(cash = 100000.0, totalNetWorth = 100000.0)
            "Criminal Bloodline" -> child.copy(notoriety = 20, aggression = (child.aggression + 10.0).coerceIn(0.0, 100.0))
            "Artistic Prodigy" -> child.copy(creativity = (child.creativity + 20.0).coerceIn(0.0, 100.0), smarts = (child.smarts + 5.0).coerceIn(0.0, 100.0))
            "Academic Legacy" -> child.copy(smarts = (child.smarts + 15.0).coerceIn(0.0, 100.0), discipline = (child.discipline + 10.0).coerceIn(0.0, 100.0))
            else -> child
        }
    }

    fun distributeInheritance(deceased: CharacterEntity, heirs: List<CharacterEntity>, dynasty: DynastyEntity?): List<Pair<CharacterEntity, Double>> {
        val totalWealth = maxOf(0.0, deceased.cash + deceased.totalNetWorth * 0.7)
        val taxRate = 0.15
        val afterTax = totalWealth * (1.0 - taxRate)
        if (heirs.isEmpty()) return listOf()
        if (heirs.size == 1) return listOf(heirs.first() to afterTax)
        val rules = dynasty?.familySecrets?.let { if (it.isNotEmpty()) InheritanceType.PRIMOGENITURE else null } ?: InheritanceType.EQUAL_SPLIT
        val distribution = when (rules) {
            InheritanceType.PRIMOGENITURE -> {
                val primaryHeir = heirs.maxByOrNull { it.health + it.smarts } ?: heirs.first()
                val secondaryShare = afterTax * 0.3 / (heirs.size - 1)
                heirs.map { if (it == primaryHeir) it to afterTax * 0.7 else it to secondaryShare }
            }
            InheritanceType.EQUAL_SPLIT -> heirs.map { it to afterTax / heirs.size }
            InheritanceType.MOST_CAPABLE -> {
                val capable = heirs.maxByOrNull { it.health + it.smarts + it.charisma } ?: heirs.first()
                val secondaryShare = afterTax * 0.5 / (heirs.size - 1)
                heirs.map { if (it == capable) it to afterTax * 0.5 else it to secondaryShare }
            }
        }
        return distribution
    }

    fun applyGenerationalTrauma(character: CharacterEntity, parent: CharacterEntity): CharacterEntity {
        var c = character
        if (parent.stress > 60.0) c = c.copy(stress = (c.stress + 5.0).coerceIn(0.0, 100.0))
        if (parent.paranoia > 40.0) c = c.copy(paranoia = (c.paranoia + 5.0).coerceIn(0.0, 100.0))
        if (!parent.isAlive && parent.sanity < 40.0) c = c.copy(sanity = (c.sanity - 5.0).coerceIn(0.0, 100.0))
        return c
    }

    fun calculateLegacyPoints(currentPoints: Int, dynasty: DynastyEntity, character: CharacterEntity): Int {
        var points = currentPoints
        if (character.totalNetWorth > 10000000) points += 100
        else if (character.totalNetWorth > 1000000) points += 50
        else if (character.totalNetWorth > 100000) points += 20
        if (character.reputation > 80) points += 30
        if (character.childIds.size >= 4) points += 25
        if (dynasty.currentGeneration >= 5) points += 50
        return points
    }

    enum class InheritanceType { PRIMOGENITURE, EQUAL_SPLIT, MOST_CAPABLE }
}

fun <T> List<Pair<T, Float>>.weightedRandom(): T {
    val totalWeight = sumOf { it.second.toDouble() }
    if (totalWeight <= 0.0) return first().first
    var r = Random.nextDouble() * totalWeight
    for ((item, weight) in this) { r -= weight; if (r <= 0.0) return item }
    return last().first
}
