// =========================================
// File: domain/engine/MilitaryEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

data class MilitaryRank(val title: String, val payGrade: Int, val basePay: Double, val isOfficer: Boolean)

@Singleton
class MilitaryEngine @Inject constructor() {

    private val enlistedRanks = listOf(
        MilitaryRank("Private (E-1)", 1, 20000.0, false),
        MilitaryRank("Private (E-2)", 2, 22500.0, false),
        MilitaryRank("Private First Class (E-3)", 3, 24000.0, false),
        MilitaryRank("Specialist (E-4)", 4, 28000.0, false),
        MilitaryRank("Sergeant (E-5)", 5, 33000.0, false),
        MilitaryRank("Staff Sergeant (E-6)", 6, 38000.0, false),
        MilitaryRank("Sergeant First Class (E-7)", 7, 45000.0, false),
        MilitaryRank("Master Sergeant (E-8)", 8, 52000.0, false),
        MilitaryRank("Sergeant Major (E-9)", 9, 60000.0, false)
    )

    private val officerRanks = listOf(
        MilitaryRank("Second Lieutenant (O-1)", 1, 38000.0, true),
        MilitaryRank("First Lieutenant (O-2)", 2, 45000.0, true),
        MilitaryRank("Captain (O-3)", 3, 55000.0, true),
        MilitaryRank("Major (O-4)", 4, 65000.0, true),
        MilitaryRank("Lieutenant Colonel (O-5)", 5, 78000.0, true),
        MilitaryRank("Colonel (O-6)", 6, 95000.0, true),
        MilitaryRank("Brigadier General (O-7)", 7, 120000.0, true),
        MilitaryRank("Major General (O-8)", 8, 140000.0, true),
        MilitaryRank("Lieutenant General (O-9)", 9, 165000.0, true),
        MilitaryRank("General (O-10)", 10, 190000.0, true)
    )

    fun enlist(character: CharacterEntity, asOfficer: Boolean = false): EnlistResult {
        val age = (2024 - (character.dateOfBirth / 31557600000L).toInt()).coerceAtLeast(0)
        if (age < 18) return EnlistResult(false, "Too young to enlist.", character)

        val firstRank = if (asOfficer) officerRanks.first() else enlistedRanks.first()
        return EnlistResult(true, "You enlisted in the military! Boot camp starts immediately.",
            character.copy(isInMilitary = true, militaryRankTitle = firstRank.title, militaryPayGrade = firstRank.payGrade,
                militaryYearsServed = 0, discipline = (character.discipline + 20.0).coerceIn(0.0, 100.0),
                athleticism = (character.athleticism + 10.0).coerceIn(0.0, 100.0)))
    }

    fun serveYear(character: CharacterEntity): MilitaryYearResult {
        if (!character.isInMilitary) return MilitaryYearResult(false, "Not in military.", character, emptyList())

        var c = character.copy(militaryYearsServed = character.militaryYearsServed + 1)
        val events = mutableListOf<String>()
        val currentRankTitle = c.militaryRankTitle
        val currentPayGrade = c.militaryPayGrade

        // Base military life effects
        c = c.copy(health = (c.health - 2.0).coerceIn(0.0, 100.0), stress = (c.stress + 5.0).coerceIn(0.0, 100.0),
            discipline = (c.discipline + 3.0).coerceIn(0.0, 100.0), athleticism = (c.athleticism + 2.0).coerceIn(0.0, 100.0))

        // Deployment
        if (Random.nextFloat() < 0.25f) {
            val deployment = processDeployment(c)
            c = deployment.character
            events.addAll(deployment.events)
            if (deployment.ptsd) {
                c = c.copy(sanity = (c.sanity - 15.0).coerceIn(0.0, 100.0), paranoia = (c.paranoia + 10.0).coerceIn(0.0, 100.0))
                events.add("You developed PTSD from combat. You need therapy.")
            }
        }

        // Determine if officer based on pay grade
        val isOfficer = c.militaryPayGrade > 9
        val allRanks = if (isOfficer) officerRanks else enlistedRanks
        val currentIdx = allRanks.indexOfFirst { it.title == c.militaryRankTitle }
        if (currentIdx >= 0 && currentIdx < allRanks.size - 1 && c.militaryYearsServed > (currentIdx + 1) * 2) {
            if (Random.nextFloat() < 0.4f) {
                val newRank = allRanks[currentIdx + 1]
                c = c.copy(militaryRankTitle = newRank.title, militaryPayGrade = newRank.payGrade + (if (isOfficer) 9 else 0),
                    cash = c.cash + newRank.basePay, reputation = (c.reputation + 5).coerceIn(0, 100))
                events.add("Promoted to ${newRank.title}!")
            }
        }

        // Pay
        val basePay = if (currentPayGrade > 9) officerRanks.getOrNull(currentPayGrade - 10)?.basePay ?: 20000.0 else enlistedRanks.getOrNull(currentPayGrade - 1)?.basePay ?: 20000.0
        c = c.copy(cash = c.cash + basePay * 0.0833)

        // Combat medals
        if (c.militaryCombatDeployments > 0 && Random.nextFloat() < 0.1f) {
            val medals = listOf("Purple Heart", "Bronze Star", "Silver Star", "Medal of Honor", "Army Commendation Medal")
            val medal = medals.random()
            c = c.copy(militaryMedals = c.militaryMedals + medal, reputation = (c.reputation + 15).coerceIn(0, 100))
            events.add("You were awarded the $medal for valor in combat!")
        }

        // Death in combat
        if (c.militaryCombatDeployments > 0 && Random.nextFloat() < 0.01f) {
            return MilitaryYearResult(false, "You were killed in action.", c.copy(isAlive = false), events)
        }

        return MilitaryYearResult(true, "Military service continues. Rank: ${currentRankTitle ?: "Recruit"}", c, events)
    }

    private fun processDeployment(c: CharacterEntity): DeploymentResult {
        var char = c.copy(militaryCombatDeployments = c.militaryCombatDeployments + 1)
        val events = mutableListOf<String>()
        val ptsd = Random.nextFloat() < 0.2f

        when (Random.nextInt(5)) {
            0 -> events.add("Deployed to a combat zone. You saw heavy action.")
            1 -> events.add("Peacekeeping mission in a foreign country.")
            2 -> events.add("Humanitarian aid mission after a natural disaster.")
            3 -> events.add("Training exercise with allied forces.")
            4 -> events.add("Counter-terrorism operation.")
        }

        char = char.copy(aggression = (char.aggression + 5.0).coerceIn(0.0, 100.0),
            stress = (char.stress + 10.0).coerceIn(0.0, 100.0),
            notoriety = char.notoriety + 2)

        return DeploymentResult(char, events, ptsd)
    }

    fun discharge(character: CharacterEntity, honorable: Boolean = true): EnlistResult {
        if (!character.isInMilitary) return EnlistResult(false, "Not in military.", character)
        val giBill = character.militaryYearsServed >= 2
        return EnlistResult(true, if (honorable) "Honorably discharged. Thank you for your service!" else "Discharged.",
            character.copy(isInMilitary = false, militaryRankTitle = null, militaryPayGrade = 0,
                reputation = (character.reputation + if (honorable) 15 else -10).coerceIn(0, 100),
                cash = if (giBill) character.cash + 50000.0 else character.cash))
    }
}

data class EnlistResult(val success: Boolean, val message: String, val character: CharacterEntity)
data class MilitaryYearResult(val success: Boolean, val message: String, val character: CharacterEntity, val events: List<String>)
data class DeploymentResult(val character: CharacterEntity, val events: List<String>, val ptsd: Boolean)
