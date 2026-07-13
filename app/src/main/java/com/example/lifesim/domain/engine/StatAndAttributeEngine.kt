package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import javax.inject.Inject
import javax.inject.Singleton

data class Moodlet(val name: String, val stat: String, val modifier: Double, val durationTicks: Int) {
    fun tick(): Moodlet = copy(durationTicks = durationTicks - 1)
    fun isExpired(): Boolean = durationTicks <= 0
}

@Singleton
class StatAndAttributeEngine @Inject constructor() {

    fun applyNeedsPenalties(character: CharacterEntity): CharacterEntity {
        var c = character
        if (c.hunger < 20.0) c = c.copy(health = (c.health - 5.0).coerceIn(0.0, 100.0), happiness = (c.happiness - 3.0).coerceIn(0.0, 100.0))
        if (c.energy < 20.0) c = c.copy(smarts = (c.smarts - 8.0).coerceIn(0.0, 100.0), athleticism = (c.athleticism - 10.0).coerceIn(0.0, 100.0))
        if (c.hygiene < 15.0) c = c.copy(happiness = (c.happiness - 4.0).coerceIn(0.0, 100.0), reputation = (c.reputation - 3).coerceIn(0, 100))
        if (c.sanity < 30.0) c = c.copy(paranoia = (c.paranoia + 5.0).coerceIn(0.0, 100.0), stress = (c.stress + 5.0).coerceIn(0.0, 100.0))
        if (c.isAddicted && c.addictionSeverity > 3 && !c.isInWithdrawal) c = c.copy(health = (c.health - 2.0 * c.addictionSeverity).coerceIn(0.0, 100.0), happiness = (c.happiness - 5.0 * c.addictionSeverity).coerceIn(0.0, 100.0), cash = c.cash - 100.0 * c.addictionSeverity)
        if (c.isAddicted && c.isInWithdrawal) c = c.copy(health = (c.health - 4.0 * c.addictionSeverity).coerceIn(0.0, 100.0), happiness = (c.happiness - 10.0 * c.addictionSeverity).coerceIn(0.0, 100.0), stress = (c.stress + 5.0 * c.addictionSeverity).coerceIn(0.0, 100.0), cash = c.cash - 50.0 * c.addictionSeverity)
        return c
    }

    fun applyMoodlets(character: CharacterEntity, activeMoodlets: List<Moodlet>): Pair<CharacterEntity, List<Moodlet>> {
        var c = character; val remaining = mutableListOf<Moodlet>()
        activeMoodlets.forEach { if (!it.isExpired()) { c = when (it.stat.lowercase()) { "health" -> c.copy(health = (c.health + it.modifier).coerceIn(0.0, 100.0)); "happiness" -> c.copy(happiness = (c.happiness + it.modifier).coerceIn(0.0, 100.0)); "smarts" -> c.copy(smarts = (c.smarts + it.modifier).coerceIn(0.0, 100.0)); "stress" -> c.copy(stress = (c.stress + it.modifier).coerceIn(0.0, 100.0)); "energy" -> c.copy(energy = (c.energy + it.modifier).coerceIn(0.0, 100.0)); else -> c }; remaining.add(it.tick()) } }
        return c to remaining
    }

