// =========================================
// File: domain/engine/MedicalEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

enum class InsurancePlan(val label: String, val monthlyCost: Double, val coveragePercent: Int) {
    NONE("Uninsured", 0.0, 0),
    BASIC("Basic Plan", 200.0, 40),
    STANDARD("Standard Plan", 500.0, 65),
    PREMIUM("Premium Plan", 1200.0, 85),
    GOLD("Gold Plan", 2500.0, 95)
}

enum class ConditionType { DISEASE, INJURY, CHRONIC, INFECTION, MENTAL }

data class MedicalCondition(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: ConditionType,
    val description: String,
    val severity: Int,              // 1-10
    val contagious: Boolean = false,
    val requiresHospitalization: Boolean = false,
    val recoveryTimeYears: Int,      // how many years to recover
    val healthDrainPerYear: Double,  // health lost per year if untreated
    val treatmentCost: Double,       // cost of full treatment
    val canKill: Boolean = false,
    val permanentEffect: String? = null,  // permanent stat effect after recovery
    val permanentEffectDelta: Double = 0.0
)

@Singleton
class MedicalEngine @Inject constructor() {

    private val diseases = listOf(
        MedicalCondition("common_cold", "Common Cold", ConditionType.INFECTION, "Mild viral infection", 2, true, false, 1, 3.0, 50.0, false),
        MedicalCondition("flu", "Influenza", ConditionType.INFECTION, "Seasonal flu with fever", 4, true, false, 1, 8.0, 150.0, false),
        MedicalCondition("pneumonia", "Pneumonia", ConditionType.INFECTION, "Severe lung infection", 7, true, true, 2, 15.0, 5000.0, true, "health", -5.0),
        MedicalCondition("strep_throat", "Strep Throat", ConditionType.INFECTION, "Bacterial throat infection", 3, true, false, 1, 5.0, 100.0, false),
        MedicalCondition("appendicitis", "Appendicitis", ConditionType.DISEASE, "Inflamed appendix needs removal", 8, false, true, 1, 25.0, 15000.0, true, "health", -8.0),
        MedicalCondition("cancer_early", "Early Stage Cancer", ConditionType.DISEASE, "Detectable but treatable malignancy", 9, false, true, 3, 20.0, 50000.0, true, "health", -15.0),
        MedicalCondition("diabetes_t2", "Type 2 Diabetes", ConditionType.CHRONIC, "Blood sugar regulation disorder", 5, false, false, 0, 5.0, 3000.0, false, "health", -3.0),
        MedicalCondition("heart_disease", "Heart Disease", ConditionType.CHRONIC, "Cardiovascular condition", 7, false, true, 0, 12.0, 25000.0, true, "athleticism", -10.0),
        MedicalCondition("asthma", "Asthma", ConditionType.CHRONIC, "Respiratory condition", 4, false, false, 0, 3.0, 2000.0, false, "athleticism", -5.0),
        MedicalCondition("arthritis", "Arthritis", ConditionType.CHRONIC, "Joint inflammation", 5, false, false, 0, 4.0, 5000.0, false, "athleticism", -8.0),
        MedicalCondition("fracture_arm", "Arm Fracture", ConditionType.INJURY, "Broken arm", 4, false, false, 1, 2.0, 3000.0, false),
        MedicalCondition("fracture_leg", "Leg Fracture", ConditionType.INJURY, "Broken leg requiring cast", 6, false, true, 2, 5.0, 5000.0, false, "athleticism", -5.0),
        MedicalCondition("concussion", "Concussion", ConditionType.INJURY, "Traumatic brain injury", 5, false, true, 1, 8.0, 4000.0, false, "smarts", -5.0),
        MedicalCondition("internal_bleeding", "Internal Bleeding", ConditionType.INJURY, "Life-threatening internal injury", 9, false, true, 2, 30.0, 20000.0, true, "health", -10.0),
        MedicalCondition("food_poisoning", "Food Poisoning", ConditionType.INFECTION, "Bacterial contamination", 3, false, false, 1, 6.0, 200.0, false),
        MedicalCondition("allergic_reaction", "Allergic Reaction", ConditionType.INFECTION, "Severe allergic response", 4, false, true, 1, 8.0, 1500.0, true),
        MedicalCondition("depression", "Depression", ConditionType.MENTAL, "Clinical depression", 6, false, false, 2, 4.0, 8000.0, true, "sanity", -10.0),
        MedicalCondition("anxiety_disorder", "Anxiety Disorder", ConditionType.MENTAL, "Chronic anxiety", 5, false, false, 2, 3.0, 6000.0, false, "sanity", -8.0),
        MedicalCondition("kidney_stones", "Kidney Stones", ConditionType.DISEASE, "Painful mineral deposits", 6, false, false, 1, 6.0, 8000.0, false),
        MedicalCondition("skin_infection", "Skin Infection", ConditionType.INFECTION, "Bacterial skin condition", 3, true, false, 1, 4.0, 300.0, false),
        MedicalCondition("migraine_chronic", "Chronic Migraines", ConditionType.CHRONIC, "Recurring severe headaches", 4, false, false, 0, 2.0, 4000.0, false, "energy", -5.0),
        MedicalCondition("hernia", "Hernia", ConditionType.INJURY, "Organ protrusion requiring surgery", 5, false, true, 1, 8.0, 12000.0, false, "athleticism", -3.0),
        MedicalCondition("hepatitis", "Hepatitis", ConditionType.INFECTION, "Liver inflammation", 6, true, true, 2, 12.0, 10000.0, true, "health", -8.0),
        MedicalCondition("stroke", "Stroke", ConditionType.DISEASE, "Cerebrovascular event", 9, false, true, 3, 35.0, 40000.0, true, "smarts", -15.0)
    )

