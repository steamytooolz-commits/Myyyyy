// =========================================
// File: domain/engine/PrisonEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.CrimeRecordEntity
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

data class PrisonTickResult(
    val character: CharacterEntity,
    val consequences: List<Consequence> = emptyList(),
    val newMemories: List<com.example.lifesim.data.local.entity.MemoryEntity> = emptyList(),
    val isDead: Boolean = false,
    val deathCause: String? = null
)

@Singleton
class PrisonEngine @Inject constructor() {

    private val gangNames = listOf("The Aryan Brotherhood", "La Eme", "Nuestra Familia", "Black Guerrilla Family",
        "Bloods", "Crips", "MS-13", "Latin Kings", "Yakuza Syndicate", "Mafia")
    private val prisonJobs = listOf("Kitchen duty", "Laundry", "Library assistant", "Janitorial",
        "Workshop", "Grounds crew", "Administrative clerk", "Infirmary orderly")
    private val contrabandTypes = listOf("Mobile phone", "Weapon (shank)", "Drugs", "Tobacco", "Alcohol", "Money", "Tools")

    data class PrisonActionResult(
        val character: CharacterEntity,
        val message: String,
        val success: Boolean
    )

    fun processPrisonTick(
        character: CharacterEntity,
        crimeRecords: List<CrimeRecordEntity>,
        currentYear: Int
    ): PrisonTickResult {
        var c = character
        val consequences = mutableListOf<Consequence>()
        val newMemories = mutableListOf<com.example.lifesim.data.local.entity.MemoryEntity>()
        c = c.copy(prisonYearsServed = c.prisonYearsServed + 1)

        // Daily prison life degradation
        c = c.copy(
            health = (c.health - 3.0).coerceIn(0.0, 100.0),
            happiness = (c.happiness - 6.0).coerceIn(0.0, 100.0),
            stress = (c.stress + 5.0).coerceIn(0.0, 100.0),
            reputation = (c.reputation - 3).coerceIn(0, 100),
            notoriety = (c.notoriety + 2).coerceIn(0, 100),
            sanity = (c.sanity - 2.0).coerceIn(0.0, 100.0)
        )

        // Gang dynamics
        c = processGangDynamics(c, consequences)

        // Prison job income and benefits
        c = processPrisonJob(c, consequences)

        // Violence and disciplinary events
        c = processPrisonViolence(c, consequences, newMemories, currentYear)

        // Contraband events
        c = processContrabandEvents(c, consequences)

        // Addiction development in prison
        if (!c.isAddicted && Random.nextFloat() < 0.08f) {
            c = c.copy(isAddicted = true, addictionType = "drugs", addictionSeverity = 1)
            consequences.add(Consequence("You were introduced to drugs by other inmates.", mapOf("health" to -3.0, "happiness" to 3.0, "stress" to -5.0), 2))
        }

        // Parole eligibility check
        val totalSentence = crimeRecords.sumOf { it.prisonSentenceYears }
        if (totalSentence > 0 && c.prisonYearsServed >= totalSentence) {
            c = c.copy(isInPrison = false, prisonGang = null, prisonJob = null, prisonEscapeProgress = 0,
                prisonContraband = emptyList())
            consequences.add(Consequence("You've served your time. You're a free person again!", mapOf("happiness" to 20.0, "stress" to -20.0), 1))
        }

        // Random isolation (solitary confinement) for disciplinary issues
        if (c.prisonDisciplinaryRecord > 5 && Random.nextFloat() < 0.2f) {
            c = c.copy(prisonDisciplinaryRecord = 0)
            consequences.add(Consequence("You were thrown into solitary confinement for your behavior.", mapOf("sanity" to -10.0, "stress" to 15.0, "happiness" to -10.0), 4))
            c = c.copy(sanity = (c.sanity - 10.0).coerceIn(0.0, 100.0), stress = (c.stress + 15.0).coerceIn(0.0, 100.0))
        }

        // Death from prison violence or health
        if (c.health <= 0.0 || c.sanity <= 0.0) {
            val cause = if (c.health <= 0.0) "died due to poor health in prison" else "lost their mind in prison"
            return PrisonTickResult(c.copy(isAlive = false), consequences, newMemories, true, cause)
        }

        // Prison education chance
        if (c.prisonEducationLevel < 3 && Random.nextFloat() < 0.15f) {
            c = c.copy(prisonEducationLevel = c.prisonEducationLevel + 1, smarts = (c.smarts + 5.0).coerceIn(0.0, 100.0))
            consequences.add(Consequence("You earned a prison education certificate!", mapOf("smarts" to 5.0, "discipline" to 3.0), 1))
        }

        return PrisonTickResult(c, consequences, newMemories)
    }

