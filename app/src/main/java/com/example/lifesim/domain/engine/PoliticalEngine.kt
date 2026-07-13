// =========================================
// File: domain/engine/PoliticalEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

data class PoliticalOffice(val title: String, val tier: Int, val salary: Double, val termYears: Int, val power: Int)

@Singleton
class PoliticalEngine @Inject constructor() {

    val offices = listOf(
        PoliticalOffice("City Council Member", 1, 45000.0, 4, 10),
        PoliticalOffice("Mayor", 2, 85000.0, 4, 25),
        PoliticalOffice("State Representative", 3, 110000.0, 2, 40),
        PoliticalOffice("State Senator", 4, 130000.0, 4, 55),
        PoliticalOffice("Governor", 5, 200000.0, 4, 70),
        PoliticalOffice("US Representative", 6, 174000.0, 2, 80),
        PoliticalOffice("US Senator", 7, 174000.0, 6, 90),
        PoliticalOffice("Vice President", 8, 284000.0, 4, 95),
        PoliticalOffice("President", 9, 400000.0, 4, 100)
    )

    val royalTitles = listOf("Baron", "Viscount", "Count", "Marquess", "Duke", "Prince", "King/Queen")

    fun runCampaign(character: CharacterEntity, office: PoliticalOffice): CampaignResult {
        val charismaBonus = character.charisma * 0.3
        val smartsBonus = character.smarts * 0.2
        val reputationBonus = character.reputation * 0.2
        val cashNeeded = office.tier * 50000.0
        val cashPenalty = if (character.cash < cashNeeded) -20.0 else 0.0
        val notorietyPenalty = character.notoriety * 0.2
        val approval = (charismaBonus + smartsBonus + reputationBonus + cashPenalty - notorietyPenalty).coerceIn(0.0, 100.0)
        val winChance = approval / 100.0 * 0.6 + Random.nextDouble() * 0.4
        val win = Random.nextDouble() < winChance

        var c = character.copy(cash = character.cash - cashNeeded * 0.3)
        val scandal = Random.nextFloat() < 0.1f

        return if (win) {
            c = c.copy(cash = c.cash + office.salary, reputation = (c.reputation + 15).coerceIn(0, 100),
                stress = (c.stress + 10.0).coerceIn(0.0, 100.0), politicalOfficeTitle = office.title,
                politicalOfficeTier = office.tier, politicalTermYearsRemaining = office.termYears)
            CampaignResult(true, "You won the election for ${office.title}! Salary: $${String.format("%.0f", office.salary)}/yr", c, office)
        } else {
            c = c.copy(reputation = (c.reputation - 5).coerceIn(0, 100), cash = c.cash)
            val reason = if (scandal) "A scandal derailed your campaign." else "The voters chose someone else."
            CampaignResult(false, "You lost the ${office.title} election. $reason", c, null)
        }
    }

    fun serveTerm(character: CharacterEntity, officeTitle: String?): PoliticalAction {
        var c = character
        val corruption = Random.nextFloat() < 0.08f
        val approvalEvent = Random.nextFloat()

        c = c.copy(stress = (c.stress + 5.0).coerceIn(0.0, 100.0), notoriety = c.notoriety + 1,
            karma = (c.karma + if (corruption) -5 else 2).coerceIn(0, 100))

        return when {
            corruption -> PoliticalAction("Corruption scandal! You were investigated.", c.copy(reputation = (c.reputation - 20).coerceIn(0, 100), stress = (c.stress + 20.0).coerceIn(0.0, 100.0)))
            approvalEvent < 0.2f -> PoliticalAction("Your approval rating dropped after a policy failure.", c.copy(reputation = (c.reputation - 5).coerceIn(0, 100)))
            approvalEvent < 0.5f -> PoliticalAction("You passed a meaningful bill. Your constituents are happy.", c.copy(reputation = (c.reputation + 8).coerceIn(0, 100), happiness = (c.happiness + 5.0).coerceIn(0.0, 100.0)))
            approvalEvent < 0.8f -> PoliticalAction("Routine governance. Nothing remarkable.", c)
            else -> PoliticalAction("You gave a powerful speech that went viral!", c.copy(charisma = (c.charisma + 3.0).coerceIn(0.0, 100.0), reputation = (c.reputation + 10).coerceIn(0, 100)))
        }
    }

    fun grantRoyalTitle(character: CharacterEntity): RoyaltyResult {
        val currentIdx = royalTitles.indexOf(character.royalTitle)
        val nextIdx = (currentIdx + 1).coerceAtMost(royalTitles.size - 1)
        if (currentIdx >= royalTitles.size - 1) {
            return RoyaltyResult(false, "You've reached the highest title.", character)
        }
        val newTitle = royalTitles[nextIdx]
        return RoyaltyResult(true, "You've been granted the title of $newTitle!",
            character.copy(royalTitle = newTitle, reputation = (character.reputation + 20).coerceIn(0, 100),
                cash = character.cash + 100000.0 * nextIdx))
    }

    fun courtIntrigue(character: CharacterEntity): PoliticalAction {
        val intrigue = Random.nextFloat()
        var c = character
        return when {
            intrigue < 0.2f -> PoliticalAction("A rival at court spread rumors about you.", c.copy(reputation = (c.reputation - 10).coerceIn(0, 100), paranoia = (c.paranoia + 5.0).coerceIn(0.0, 100.0)))
            intrigue < 0.4f -> PoliticalAction("You discovered a conspiracy against the crown! Your loyalty is noted.", c.copy(reputation = (c.reputation + 15).coerceIn(0, 100), karma = (c.karma + 5).coerceIn(0, 100)))
            intrigue < 0.7f -> PoliticalAction("A noble offers their daughter/son for marriage. Political alliance formed.", c.copy(reputation = (c.reputation + 8).coerceIn(0, 100)))
            else -> PoliticalAction("You hosted a magnificent ball. The nobility is impressed.", c.copy(charisma = (c.charisma + 4.0).coerceIn(0.0, 100.0), happiness = (c.happiness + 5.0).coerceIn(0.0, 100.0), cash = c.cash - 50000.0))
        }
    }
}

data class CampaignResult(val success: Boolean, val message: String, val character: CharacterEntity, val office: PoliticalOffice? = null)
data class PoliticalAction(val description: String, val character: CharacterEntity)
data class RoyaltyResult(val success: Boolean, val message: String, val character: CharacterEntity)