    /** Get all possible medical conditions */
    fun getAllConditions(): List<MedicalCondition> = diseases

    /** Get a condition by ID */
    fun getCondition(id: String): MedicalCondition? = diseases.find { it.id == id }

    /** Get random condition based on character stats and environment */
    fun generateRandomCondition(character: CharacterEntity, currentYear: Int): MedicalCondition? {
        val baseChance = when {
            character.health < 20 -> 0.8
            character.health < 40 -> 0.4
            character.health < 60 -> 0.2
            else -> 0.08
        }
        if (Random.nextDouble() > baseChance) return null

        val eligible = diseases.filter { d ->
            when {
                d.requiresHospitalization && !character.isInHospital -> Random.nextFloat() < 0.3f
                d.type == ConditionType.CHRONIC -> Random.nextFloat() < 0.4f
                else -> true
            }
        }
        return eligible.randomOrNull()
    }

    /** Parse conditions string from character entity */
    fun parseConditions(data: String): List<MedicalCondition> {
        if (data.isBlank()) return emptyList()
        return data.split("|").mapNotNull { entry ->
            val parts = entry.split(",")
            if (parts.size < 2) return@mapNotNull null
            val idParts = parts[0].split(":")
            val condId = idParts[0]
            val yearsRemaining = idParts.getOrNull(1)?.toIntOrNull() ?: 1
            val baseCond = diseases.find { it.id == condId } ?: return@mapNotNull null
            baseCond.copy(id = condId, recoveryTimeYears = yearsRemaining)
        }
    }

    /** Serialize conditions to string */
    fun serializeConditions(conditions: List<MedicalCondition>): String =
        conditions.filter { it.recoveryTimeYears > 0 }
            .joinToString("|") { "${it.id}:${it.recoveryTimeYears}" }

    /** Apply a condition to character */
    fun contractCondition(character: CharacterEntity, condition: MedicalCondition, year: Int): CharacterEntity {
        val existing = parseConditions(character.medicalConditions)
        if (existing.any { it.id == condition.id }) return character // already has it
        val newList = existing + condition
        return character.copy(
            medicalConditions = serializeConditions(newList),
            isInHospital = condition.requiresHospitalization || character.isInHospital,
            health = (character.health - condition.healthDrainPerYear * 0.5).coerceIn(0.0, 100.0)
        )
    }