    fun joinGang(character: CharacterEntity): PrisonActionResult {
        if (character.prisonGang != null) return PrisonActionResult(character, "Already in a gang.", false)
        if (!character.isInPrison) return PrisonActionResult(character, "You're not in prison.", false)

        val gang = gangNames.random()
        val successChance = 0.3 + character.aggression / 100.0 * 0.4 + character.notoriety * 0.003
        val success = Random.nextFloat() < successChance

        if (success) {
            return PrisonActionResult(
                character.copy(prisonGang = gang, hasPrisonAllies = true, aggression = (character.aggression + 10.0).coerceIn(0.0, 100.0)),
                "You were accepted into $gang. You now have protection, but also enemies.",
                true
            )
        }
        return PrisonActionResult(
            character.copy(hasPrisonEnemies = true),
            "The gang rejected you. They now see you as a target.",
            false
        )
    }

    fun takePrisonJob(character: CharacterEntity): PrisonActionResult {
        if (character.prisonJob != null) return PrisonActionResult(character, "Already working ${character.prisonJob}.", false)
        if (!character.isInPrison) return PrisonActionResult(character, "Not in prison.", false)

        val job = prisonJobs.random()
        return PrisonActionResult(
            character.copy(prisonJob = job, discipline = (character.discipline + 5.0).coerceIn(0.0, 100.0)),
            "You start working $job. It's not glamorous, but it passes the time.",
            true
        )
    }

    fun attemptEscape(character: CharacterEntity): PrisonActionResult {
        if (!character.isInPrison) return PrisonActionResult(character, "You're not in prison.", false)

        val progress = character.prisonEscapeProgress + 1
        val escapeChance = (character.smarts * 0.3 + character.athleticism * 0.3 + character.charisma * 0.1 + progress * 5.0
            - character.prisonDisciplinaryRecord * 2.0).coerceIn(0.0, 0.6)

        if (Random.nextDouble() < escapeChance) {
            return PrisonActionResult(
                character.copy(isInPrison = false, prisonGang = null, prisonJob = null, prisonEscapeProgress = 0,
                    prisonContraband = emptyList(), notoriety = character.notoriety + 20,
                    karma = (character.karma - 5).coerceIn(0, 100)),
                "You escaped! The alarms are blaring but you're free!",
                true
            )
        }

        // Failed escape - caught
        val caught = Random.nextFloat() < 0.7f
        if (caught) {
            return PrisonActionResult(
                character.copy(prisonEscapeProgress = 0, prisonDisciplinaryRecord = character.prisonDisciplinaryRecord + 3,
                    health = (character.health - 10.0).coerceIn(0.0, 100.0)),
                "You were caught trying to escape. Sent to solitary and your sentence increased.",
                false
            )
        }

        // Not caught but failed - progress preserved
        return PrisonActionResult(
            character.copy(prisonEscapeProgress = progress),
            "Your escape attempt failed but no one noticed. Progress: $progress/10",
            true
        )
    }

    fun requestParole(character: CharacterEntity, crimeRecords: List<CrimeRecordEntity>): PrisonActionResult {
        if (!character.isInPrison) return PrisonActionResult(character, "Not in prison.", false)

        val totalSentence = crimeRecords.sumOf { it.prisonSentenceYears }
        val minServe = (totalSentence * 0.5).toInt()
        if (character.prisonYearsServed < minServe) {
            return PrisonActionResult(character, "Not eligible yet. Need to serve $minServe years (served: ${character.prisonYearsServed}).", false)
        }

        val paroleChance = 0.3 + character.charisma * 0.003 + character.prisonEducationLevel * 0.05
            - character.prisonDisciplinaryRecord * 0.02 - (character.prisonGang != null).compareTo(false) * 0.1

        if (Random.nextFloat() < paroleChance) {
            return PrisonActionResult(
                character.copy(isInPrison = false, prisonGang = null, prisonJob = null, prisonEscapeProgress = 0,
                    prisonContraband = emptyList()),
                "Parole granted! You're released on good behavior.",
                true
            )
        }
        return PrisonActionResult(
            character.copy(prisonDisciplinaryRecord = character.prisonDisciplinaryRecord + 1),
            "Parole denied. The board cited your record and behavior.",
            false
        )
    }

    fun acquireContraband(character: CharacterEntity): PrisonActionResult {
        if (!character.isInPrison) return PrisonActionResult(character, "Not in prison.", false)
        val item = contrabandTypes.random()
        val success = Random.nextFloat() < 0.4f
        if (success) {
            return PrisonActionResult(
                character.copy(prisonContraband = character.prisonContraband + item),
                "You acquired $item on the black market.",
                true
            )
        }
        return PrisonActionResult(
            character.copy(prisonDisciplinaryRecord = character.prisonDisciplinaryRecord + 2),
            "You got caught trying to acquire contraband. Disciplinary action taken.",
            false
        )
    }

