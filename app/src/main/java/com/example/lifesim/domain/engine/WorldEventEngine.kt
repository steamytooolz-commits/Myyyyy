// =========================================
// File: domain/engine/WorldEventEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

enum class WorldEventType {
    MARKET_CRASH, RECESSION, GOLDEN_AGE, TECH_BOOM,
    WAR, PANDEMIC, NATURAL_DISASTER, PEACE_TREATY,
    POLITICAL_CRISIS, CULTURAL_RENAISSANCE,
    DISCOVERY, OIL_BOOM, TRADE_WAR, INFRASTRUCTURE_BOOM,
    DROUGHT, PLAGUE, INNOVATION_WAVE, SOCIAL_UNREST,
    ROYAL_WEDDING, SPACE_RACE, GREEN_REVOLUTION, AI_BREAKTHROUGH
}

data class WorldEvent(
    val type: WorldEventType,
    val title: String,
    val description: String,
    val duration: Int, // years remaining (1-5)
    val severity: Int, // 1-10
    val yearStarted: Int
) {
    fun tick(): WorldEvent = copy(duration = duration - 1)
    fun isExpired(): Boolean = duration <= 0
}

data class WorldEventEffects(
    val marketMultiplier: Double = 1.0,      // applied to InvestmentEngine returns
    val realEstateMultiplier: Double = 1.0,  // applied to RealEstateEngine trends
    val statModifiers: Map<String, Double> = emptyMap(),  // applied to character each year
    val cashDelta: Double = 0.0,              // applied to character cash each year
    val reputationDelta: Int = 0,             // applied to character reputation each year
    val activityBoosts: Map<String, Double> = emptyMap(),  // activity type -> effect multiplier
    val isDeadly: Boolean = false,            // may cause death
    val deathChanceBonus: Double = 0.0        // added to death probability
)

@Singleton
class WorldEventEngine @Inject constructor() {

    private val activeEvents = mutableListOf<WorldEvent>()
    private var eventHistory = mutableListOf<String>() // descriptions of past events

    fun getActiveEvents(): List<WorldEvent> = activeEvents.toList()

    fun getEventHistory(): List<String> = eventHistory.toList()

    fun getCurrentMajorEvent(): WorldEvent? = activeEvents.maxByOrNull { it.severity }

    fun hasActiveEvents(): Boolean = activeEvents.isNotEmpty()

    fun generateAnnualEvent(currentYear: Int): WorldEvent? {
        tickActiveEvents()
        // 55% chance of a new event each year
        if (Random.nextFloat() > 0.55f) return null
        // Don't generate if more than 2 events are already active
        if (activeEvents.size >= 3) return null

        val template = generateRandomEvent()
        val event = WorldEvent(
            type = template.type,
            title = template.title,
            description = template.description,
            duration = template.durationRange.random(),
            severity = template.severityRange.random(),
            yearStarted = currentYear
        )
        activeEvents.add(event)
        eventHistory.add("$currentYear: ${event.title} — ${event.description}")
        return event
    }

    fun getCombinedEffects(): WorldEventEffects {
        var marketMult = 1.0
        var realEstateMult = 1.0
        val statMods = mutableMapOf<String, Double>()
        var cashDelta = 0.0
        var repDelta = 0
        val activityBoosts = mutableMapOf<String, Double>()
        var isDeadly = false
        var deathChance = 0.0

        activeEvents.forEach { event ->
            val effects = getEventEffects(event)
            marketMult *= effects.marketMultiplier
            realEstateMult *= effects.realEstateMultiplier
            effects.statModifiers.forEach { (k, v) -> statMods[k] = (statMods[k] ?: 0.0) + v }
            cashDelta += effects.cashDelta
            repDelta += effects.reputationDelta
            effects.activityBoosts.forEach { (k, v) -> activityBoosts[k] = (activityBoosts[k] ?: 0.0) + v }
            if (effects.isDeadly) isDeadly = true
            deathChance += effects.deathChanceBonus
        }

        return WorldEventEffects(
            marketMultiplier = marketMult,
            realEstateMultiplier = realEstateMult,
            statModifiers = statMods,
            cashDelta = cashDelta,
            reputationDelta = repDelta,
            activityBoosts = activityBoosts,
            isDeadly = isDeadly,
            deathChanceBonus = deathChance
        )
    }