    /** Treat a condition — costs money based on insurance */
    fun treatCondition(character: CharacterEntity, conditionId: String, year: Int): Pair<CharacterEntity, String> {
        val conditions = parseConditions(character.medicalConditions).toMutableList()
        val idx = conditions.indexOfFirst { it.id == conditionId }
        if (idx < 0) return character to "No such condition."

        val condition = conditions[idx]
        val insurance = InsurancePlan.entries.find { it.name == character.insurancePlan } ?: InsurancePlan.NONE
        val coveredCost = condition.treatmentCost * (1.0 - insurance.coveragePercent / 100.0)
        val adminFee = 100.0 + insurance.monthlyCost * 0.1

        val totalCost = coveredCost + adminFee
        if (character.cash < totalCost) return character to "Treatment costs $${totalCost.roundToInt()}, can't afford it."

        conditions.removeAt(idx)
        var c = character.copy(
            medicalConditions = serializeConditions(conditions),
            cash = character.cash - totalCost,
            health = (character.health + condition.healthDrainPerYear * 2.0).coerceIn(0.0, 100.0)
        )

        // Apply permanent effects if any
        if (condition.permanentEffect != null && condition.permanentEffectDelta != 0.0) {
            c = when (condition.permanentEffect) {
                "health" -> c.copy(health = (c.health + condition.permanentEffectDelta).coerceIn(0.0, 100.0))
                "smarts" -> c.copy(smarts = (c.smarts + condition.permanentEffectDelta).coerceIn(0.0, 100.0))
                "athleticism" -> c.copy(athleticism = (c.athleticism + condition.permanentEffectDelta).coerceIn(0.0, 100.0))
                "sanity" -> c.copy(sanity = (c.sanity + condition.permanentEffectDelta).coerceIn(0.0, 100.0))
                "energy" -> c.copy(energy = (c.energy + condition.permanentEffectDelta).coerceIn(0.0, 100.0))
                else -> c
            }
        }

        if (conditions.none { it.requiresHospitalization }) {
            c = c.copy(isInHospital = false)
        }

        return c to "Treated ${condition.name}. Cost: $${totalCost.roundToInt()} (insurance covered ${insurance.coveragePercent}%)"
    }

    /** Age all conditions — reduce recovery time, apply health drain, death check */
    fun ageConditions(character: CharacterEntity): CharacterEntity {
        val conds = parseConditions(character.medicalConditions).toMutableList()
        if (conds.isEmpty()) return character
        var c = character
        var deathCause: String? = null
        val updatedConds = mutableListOf<MedicalCondition>()

        conds.forEach { condition ->
            // Apply health drain
            c = c.copy(health = (c.health - condition.healthDrainPerYear).coerceIn(0.0, 100.0))
            c = c.copy(stress = (c.stress + condition.severity.toDouble()).coerceIn(0.0, 100.0))
            c = c.copy(happiness = (c.happiness - condition.severity.toDouble()).coerceIn(0.0, 100.0))

            // Reduce recovery time (0 means chronic — doesn't recover)
            if (condition.recoveryTimeYears > 0) {
                val updated = condition.copy(recoveryTimeYears = condition.recoveryTimeYears - 1)
                if (updated.recoveryTimeYears > 0) {
                    updatedConds.add(updated)
                }
            } else {
                updatedConds.add(condition)
            }

            // Death check
            if (condition.canKill && c.health <= 0) {
                deathCause = "died from ${condition.name}"
            }
        }

        c = c.copy(medicalConditions = serializeConditions(updatedConds))
        if (updatedConds.none { it.requiresHospitalization }) c = c.copy(isInHospital = false)

        if (deathCause != null) c = c.copy(isAlive = false)
        return c
    }

    /** Get active conditions for display */
    fun getActiveConditions(character: CharacterEntity): List<MedicalCondition> =
        parseConditions(character.medicalConditions)

    /** Change insurance plan */
    fun setInsurance(character: CharacterEntity, plan: InsurancePlan): CharacterEntity {
        val costDiff = plan.monthlyCost - getInsurance(character).monthlyCost
        return character.copy(
            insurancePlan = plan.name,
            cash = character.cash - costDiff
        )
    }

    /** Get current insurance */
    fun getInsurance(character: CharacterEntity): InsurancePlan =
        InsurancePlan.entries.find { it.name == character.insurancePlan } ?: InsurancePlan.NONE
}