    private fun processGangDynamics(c: CharacterEntity, consequences: MutableList<Consequence>): CharacterEntity {
        var updated = c
        if (updated.prisonGang != null) {
            // Gang protection reduces violence risk
            if (Random.nextFloat() < 0.1f) {
                consequences.add(Consequence("Your gang ${updated.prisonGang} has your back.", mapOf("happiness" to 3.0, "stress" to -3.0), 1))
            }
            // Gang demands
            if (Random.nextFloat() < 0.15f) {
                updated = updated.copy(aggression = (updated.aggression + 3.0).coerceIn(0.0, 100.0), notoriety = updated.notoriety + 2)
                consequences.add(Consequence("Your gang demands loyalty. You had to prove yourself.", mapOf("aggression" to 3.0, "notoriety" to 2.0), 2))
            }
        } else if (updated.isInPrison && Random.nextFloat() < 0.2f) {
            // No gang - higher risk of being targeted
            updated = updated.copy(stress = (updated.stress + 3.0).coerceIn(0.0, 100.0))
        }
        return updated
    }

    private fun processPrisonJob(c: CharacterEntity, consequences: MutableList<Consequence>): CharacterEntity {
        if (c.prisonJob == null) return c
        var updated = c
        updated = updated.copy(cash = updated.cash + 25.0, discipline = (updated.discipline + 2.0).coerceIn(0.0, 100.0))
        if (Random.nextFloat() < 0.05f) {
            consequences.add(Consequence("Good work in ${updated.prisonJob} earned you respect.", mapOf("reputation" to 3.0, "discipline" to 3.0), 1))
        }
        return updated
    }

    private fun processPrisonViolence(
        c: CharacterEntity,
        consequences: MutableList<Consequence>,
        memories: MutableList<com.example.lifesim.data.local.entity.MemoryEntity>,
        currentYear: Int
    ): CharacterEntity {
        var updated = c
        val violenceChance = if (updated.prisonGang != null) 0.05f else 0.12f +
            (updated.hasPrisonEnemies.compareTo(false) * 0.08f) + (updated.aggression / 100.0 * 0.1).toFloat()

        if (Random.nextFloat() < violenceChance) {
            val injured = Random.nextFloat() < 0.3f
            if (injured) {
                updated = updated.copy(health = (updated.health - 15.0).coerceIn(0.0, 100.0))
                consequences.add(Consequence("You got into a fight and were injured. Sent to the infirmary.", mapOf("health" to -15.0, "stress" to 10.0), 3))
            } else {
                updated = updated.copy(aggression = (updated.aggression + 5.0).coerceIn(0.0, 100.0))
                consequences.add(Consequence("You got into a fight but held your own. The other inmate backed down.", mapOf("aggression" to 5.0, "notoriety" to 3.0), 2))
            }
            updated = updated.copy(prisonDisciplinaryRecord = updated.prisonDisciplinaryRecord + 1)
        }

        // Check for enemy attacks
        if (updated.hasPrisonEnemies && Random.nextFloat() < 0.1f) {
            updated = updated.copy(health = (updated.health - 8.0).coerceIn(0.0, 100.0), stress = (updated.stress + 8.0).coerceIn(0.0, 100.0))
            consequences.add(Consequence("Your enemies attacked you when you let your guard down.", mapOf("health" to -8.0, "stress" to 8.0), 3))
        }

        return updated
    }

    private fun processContrabandEvents(c: CharacterEntity, consequences: MutableList<Consequence>): CharacterEntity {
        if (c.prisonContraband.isEmpty()) return c
        var updated = c

        // Random shakedown
        if (Random.nextFloat() < 0.15f) {
            val found = Random.nextFloat() < 0.4f
            if (found) {
                consequences.add(Consequence("Guard shakedown! Your contraband was confiscated.", mapOf("stress" to 8.0, "happiness" to -5.0), 2))
                updated = updated.copy(prisonContraband = emptyList(), prisonDisciplinaryRecord = updated.prisonDisciplinaryRecord + 2)
            } else {
                consequences.add(Consequence("Guard shakedown, but your contraband was well-hidden.", mapOf("stress" to 3.0), 1))
            }
        }

        // Using contraband for benefits
        if (updated.prisonContraband.contains("Mobile phone") && Random.nextFloat() < 0.1f) {
            consequences.add(Consequence("You used the contraband phone to coordinate with the outside world.", mapOf("happiness" to 3.0, "stress" to -3.0), 1))
        }
        if (updated.prisonContraband.contains("Drugs") && Random.nextFloat() < 0.2f) {
            updated = updated.copy(happiness = (updated.happiness + 5.0).coerceIn(0.0, 100.0),
                stress = (updated.stress - 5.0).coerceIn(0.0, 100.0))
        }

        return updated
    }
}
