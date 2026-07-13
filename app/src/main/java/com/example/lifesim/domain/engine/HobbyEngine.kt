// =========================================
// File: domain/engine/HobbyEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

enum class HobbyCategory { CREATIVE, PHYSICAL, INTELLECTUAL, SOCIAL, DOMESTIC, OUTDOOR }

data class HobbyDef(
    val id: String,
    val name: String,
    val category: HobbyCategory,
    val description: String,
    val statEffects: Map<String, Double>,  // base stat changes per session
    val incomePerLevel: Double,             // $ per mastery level when monetized
    val costPerSession: Double,             // money cost per session
    val energyCost: Double,                 // energy consumed per session
    val iconPrefix: String                  // for UI icon selection
)

data class HobbyProgress(
    val level: Int = 0,    // 1-10 mastery
    val xp: Double = 0.0   // progress to next level
)

@Singleton
class HobbyEngine @Inject constructor() {

    val hobbies: List<HobbyDef> = listOf(
        // CREATIVE
        HobbyDef("painting", "Painting", HobbyCategory.CREATIVE, "Express yourself through art", mapOf("creativity" to 6.0, "happiness" to 4.0, "stress" to -5.0), 50.0, 30.0, 6.0, "palette"),
        HobbyDef("writing", "Writing", HobbyCategory.CREATIVE, "Craft stories, poems, or journals", mapOf("creativity" to 8.0, "smarts" to 4.0, "stress" to -3.0), 40.0, 5.0, 5.0, "edit"),
        HobbyDef("music", "Music", HobbyCategory.CREATIVE, "Play an instrument or sing", mapOf("creativity" to 6.0, "charisma" to 3.0, "happiness" to 5.0), 60.0, 50.0, 4.0, "music_note"),
        HobbyDef("photography", "Photography", HobbyCategory.CREATIVE, "Capture moments through a lens", mapOf("creativity" to 5.0, "smarts" to 2.0, "happiness" to 4.0), 35.0, 40.0, 4.0, "camera_alt"),
        HobbyDef("pottery", "Pottery", HobbyCategory.CREATIVE, "Shape clay into beautiful objects", mapOf("creativity" to 5.0, "stress" to -6.0, "discipline" to 3.0), 45.0, 25.0, 7.0, "handyman"),
        // PHYSICAL
        HobbyDef("running", "Running", HobbyCategory.PHYSICAL, "Hit the pavement and build endurance", mapOf("athleticism" to 8.0, "health" to 5.0, "stress" to -4.0, "energy" to -8.0), 10.0, 0.0, 8.0, "directions_run"),
        HobbyDef("yoga", "Yoga", HobbyCategory.PHYSICAL, "Find balance of body and mind", mapOf("athleticism" to 4.0, "stress" to -8.0, "health" to 3.0, "sanity" to 4.0), 25.0, 15.0, 4.0, "self_improvement"),
        HobbyDef("dance", "Dance", HobbyCategory.PHYSICAL, "Move to the rhythm", mapOf("athleticism" to 6.0, "charisma" to 3.0, "happiness" to 6.0, "energy" to -6.0), 30.0, 10.0, 6.0, "music_note"),
        HobbyDef("martial_arts", "Martial Arts", HobbyCategory.PHYSICAL, "Discipline of body and spirit", mapOf("athleticism" to 10.0, "discipline" to 8.0, "aggression" to 3.0, "health" to 4.0), 20.0, 40.0, 8.0, "sports_martial_arts"),
        HobbyDef("swimming", "Swimming", HobbyCategory.PHYSICAL, "Lap after lap in the water", mapOf("athleticism" to 7.0, "health" to 6.0, "stress" to -4.0, "looks" to 2.0), 15.0, 20.0, 7.0, "pool"),
        HobbyDef("cycling", "Cycling", HobbyCategory.PHYSICAL, "Explore on two wheels", mapOf("athleticism" to 7.0, "health" to 4.0, "energy" to -6.0, "happiness" to 3.0), 12.0, 5.0, 6.0, "pedal_bike"),
        // INTELLECTUAL
        HobbyDef("chess", "Chess", HobbyCategory.INTELLECTUAL, "Master the ancient game of strategy", mapOf("smarts" to 8.0, "discipline" to 4.0, "stress" to 2.0), 20.0, 10.0, 3.0, "castle"),
        HobbyDef("programming", "Programming", HobbyCategory.INTELLECTUAL, "Build software and solve problems", mapOf("smarts" to 10.0, "creativity" to 4.0, "stress" to 5.0, "energy" to -4.0), 100.0, 0.0, 5.0, "code"),
        HobbyDef("reading", "Reading", HobbyCategory.INTELLECTUAL, "Lose yourself in books", mapOf("smarts" to 6.0, "creativity" to 3.0, "stress" to -5.0, "empathy" to 2.0), 15.0, 5.0, 2.0, "menu_book"),
        HobbyDef("languages", "Languages", HobbyCategory.INTELLECTUAL, "Learn new ways to communicate", mapOf("smarts" to 8.0, "charisma" to 3.0, "discipline" to 5.0), 25.0, 20.0, 4.0, "translate"),
        HobbyDef("puzzles", "Puzzles", HobbyCategory.INTELLECTUAL, "Solve crosswords, sudoku, and more", mapOf("smarts" to 5.0, "discipline" to 3.0, "stress" to -3.0), 10.0, 5.0, 2.0, "extension"),
        // SOCIAL
        HobbyDef("board_games", "Board Games", HobbyCategory.SOCIAL, "Game night with friends", mapOf("charisma" to 4.0, "happiness" to 6.0, "smarts" to 3.0, "stress" to -4.0), 15.0, 10.0, 3.0, "games"),
        HobbyDef("volunteering", "Volunteering", HobbyCategory.SOCIAL, "Give back to your community", mapOf("empathy" to 8.0, "happiness" to 5.0, "reputation" to 8.0, "karma" to 10.0), 0.0, 0.0, 5.0, "volunteer_activism"),
        HobbyDef("cooking", "Cooking", HobbyCategory.DOMESTIC, "Create culinary masterpieces", mapOf("creativity" to 4.0, "discipline" to 3.0, "happiness" to 5.0, "hunger" to 15.0), 40.0, 25.0, 5.0, "restaurant"),
        HobbyDef("baking", "Baking", HobbyCategory.DOMESTIC, "Sweet treats and breads", mapOf("creativity" to 4.0, "stress" to -5.0, "happiness" to 5.0, "hunger" to 10.0), 35.0, 20.0, 5.0, "bakery_dining"),
        HobbyDef("gardening", "Gardening", HobbyCategory.DOMESTIC, "Grow plants and flowers", mapOf("stress" to -7.0, "happiness" to 4.0, "health" to 2.0, "athleticism" to 2.0), 30.0, 15.0, 5.0, "yard"),
        HobbyDef("woodworking", "Woodworking", HobbyCategory.DOMESTIC, "Craft furniture and decor", mapOf("creativity" to 6.0, "discipline" to 5.0, "athleticism" to 3.0, "stress" to -3.0), 80.0, 50.0, 7.0, "handyman"),
        // OUTDOOR
        HobbyDef("hiking", "Hiking", HobbyCategory.OUTDOOR, "Explore nature on foot", mapOf("athleticism" to 5.0, "health" to 4.0, "happiness" to 6.0, "stress" to -6.0), 10.0, 0.0, 6.0, "hiking"),
        HobbyDef("fishing", "Fishing", HobbyCategory.OUTDOOR, "Cast a line and relax", mapOf("stress" to -8.0, "discipline" to 3.0, "happiness" to 3.0, "hunger" to 5.0), 20.0, 10.0, 4.0, "set_meal"),
        HobbyDef("camping", "Camping", HobbyCategory.OUTDOOR, "Sleep under the stars", mapOf("athleticism" to 3.0, "stress" to -7.0, "happiness" to 5.0, "survival" to 5.0), 15.0, 20.0, 5.0, "night_shelter"),
        HobbyDef("astronomy", "Astronomy", HobbyCategory.OUTDOOR, "Study the night sky", mapOf("smarts" to 6.0, "creativity" to 3.0, "stress" to -5.0, "happiness" to 4.0), 25.0, 30.0, 3.0, "telescope")
    )

