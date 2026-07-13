package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.CrimeRecordEntity
import com.example.lifesim.data.local.entity.CrimeType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.random.Random

@Singleton
class CrimeAndLegalEngine @Inject constructor() {

    data class CrimeResult(val success: Boolean, val caught: Boolean, val severity: Int, val bailAmount: Double, val sentenceYears: Int, val description: String)

    fun attemptCrime(character: CharacterEntity, crimeType: CrimeType, equipmentBonus: Double = 0.0, stealthBonus: Double = 0.0): CrimeResult {
        val securityLevel = when (crimeType) {
            CrimeType.MURDER, CrimeType.ROBBERY, CrimeType.KIDNAPPING -> 8
            CrimeType.BURGLARY, CrimeType.ASSAULT, CrimeType.ARSON -> 6
            CrimeType.THEFT, CrimeType.FRAUD, CrimeType.HACKING -> 4
            CrimeType.VANDALISM, CrimeType.TRESPASSING, CrimeType.DUI -> 2
            else -> 5
        }
        val successProb = (character.smarts * 0.3 + character.athleticism * 0.3 + equipmentBonus * 0.2 + stealthBonus * 0.2 - securityLevel * 0.5).coerceIn(0.0, 1.0)
        val success = Random.nextDouble() < successProb
        val caught = !success || (success && Random.nextDouble() < (1.0 - successProb) * 0.5)
        val severity = when (crimeType) {
            CrimeType.MURDER, CrimeType.KIDNAPPING, CrimeType.ARSON -> 8..10
            CrimeType.ROBBERY, CrimeType.ASSAULT, CrimeType.BURGLARY -> 5..7
            CrimeType.FRAUD, CrimeType.HACKING, CrimeType.IDENTITY_THEFT -> 3..6
            CrimeType.THEFT, CrimeType.VANDALISM, CrimeType.TRESPASSING -> 1..3
            else -> 2..5
        }.random()
        val bailAmount = severity * 5000.0
        val sentenceYears = if (caught) (severity * Random.nextDouble(0.3, 1.0)).roundToInt().coerceAtLeast(0) else 0
        val description = when {
            success && !caught -> "You pulled it off! No one suspects a thing."
            success && caught -> "You succeeded but got caught. The evidence was overwhelming."
            !success && caught -> "You failed and were apprehended immediately."
            else -> "Your attempt failed, but you managed to escape without being identified."
        }
        return CrimeResult(success, caught, severity, bailAmount, sentenceYears, description)
    }

    fun servePrisonYear(character: CharacterEntity, records: List<CrimeRecordEntity>): CharacterEntity {
        var c = character.copy(
            health = (character.health - 5.0).coerceIn(0.0, 100.0),
            happiness = (character.happiness - 8.0).coerceIn(0.0, 100.0),
            stress = (character.stress + 8.0).coerceIn(0.0, 100.0),
            reputation = (character.reputation - 5).coerceIn(0, 100)
        )
        if (Random.nextFloat() < 0.1f) {
            c = c.copy(aggression = (c.aggression + 5.0).coerceIn(0.0, 100.0), notoriety = c.notoriety + 3)
        }
        if (Random.nextFloat() < 0.15f) {
            c = c.copy(smarts = (c.smarts + 3.0).coerceIn(0.0, 100.0)) // prison education
        }
        if (Random.nextFloat() < 0.05f) {
            c = c.copy(isAddicted = true, addictionType = "drugs", addictionSeverity = c.addictionSeverity + 1)
        }
        return c
    }

    fun checkParoleEligibility(record: CrimeRecordEntity, yearsServed: Int): Boolean {
        val minServe = (record.prisonSentenceYears * 0.5).roundToInt()
        return yearsServed >= minServe && Random.nextFloat() < 0.4f
    }

    fun calculateBailReduction(character: CharacterEntity, bailAmount: Double): Double {
        var reduction = 0.0
        if (character.charisma > 60) reduction += bailAmount * 0.1
        if (character.reputation > 70) reduction += bailAmount * 0.15
        if (character.cash > bailAmount) reduction += bailAmount * 0.1
        return (bailAmount - reduction).coerceAtLeast(0.0)
    }

    fun attemptEscape(athleticism: Double, smarts: Double, prisonLevel: Int = 5): Boolean {
        val escapeProb = (athleticism * 0.4 + smarts * 0.3 - prisonLevel * 0.05).coerceIn(0.0, 0.8)
        return Random.nextDouble() < escapeProb
    }

    fun generateCrimeRecord(character: CharacterEntity, result: CrimeResult, crimeType: CrimeType, year: Int): CrimeRecordEntity {
        return CrimeRecordEntity(
            characterId = character.characterId,
            crimeType = crimeType,
            severity = result.severity,
            yearCommitted = year,
            tickCommitted = System.currentTimeMillis(),
            bailAmount = result.bailAmount,
            isConvicted = result.caught,
            prisonSentenceYears = result.sentenceYears,
            finesPaid = 0.0,
            witnessIds = if (result.caught) listOf("anonymous") else emptyList()
        )
    }
}