    fun calculateStatChange(character: CharacterEntity, activityType: String, duration: Int = 1): Map<String, Double> {
        return when (activityType.lowercase()) {
            "gym" -> mapOf("athleticism" to 5.0*duration, "energy" to -8.0*duration, "looks" to 1.0*duration)
            "study" -> mapOf("smarts" to 6.0*duration, "energy" to -5.0*duration, "stress" to 3.0*duration)
            "meditate" -> mapOf("stress" to -8.0*duration, "happiness" to 3.0*duration, "sanity" to 3.0*duration, "energy" to -2.0*duration)
            "socialize" -> mapOf("happiness" to 5.0*duration, "charisma" to 2.0*duration, "energy" to -4.0*duration)
            "work" -> mapOf("smarts" to 2.0*duration, "stress" to 4.0*duration, "cash" to 5000.0*duration, "energy" to -15.0*duration)
            "crime" -> mapOf("aggression" to 3.0*duration, "notoriety" to 5.0*duration, "karma" to -5.0*duration, "energy" to -10.0*duration)
            "party" -> mapOf("happiness" to 8.0*duration, "energy" to -12.0*duration, "stress" to -3.0*duration, "hygiene" to -2.0*duration)
            "travel" -> mapOf("happiness" to 10.0*duration, "smarts" to 3.0*duration, "cash" to -2000.0*duration, "stress" to -5.0*duration, "energy" to -15.0*duration)
            "eat" -> mapOf("hunger" to 25.0*duration, "happiness" to 3.0*duration, "cash" to -30.0*duration)
            "sleep" -> mapOf("stress" to -5.0*duration, "health" to 1.0*duration)
            "shower" -> mapOf("hygiene" to 40.0, "happiness" to 2.0)
            "therapy" -> mapOf("sanity" to 10.0*duration, "stress" to -8.0*duration, "cash" to -150.0*duration, "energy" to -5.0*duration)
            "rehab" -> mapOf("addictionSeverity" to -3.0*duration, "health" to 5.0*duration, "cash" to -5000.0*duration, "energy" to -10.0*duration)
            "drink" -> mapOf("stress" to -5.0*duration, "happiness" to 4.0*duration, "health" to -2.0*duration, "cash" to -40.0*duration, "addiction_substance" to 1.0)
            "gamble" -> mapOf("stress" to -3.0*duration, "cash" to -200.0*duration, "smarts" to 2.0*duration, "addiction_gambling" to 1.0, "energy" to -5.0*duration)
            "smoke" -> mapOf("stress" to -3.0*duration, "happiness" to 2.0*duration, "health" to -3.0*duration, "cash" to -20.0*duration, "addiction_smoking" to 1.0)
            // MILITARY activities
            "boot_camp" -> mapOf("athleticism" to 15.0, "discipline" to 15.0, "health" to 5.0, "stress" to 10.0, "energy" to -10.0)
            "combat_training" -> mapOf("athleticism" to 5.0, "aggression" to 8.0, "discipline" to 5.0, "stress" to 8.0)
            "military_exercise" -> mapOf("athleticism" to 8.0, "discipline" to 5.0, "energy" to -8.0, "stress" to 5.0)
            "guard_duty" -> mapOf("discipline" to 3.0, "energy" to -3.0, "stress" to 2.0)
            "weapons_training" -> mapOf("smarts" to 3.0, "athleticism" to 5.0, "aggression" to 3.0)
            // EDUCATION activities
            "attend_class" -> mapOf("smarts" to 8.0*duration, "discipline" to 3.0*duration, "stress" to 5.0*duration, "energy" to -4.0*duration)
            "do_homework" -> mapOf("smarts" to 5.0*duration, "discipline" to 4.0*duration, "stress" to 3.0*duration, "energy" to -3.0*duration)
            "research" -> mapOf("smarts" to 10.0*duration, "creativity" to 5.0*duration, "stress" to 4.0*duration, "energy" to -5.0*duration)
            "join_study_group" -> mapOf("smarts" to 6.0*duration, "charisma" to 3.0*duration, "happiness" to 2.0*duration)
            "take_exam" -> mapOf("smarts" to 12.0*duration, "stress" to 15.0*duration, "energy" to -8.0*duration)
            // POLITICAL activities
            "campaign_rally" -> mapOf("charisma" to 8.0*duration, "stress" to 10.0*duration, "energy" to -8.0*duration, "cash" to -2000.0*duration)
            "fundraiser" -> mapOf("charisma" to 5.0*duration, "reputation" to 8.0*duration, "cash" to 5000.0*duration, "stress" to 5.0*duration)
            "public_speech" -> mapOf("charisma" to 10.0*duration, "reputation" to 6.0*duration, "stress" to 12.0*duration)
            "meet_constituents" -> mapOf("charisma" to 4.0*duration, "empathy" to 5.0*duration, "reputation" to 5.0*duration, "energy" to -5.0*duration)
            "political_debate" -> mapOf("smarts" to 8.0*duration, "charisma" to 8.0*duration, "stress" to 12.0*duration, "reputation" to 10.0*duration)
            // REAL ESTATE activities
            "buy_property" -> mapOf("cash" to -100000.0*duration, "stress" to 8.0*duration)
            "sell_property" -> mapOf("cash" to 120000.0*duration, "happiness" to 5.0*duration)
            "renovate_home" -> mapOf("cash" to -5000.0*duration, "happiness" to 5.0*duration, "stress" to 5.0*duration)
            "hire_contractor" -> mapOf("cash" to -2000.0*duration, "stress" to -5.0*duration)
            "property_inspection" -> mapOf("smarts" to 3.0*duration, "energy" to -3.0*duration)
            // INVESTMENT activities
            "day_trade" -> mapOf("smarts" to 5.0, "stress" to 10.0, "cash" to if (kotlin.random.Random.nextFloat() < 0.4f) 2000.0 else -1500.0)
            "research_stocks" -> mapOf("smarts" to 8.0, "stress" to 2.0, "energy" to -3.0)
            "check_portfolio" -> mapOf("stress" to if (kotlin.random.Random.nextFloat() < 0.5f) -3.0 else 5.0)
            "diversify_investments" -> mapOf("smarts" to 5.0, "discipline" to 3.0)
            else -> emptyMap()
        }
    }