    fun tickActiveEvents() {
        val surviving = mutableListOf<WorldEvent>()
        activeEvents.forEach { event ->
            val ticked = event.tick()
            if (ticked.isExpired()) {
                eventHistory.add("Event ended: ${ticked.title}")
            } else {
                surviving.add(ticked)
            }
        }
        activeEvents.clear()
        activeEvents.addAll(surviving)
    }

    fun clearEvents() {
        activeEvents.clear()
    }

    private fun getEventEffects(event: WorldEvent): WorldEventEffects {
        val s = event.severity / 10.0 // normalize severity to 0.1-1.0
        return when (event.type) {
            WorldEventType.MARKET_CRASH -> WorldEventEffects(
                marketMultiplier = (0.3 + (1.0 - s)).coerceAtMost(1.0),
                realEstateMultiplier = (0.5 + (1.0 - s)).coerceAtMost(1.0),
                statModifiers = mapOf("stress" to 5.0 * s, "happiness" to -5.0 * s),
                cashDelta = -2000.0 * s,
                activityBoosts = mapOf("day_trade" to 0.5, "research_stocks" to 1.5)
            )
            WorldEventType.RECESSION -> WorldEventEffects(
                marketMultiplier = (0.5 + (1.0 - s)).coerceAtMost(1.0),
                cashDelta = -1000.0 * s,
                statModifiers = mapOf("stress" to 8.0 * s, "happiness" to -3.0 * s),
                reputationDelta = -2
            )
            WorldEventType.GOLDEN_AGE -> WorldEventEffects(
                marketMultiplier = 1.0 + 0.3 * s,
                realEstateMultiplier = 1.0 + 0.2 * s,
                statModifiers = mapOf("happiness" to 8.0 * s, "stress" to -5.0 * s, "energy" to 5.0 * s),
                cashDelta = 3000.0 * s,
                reputationDelta = 5,
                activityBoosts = mapOf("work" to 1.3, "socialize" to 1.5)
            )
            WorldEventType.TECH_BOOM -> WorldEventEffects(
                marketMultiplier = 1.0 + 0.4 * s,
                statModifiers = mapOf("smarts" to 5.0 * s, "creativity" to 3.0 * s),
                cashDelta = 2000.0 * s,
                activityBoosts = mapOf("research" to 2.0, "research_stocks" to 1.8, "attend_class" to 1.5)
            )
            WorldEventType.WAR -> WorldEventEffects(
                marketMultiplier = (0.6 + (1.0 - s)).coerceAtMost(1.0),
                statModifiers = mapOf("stress" to 10.0 * s, "aggression" to 8.0 * s, "paranoia" to 5.0 * s, "happiness" to -8.0 * s),
                cashDelta = -3000.0 * s,
                reputationDelta = -3,
                isDeadly = true,
                deathChanceBonus = 0.01 * s,
                activityBoosts = mapOf("combat_training" to 1.5, "weapons_training" to 1.3)
            )
            WorldEventType.PANDEMIC -> WorldEventEffects(
                marketMultiplier = (0.6 + (1.0 - s)).coerceAtMost(1.0),
                statModifiers = mapOf("health" to -8.0 * s, "stress" to 10.0 * s, "happiness" to -8.0 * s, "hygiene" to 5.0 * s),
                cashDelta = -1000.0 * s,
                isDeadly = true,
                deathChanceBonus = 0.008 * s
            )
            WorldEventType.NATURAL_DISASTER -> WorldEventEffects(
                realEstateMultiplier = (0.5 + (1.0 - s)).coerceAtMost(1.0),
                statModifiers = mapOf("health" to -5.0 * s, "stress" to 12.0 * s, "happiness" to -6.0 * s),
                cashDelta = -5000.0 * s,
                isDeadly = true,
                deathChanceBonus = 0.005 * s
            )
            WorldEventType.PEACE_TREATY -> WorldEventEffects(
                marketMultiplier = 1.0 + 0.1 * s,
                statModifiers = mapOf("happiness" to 8.0 * s, "stress" to -8.0 * s, "paranoia" to -3.0 * s),
                reputationDelta = 8,
                activityBoosts = mapOf("socialize" to 1.5, "travel" to 1.8, "meditate" to 1.3)
            )
            WorldEventType.POLITICAL_CRISIS -> WorldEventEffects(
                statModifiers = mapOf("stress" to 8.0 * s, "paranoia" to 5.0 * s),
                reputationDelta = -5,
                activityBoosts = mapOf("public_speech" to 1.5, "campaign_rally" to 1.4, "political_debate" to 1.8)
            )
            WorldEventType.CULTURAL_RENAISSANCE -> WorldEventEffects(
                statModifiers = mapOf("creativity" to 8.0 * s, "happiness" to 5.0 * s, "charisma" to 3.0 * s),
                cashDelta = 1000.0 * s,
                reputationDelta = 3,
                activityBoosts = mapOf("socialize" to 1.3, "party" to 1.5, "travel" to 1.4)
            )
            WorldEventType.DISCOVERY -> WorldEventEffects(
                statModifiers = mapOf("smarts" to 8.0 * s, "creativity" to 5.0 * s),
                activityBoosts = mapOf("research" to 2.0, "study" to 1.5, "attend_class" to 1.3)
            )
            WorldEventType.OIL_BOOM -> WorldEventEffects(
                cashDelta = 5000.0 * s,
                marketMultiplier = 1.0 + 0.2 * s,
                statModifiers = mapOf("cash" to 5000.0 * s, "happiness" to 3.0 * s)
            )
            WorldEventType.TRADE_WAR -> WorldEventEffects(
                marketMultiplier = (0.7 + (1.0 - s)).coerceAtMost(1.0),
                cashDelta = -2000.0 * s,
                statModifiers = mapOf("stress" to 5.0 * s),
                reputationDelta = -2,
                activityBoosts = mapOf("work" to 1.2)
            )
            WorldEventType.INFRASTRUCTURE_BOOM -> WorldEventEffects(
                realEstateMultiplier = 1.0 + 0.3 * s,
                cashDelta = 2000.0 * s,
                statModifiers = mapOf("stress" to -3.0 * s, "happiness" to 3.0 * s),
                activityBoosts = mapOf("buy_property" to 1.5, "renovate_home" to 1.8)
            )
            WorldEventType.DROUGHT -> WorldEventEffects(
                statModifiers = mapOf("health" to -3.0 * s, "stress" to 5.0 * s),
                cashDelta = -1500.0 * s,
                realEstateMultiplier = (0.7 + (1.0 - s)).coerceAtMost(1.0)
            )
            WorldEventType.PLAGUE -> WorldEventEffects(
                statModifiers = mapOf("health" to -10.0 * s, "happiness" to -6.0 * s),
                cashDelta = -2000.0 * s,
                isDeadly = true,
                deathChanceBonus = 0.012 * s
            )
            WorldEventType.INNOVATION_WAVE -> WorldEventEffects(
                statModifiers = mapOf("smarts" to 10.0 * s, "creativity" to 8.0 * s),
                cashDelta = 3000.0 * s,
                activityBoosts = mapOf("research" to 2.5, "study" to 1.8, "work" to 1.3)
            )
            WorldEventType.SOCIAL_UNREST -> WorldEventEffects(
                statModifiers = mapOf("stress" to 8.0 * s, "paranoia" to 5.0 * s, "happiness" to -5.0 * s),
                reputationDelta = -3,
                activityBoosts = mapOf("public_speech" to 1.6, "political_debate" to 1.4)
            )
            WorldEventType.ROYAL_WEDDING -> WorldEventEffects(
                statModifiers = mapOf("happiness" to 5.0 * s, "charisma" to 2.0 * s),
                cashDelta = 1000.0 * s,
                reputationDelta = 3,
                activityBoosts = mapOf("party" to 1.6, "socialize" to 1.4)
            )
            WorldEventType.SPACE_RACE -> WorldEventEffects(
                statModifiers = mapOf("smarts" to 8.0 * s, "creativity" to 5.0 * s, "discipline" to 3.0 * s),
                cashDelta = 2000.0 * s,
                marketMultiplier = 1.0 + 0.15 * s,
                activityBoosts = mapOf("research" to 1.8, "study" to 1.5)
            )
            WorldEventType.GREEN_REVOLUTION -> WorldEventEffects(
                statModifiers = mapOf("happiness" to 5.0 * s, "health" to 3.0 * s, "creativity" to 3.0 * s),
                cashDelta = 1000.0 * s,
                reputationDelta = 5,
                realEstateMultiplier = 1.0 + 0.1 * s,
                activityBoosts = mapOf("travel" to 1.3, "renovate_home" to 1.4)
            )
            WorldEventType.AI_BREAKTHROUGH -> WorldEventEffects(
                statModifiers = mapOf("smarts" to 6.0 * s, "stress" to 4.0 * s, "happiness" to -2.0 * s),
                cashDelta = 4000.0 * s,
                marketMultiplier = 1.0 + 0.3 * s,
                activityBoosts = mapOf("research" to 2.2, "study" to 1.8, "work" to 1.5)
            )
        }
    }

