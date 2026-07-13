// =========================================
// File: domain/engine/AddictionEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddictionEngine @Inject constructor() {

    data class AddictionTickResult(
        val character: CharacterEntity,
        val consequences: List<Consequence> = emptyList(),
        val triggeredRelapse: Boolean = false
    )

    fun processTick(character: CharacterEntity): AddictionTickResult {
        var c = character
        val consequences = mutableListOf<Consequence>()
        var relapse = false

        if (!c.isAddicted) return AddictionTickResult(c)

        when {
            // Active withdrawal
            c.isInWithdrawal -> {
                c = processWithdrawal(c)
                c = c.copy(withdrawalDuration = c.withdrawalDuration + 1)

                // Check if withdrawal is over (lasts 6-12 ticks)
                if (c.withdrawalDuration > 6 + c.addictionSeverity * 2) {
                    c = c.copy(isInWithdrawal = false, withdrawalDuration = 0)
                    // Recovery begins
                    if (c.addictionRecoveryProgress <= 0) {
                        c = c.copy(addictionRecoveryProgress = 1)
                    }
                }

                // Withdrawal can kill if severe enough
                if (c.health <= 0.0) {
                    c = c.copy(isAlive = false)
                    consequences.add(Consequence("Your body couldn't withstand the withdrawal.", mapOf("health" to -100.0), 5))
                }

                // High stress during withdrawal increases relapse chance
                if (c.stress > 70 && Random.nextFloat() < 0.15f) {
                    relapse = true
                    consequences.add(Consequence("The stress of withdrawal is unbearable. You relapse.", mapOf("happiness" to 5.0, "health" to -5.0, "cash" to -200.0), 3))
                }
            }

            // In recovery
            c.addictionRecoveryProgress > 0 -> {
                c = c.copy(addictionRecoveryProgress = c.addictionRecoveryProgress + 1)
                c = processRecoveryBenefits(c)

                // Full recovery after 12-20 ticks
                if (c.addictionRecoveryProgress >= 12 + c.addictionSeverity * 2) {
                    if (Random.nextFloat() < 0.85f) {
                        c = c.copy(isAddicted = false, addictionSeverity = 0, addictionRecoveryProgress = 0,
                            addictionTicksWithoutUse = 0, addictionType = null)
                        consequences.add(Consequence("You've overcome your addiction! A fresh start.", mapOf("happiness" to 15.0, "health" to 10.0, "sanity" to 10.0), 1))
                    }
                }

                // Relapse during recovery
                if (c.stress > 80 && Random.nextFloat() < 0.1f) {
                    relapse = true
                    c = c.copy(addictionSeverity = (c.addictionSeverity + 1).coerceAtMost(10),
                        addictionRecoveryProgress = 0)
                    consequences.add(Consequence("Recovery is hard. A moment of weakness leads to a relapse.", mapOf("stress" to 10.0, "happiness" to -5.0), 4))
                }
            }

            // Using regularly (active addiction, no recovery)
            else -> {
                c = c.copy(addictionTicksWithoutUse = c.addictionTicksWithoutUse + 1)

                // Needs the substance - triggers withdrawal
                if (c.addictionTicksWithoutUse > 3 && c.addictionSeverity > 2) {
                    c = c.copy(isInWithdrawal = true, addictionTicksWithoutUse = 0)
                    consequences.add(Consequence("Withdrawal symptoms begin. You need ${c.addictionType ?: "your fix"}.", mapOf("stress" to 12.0, "health" to -5.0, "happiness" to -10.0), 3))
                }

                // If managed to stay away, start recovery
                if (c.addictionTicksWithoutUse > 10 && c.addictionSeverity <= 2) {
                    c = c.copy(addictionRecoveryProgress = 1, addictionTicksWithoutUse = 0)
                }
            }
        }

        // Auto-spending on addiction
        if (c.isAddicted && !c.isInWithdrawal && c.addictionRecoveryProgress <= 0 && c.cash > 0) {
            val spend = (c.cash * 0.05 * c.addictionSeverity).coerceIn(10.0, c.cash * 0.3)
            c = c.copy(cash = c.cash - spend)
        }

        return AddictionTickResult(c, consequences, relapse)
    }

    fun applySubstance(character: CharacterEntity, substanceType: String): CharacterEntity {
        var c = character.copy(
            addictionTicksWithoutUse = 0,
            isInWithdrawal = false,
            withdrawalDuration = 0
        )

        // Increase severity over time
        if (c.isAddicted && c.addictionType == substanceType) {
            c = c.copy(addictionSeverity = (c.addictionSeverity + 1).coerceIn(0, 10))
        } else if (!c.isAddicted) {
            // First time - chance of developing addiction
            val addictionChance = when (substanceType) {
                "alcohol" -> 0.05f
                "drugs" -> 0.15f
                "gambling" -> 0.08f
                "smoking" -> 0.10f
                else -> 0.03f
            }
            if (Random.nextFloat() < addictionChance) {
                c = c.copy(isAddicted = true, addictionType = substanceType, addictionSeverity = 1)
            }
        }

        // Short-term effects
        c = c.copy(
            stress = (c.stress - c.addictionSeverity * 3.0).coerceIn(0.0, 100.0),
            happiness = (c.happiness + c.addictionSeverity * 4.0).coerceIn(0.0, 100.0)
        )

        return c
    }

    private fun processWithdrawal(c: CharacterEntity): CharacterEntity {
        val severityMult = c.addictionSeverity
        return c.copy(
            health = (c.health - 3.0 * severityMult).coerceIn(0.0, 100.0),
            happiness = (c.happiness - 8.0 * severityMult).coerceIn(0.0, 100.0),
            stress = (c.stress + 6.0 * severityMult).coerceIn(0.0, 100.0),
            energy = (c.energy - 5.0 * severityMult).coerceIn(0.0, 100.0),
            paranoia = (c.paranoia + 3.0 * severityMult).coerceIn(0.0, 100.0),
            sanity = (c.sanity - 4.0 * severityMult).coerceIn(0.0, 100.0)
        )
    }

    private fun processRecoveryBenefits(c: CharacterEntity): CharacterEntity {
        return c.copy(
            health = (c.health + 2.0).coerceIn(0.0, 100.0),
            happiness = (c.happiness + 1.0).coerceIn(0.0, 100.0),
            discipline = (c.discipline + 2.0).coerceIn(0.0, 100.0)
        )
    }

    fun getSeverityLabel(severity: Int): String = when {
        severity <= 2 -> "Mild"
        severity <= 4 -> "Moderate"
        severity <= 6 -> "Severe"
        severity <= 8 -> "Critical"
        else -> "Life-threatening"
    }
}