    fun applyWorldEventEffects(character: CharacterEntity, effects: WorldEventEffects): CharacterEntity {
        var c = character
        effects.statModifiers.forEach { (stat, delta) ->
            c = when (stat.lowercase()) {
                "health" -> c.copy(health = (c.health + delta).coerceIn(0.0, 100.0))
                "happiness" -> c.copy(happiness = (c.happiness + delta).coerceIn(0.0, 100.0))
                "smarts" -> c.copy(smarts = (c.smarts + delta).coerceIn(0.0, 100.0))
                "stress" -> c.copy(stress = (c.stress + delta).coerceIn(0.0, 100.0))
                "energy" -> c.copy(energy = (c.energy + delta).coerceIn(0.0, 100.0))
                "creativity" -> c.copy(creativity = (c.creativity + delta).coerceIn(0.0, 100.0))
                "charisma" -> c.copy(charisma = (c.charisma + delta).coerceIn(0.0, 100.0))
                "aggression" -> c.copy(aggression = (c.aggression + delta).coerceIn(0.0, 100.0))
                "paranoia" -> c.copy(paranoia = (c.paranoia + delta).coerceIn(0.0, 100.0))
                "discipline" -> c.copy(discipline = (c.discipline + delta).coerceIn(0.0, 100.0))
                "hygiene" -> c.copy(hygiene = (c.hygiene + delta).coerceIn(0.0, 100.0))
                "sanity" -> c.copy(sanity = (c.sanity + delta).coerceIn(0.0, 100.0))
                else -> c
            }
        }
        return c
    }

    fun createMoodletForEvent(eventType: String): Moodlet? = when (eventType.lowercase()) {
        "romance" -> Moodlet("In Love", "happiness", 8.0, 5)
        "betrayal" -> Moodlet("Betrayed", "happiness", -10.0, 8)
        "triumph" -> Moodlet("Empowered", "happiness", 12.0, 6)
        "trauma" -> Moodlet("Traumatized", "stress", 10.0, 12)
        "crime_success" -> Moodlet("Feeling Invincible", "aggression", 5.0, 4)
        "loss" -> Moodlet("Grieving", "happiness", -15.0, 10)
        "promotion" -> Moodlet("Career High", "happiness", 10.0, 5)
        "fired" -> Moodlet("Career Crisis", "stress", 12.0, 8)
        "discovery" -> Moodlet("Enlightened", "smarts", 8.0, 4)
        "grateful" -> Moodlet("Grateful", "happiness", 5.0, 3)
        else -> null
    }
}