    private fun generateRandomEvent(): EventTemplate {
        val templates = listOf(
            EventTemplate(WorldEventType.MARKET_CRASH, "Global Market Crash", "Stock markets plummet worldwide. Panic selling grips investors.", 2..4, 6..9),
            EventTemplate(WorldEventType.RECESSION, "Economic Recession", "The economy contracts sharply. Businesses close, jobs vanish.", 3..5, 5..8),
            EventTemplate(WorldEventType.GOLDEN_AGE, "Golden Age Dawns", "A period of unprecedented prosperity and cultural flourishing.", 3..5, 7..10),
            EventTemplate(WorldEventType.TECH_BOOM, "Technology Boom", "Innovation accelerates. Tech stocks soar to new heights.", 2..4, 6..9),
            EventTemplate(WorldEventType.WAR, "War Breaks Out", "Geopolitical tensions erupt into open conflict.", 2..5, 7..10),
            EventTemplate(WorldEventType.PANDEMIC, "Global Pandemic", "A deadly virus spreads across the world.", 2..4, 7..10),
            EventTemplate(WorldEventType.NATURAL_DISASTER, "Great Natural Disaster", "A catastrophic earthquake and tsunami devastate major cities.", 1..3, 8..10),
            EventTemplate(WorldEventType.PEACE_TREATY, "Historic Peace Treaty", "Warring nations sign a groundbreaking peace agreement.", 3..5, 6..9),
            EventTemplate(WorldEventType.POLITICAL_CRISIS, "Political Crisis", "Government instability threatens the nation's future.", 2..4, 5..8),
            EventTemplate(WorldEventType.CULTURAL_RENAISSANCE, "Cultural Renaissance", "Art, music, and literature explode with new creative energy.", 3..5, 5..8),
            EventTemplate(WorldEventType.DISCOVERY, "Great Discovery", "A revolutionary scientific discovery changes everything.", 3..5, 6..9),
            EventTemplate(WorldEventType.OIL_BOOM, "Oil Boom", "Massive oil deposits discovered. Energy sector explodes.", 2..4, 5..8),
            EventTemplate(WorldEventType.TRADE_WAR, "Trade War Escalates", "Countries impose heavy tariffs on each other's goods.", 2..4, 4..7),
            EventTemplate(WorldEventType.INFRASTRUCTURE_BOOM, "Infrastructure Renaissance", "Massive government investment in roads, bridges, and broadband.", 3..5, 5..7),
            EventTemplate(WorldEventType.DROUGHT, "Severe Drought", "Unprecedented drought devastates agriculture and water supplies.", 2..4, 5..8),
            EventTemplate(WorldEventType.PLAGUE, "Bubonic Plague Resurgence", "A medieval plague returns with modern consequences.", 2..3, 8..10),
            EventTemplate(WorldEventType.INNOVATION_WAVE, "Wave of Innovation", "Startups and inventors usher in a new era of progress.", 3..5, 6..9),
            EventTemplate(WorldEventType.SOCIAL_UNREST, "Social Unrest", "Mass protests and civil disobedience sweep the nation.", 2..4, 5..8),
            EventTemplate(WorldEventType.ROYAL_WEDDING, "Royal Wedding", "A grand royal wedding captures the world's imagination.", 1..2, 3..5),
            EventTemplate(WorldEventType.SPACE_RACE, "New Space Race", "Nations compete to reach Mars and beyond.", 3..5, 6..8),
            EventTemplate(WorldEventType.GREEN_REVOLUTION, "Green Revolution", "Renewable energy transforms the global economy.", 3..5, 5..8),
            EventTemplate(WorldEventType.AI_BREAKTHROUGH, "AI Breakthrough", "Artificial intelligence reaches new capabilities, disrupting industries.", 3..5, 7..10)
        )
        val template = templates.random()
        val dur = template.durationRange.random()
        val sev = template.severityRange.random()
        return EventTemplate(template.type, template.title, template.description, dur..dur, sev..sev)
    }

    private data class EventTemplate(
        val type: WorldEventType,
        val title: String,
        val description: String,
        val durationRange: IntRange,
        val severityRange: IntRange
    )
}