    private val xpPerLevel = 100.0

    /** Parse hobby data string into a mutable map of hobbyId -> HobbyProgress */
    fun parseHobbyData(data: String): MutableMap<String, HobbyProgress> {
        val map = mutableMapOf<String, HobbyProgress>()
        if (data.isBlank()) return map
        data.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 3) {
                val id = parts[0]
                val level = parts[1].toIntOrNull() ?: 0
                val xp = parts[2].toDoubleOrNull() ?: 0.0
                map[id] = HobbyProgress(level, xp)
            }
        }
        return map
    }

    /** Serialize hobby progress map to string for storage */
    fun serializeHobbyData(progress: Map<String, HobbyProgress>): String =
        progress.entries.joinToString(",") { "${it.key}:${it.value.level}:${String.format("%.1f", it.value.xp)}" }

    /** Start a new hobby. Returns null if already started. */
    fun startHobby(character: CharacterEntity, hobbyId: String): CharacterEntity? {
        if (character.hobbies.contains(hobbyId)) return null
        val hobby = hobbies.find { it.id == hobbyId } ?: return null
        val newHobbies = character.hobbies + hobbyId
        val progress = parseHobbyData(character.hobbyData)
        progress[hobbyId] = HobbyProgress(level = 1, xp = 0.0)
        return character.copy(
            hobbies = newHobbies,
            hobbyData = serializeHobbyData(progress),
            cash = (character.cash - hobby.costPerSession).coerceAtLeast(0.0),
            energy = (character.energy - hobby.energyCost * 0.5).coerceIn(0.0, 100.0)
        )
    }

    /** Practice a hobby — gain XP, apply stat effects */
    fun practiceHobby(character: CharacterEntity, hobbyId: String): Pair<CharacterEntity, String> {
        if (!character.hobbies.contains(hobbyId)) return character to "You haven't started this hobby yet."
        val hobby = hobbies.find { it.id == hobbyId } ?: return character to "Unknown hobby."
        val progress = parseHobbyData(character.hobbyData)
        val current = progress[hobbyId] ?: HobbyProgress(level = 1, xp = 0.0)

        if (current.level >= 10) return character to "You've mastered this hobby! No more to learn."

        var c = character
        val xpGain = 5.0 + Random.nextDouble() * 10.0 + (character.smarts * 0.05)
        var newXp = current.xp + xpGain
        var newLevel = current.level
        var leveledUp = false

        while (newXp >= xpPerLevel && newLevel < 10) {
            newXp -= xpPerLevel
            newLevel++
            leveledUp = true
        }

        progress[hobbyId] = HobbyProgress(level = newLevel, xp = newXp)
        c = c.copy(hobbyData = serializeHobbyData(progress))

        // Apply stat effects
        c = applyHobbyStatEffects(c, hobby)
        // Apply costs
        c = c.copy(
            cash = (c.cash - hobby.costPerSession).coerceAtLeast(0.0),
            energy = (c.energy - hobby.energyCost).coerceIn(0.0, 100.0)
        )

        val message = if (leveledUp) {
            "You practiced $hobby.name! Level up! Now level $newLevel — ${getMasteryTitle(newLevel)}"
        } else {
            "You practiced $hobby.name. XP: ${newXp.toInt()}/$xpPerLevel"
        }
        return c to message
    }

    /** Monetize a hobby — earn cash based on mastery level */
    fun monetizeHobby(character: CharacterEntity, hobbyId: String): Pair<CharacterEntity, String> {
        if (!character.hobbies.contains(hobbyId)) return character to "You haven't started this hobby yet."
        val hobby = hobbies.find { it.id == hobbyId } ?: return character to "Unknown hobby."
        val progress = parseHobbyData(character.hobbyData)
        val current = progress[hobbyId] ?: HobbyProgress(level = 1, xp = 0.0)
        if (current.level < 3) return character to "Need at least level 3 mastery to monetize this hobby."

        val earnings = hobby.incomePerLevel * current.level * (0.8 + Random.nextDouble() * 0.4)
        val c = character.copy(cash = character.cash + earnings)
        val message = "You sold your ${hobby.name} work for $${String.format("%.0f", earnings)}! (Mastery ${current.level}/10)"
        return c to message
    }

    /** Decay hobbies during age-up (small XP loss for un-practiced hobbies) */
    fun decayHobbies(character: CharacterEntity): CharacterEntity {
        if (character.hobbies.isEmpty()) return character
        val progress = parseHobbyData(character.hobbyData)
        var changed = false
        progress.forEach { (id, prog) ->
            if (prog.level > 1 && prog.xp > 0 && Random.nextFloat() < 0.3f) {
                val decay = 2.0 + Random.nextDouble() * 5.0
                var newXp = (prog.xp - decay).coerceAtLeast(0.0)
                var newLevel = prog.level
                if (newXp <= 0 && newLevel > 1) {
                    newLevel--
                    newXp = xpPerLevel - decay
                }
                progress[id] = HobbyProgress(level = newLevel, xp = newXp)
                changed = true
            }
        }
        return if (changed) character.copy(hobbyData = serializeHobbyData(progress)) else character
    }

    /** Get hobby progress for display */
    fun getProgress(character: CharacterEntity, hobbyId: String): HobbyProgress? {
        if (!character.hobbies.contains(hobbyId)) return null
        return parseHobbyData(character.hobbyData)[hobbyId]
    }

    /** Get all hobby progresses for the character */
    fun getAllProgress(character: CharacterEntity): Map<String, HobbyProgress> =
        parseHobbyData(character.hobbyData).toMap()

    fun getMasteryTitle(level: Int): String = when {
        level >= 10 -> "Grandmaster"
        level >= 8 -> "Expert"
        level >= 6 -> "Advanced"
        level >= 4 -> "Intermediate"
        level >= 2 -> "Apprentice"
        else -> "Beginner"
    }

    private fun applyHobbyStatEffects(character: CharacterEntity, hobby: HobbyDef): CharacterEntity {
        var c = character
        hobby.statEffects.forEach { (stat, delta) ->
            c = applyStat(c, stat, delta)
        }
        return c
    }

    private fun applyStat(c: CharacterEntity, stat: String, delta: Double): CharacterEntity = when (stat.lowercase()) {
        "health" -> c.copy(health = (c.health + delta).coerceIn(0.0, 100.0))
        "happiness" -> c.copy(happiness = (c.happiness + delta).coerceIn(0.0, 100.0))
        "smarts" -> c.copy(smarts = (c.smarts + delta).coerceIn(0.0, 100.0))
        "looks" -> c.copy(looks = (c.looks + delta).coerceIn(0.0, 100.0))
        "stress" -> c.copy(stress = (c.stress + delta).coerceIn(0.0, 100.0))
        "energy" -> c.copy(energy = (c.energy + delta).coerceIn(0.0, 100.0))
        "athleticism" -> c.copy(athleticism = (c.athleticism + delta).coerceIn(0.0, 100.0))
        "charisma" -> c.copy(charisma = (c.charisma + delta).coerceIn(0.0, 100.0))
        "creativity" -> c.copy(creativity = (c.creativity + delta).coerceIn(0.0, 100.0))
        "discipline" -> c.copy(discipline = (c.discipline + delta).coerceIn(0.0, 100.0))
        "empathy" -> c.copy(empathy = (c.empathy + delta).coerceIn(0.0, 100.0))
        "aggression" -> c.copy(aggression = (c.aggression + delta).coerceIn(0.0, 100.0))
        "sanity" -> c.copy(sanity = (c.sanity + delta).coerceIn(0.0, 100.0))
        "hunger" -> c.copy(hunger = (c.hunger + delta).coerceIn(0.0, 100.0))
        "reputation" -> c.copy(reputation = (c.reputation + delta.toInt()).coerceIn(0, 100))
        "karma" -> c.copy(karma = (c.karma + delta.toInt()).coerceIn(0, 100))
        else -> c
    }
}
