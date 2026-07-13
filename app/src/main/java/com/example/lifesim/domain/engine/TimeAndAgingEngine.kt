package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.LifeStage
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class TimeAndAgingEngine @Inject constructor() {

    fun advanceMonth(character: CharacterEntity, age: Int, inFosterCare: Boolean = false): CharacterEntity {
        var c = character
        c = c.copy(energy = (c.energy - 2.0).coerceIn(0.0, 100.0))
        c = c.copy(hunger = (c.hunger - 1.5).coerceIn(0.0, 100.0))
        c = c.copy(hygiene = (c.hygiene - 1.0).coerceIn(0.0, 100.0))

        if (age < 18) {
            // Parents or foster care provide basic needs
            c = c.copy(hunger = (c.hunger + 1.5).coerceIn(0.0, 100.0))
            c = c.copy(hygiene = (c.hygiene + 1.0).coerceIn(0.0, 100.0))
            
            if (inFosterCare) {
                c = c.copy(happiness = (c.happiness - 0.5).coerceIn(0.0, 100.0))
            }
        }

        if (c.hunger < 20.0) c = c.copy(health = (c.health - 3.0).coerceIn(0.0, 100.0))
        if (c.energy < 20.0) c = c.copy(happiness = (c.happiness - 2.0).coerceIn(0.0, 100.0))
        if (c.hygiene < 20.0) c = c.copy(happiness = (c.happiness - 1.5).coerceIn(0.0, 100.0))
        return applyAgeRelatedDecay(c)
    }

    fun advanceYear(character: CharacterEntity, birthYear: Int, currentYear: Int): CharacterEntity {
        var c = character
        val age = currentYear - birthYear
        if (age < 0) return c
        c = c.copy(energy = 100.0)
        c = applyAgeModifiers(c, age)
        c = applyStressDecay(c)
        c = applyRelationshipDecay(c)
        return c
    }

    private fun applyAgeRelatedDecay(c: CharacterEntity): CharacterEntity {
        var healthDecay = 0.5 * (1.0 + c.stress / 200.0)
        if (c.athleticism > 60) healthDecay *= 0.7
        if (c.addictionSeverity > 3) healthDecay *= 1.5
        return c.copy(health = (c.health - healthDecay).coerceIn(0.0, 100.0))
    }

    private fun applyAgeModifiers(c: CharacterEntity, age: Int): CharacterEntity {
        val ageMult = if (age < 30) 1.0 else 1.0 + (age - 30) * 0.05
        var health = c.health; var looks = c.looks; var smarts = c.smarts; var athleticism = c.athleticism
        when {
            age in 31..60 -> { health -= 0.5 * ageMult * 2; looks -= 0.3 * ageMult * 2; athleticism -= 0.4 * ageMult * 2 }
            age > 60 -> { health -= 1.0 * ageMult * 2; looks -= 0.5 * ageMult * 2; athleticism -= 0.6 * ageMult * 2; smarts -= 0.8 }
        }
        when (age) { in 18..30 -> smarts += 1.5; in 31..60 -> smarts += 0.5 }
        return c.copy(health = health.coerceIn(0.0, 100.0), looks = looks.coerceIn(0.0, 100.0),
            smarts = smarts.coerceIn(0.0, 100.0), athleticism = athleticism.coerceIn(0.0, 100.0))
    }

    private fun applyStressDecay(c: CharacterEntity): CharacterEntity {
        val delta = if (c.stress > 50) 3.0 else -2.0
        return c.copy(stress = (c.stress + delta).coerceIn(0.0, 100.0))
    }

    private fun applyRelationshipDecay(c: CharacterEntity): CharacterEntity {
        val delta = if (c.spouseId != null) 2.0 else if (c.parentsIds.isNotEmpty()) 1.0 else -1.0
        return c.copy(happiness = (c.happiness + delta).coerceIn(0.0, 100.0))
    }

    fun getLifeStage(age: Int): LifeStage = when {
        age < 13 -> LifeStage.CHILD; age < 18 -> LifeStage.TEENAGER; age < 25 -> LifeStage.YOUNG_ADULT
        age < 45 -> LifeStage.ADULT; age < 60 -> LifeStage.MIDDLE_AGED; age < 75 -> LifeStage.SENIOR
        else -> LifeStage.ELDERLY
    }

    fun calculateDeathProbability(character: CharacterEntity, birthYear: Int, currentYear: Int): Double {
        val age = currentYear - birthYear
        if (character.health <= 0.0) return 1.0
        if (age >= 120) return 1.0
        var prob = when { age < 5 -> 0.02; age < 20 -> 0.001; age < 40 -> 0.002; age < 60 -> 0.01
            age < 70 -> 0.03; age < 80 -> 0.08; age < 90 -> 0.15; age < 100 -> 0.3; age < 110 -> 0.6; else -> 0.8 }
        if (character.health < 20) prob += 0.15
        if (character.health < 10) prob += 0.3
        if (character.stress > 80) prob += 0.05
        if (character.isAddicted) prob += 0.03
        if (character.sanity < 30) prob += 0.05
        return prob.coerceIn(0.0, 0.99)
    }
}
