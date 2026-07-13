package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import com.example.lifesim.data.local.entity.CareerEntity
import com.example.lifesim.data.local.entity.OfficePoliticsAlignment
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

@Singleton
class CareerAndEconomyEngine @Inject constructor() {

    var inflationRate: Double = 0.03
    private var marketTrend: Double = 0.0

    fun simulateYear(marketTrend: Double = Random.nextDouble(-0.1, 0.15)): Double {
        this.marketTrend = marketTrend
        inflationRate = 0.03 + abs(marketTrend) * 0.1
        return marketTrend
    }

    fun calculatePerformance(career: CareerEntity, character: CharacterEntity): Int {
        val base = 50
        val smartsBonus = (character.smarts / 2.0).roundToInt()
        val energyPenalty = if (character.energy < 30) -20 else 0
        val stressPenalty = if (character.stress > 70) -15 else 0
        val tierBonus = (10 - career.jobTier) * 2
        val politicsBonus = when (career.officePoliticsAlignment) {
            OfficePoliticsAlignment.BOSS_FAVORITE -> 15
            OfficePoliticsAlignment.TEAM_PLAYER -> 10
            OfficePoliticsAlignment.UNION_LEADER -> if (career.performanceScore > 60) 20 else -10
            OfficePoliticsAlignment.REBEL -> -15
            else -> 0
        }
        return (base + smartsBonus + energyPenalty + stressPenalty + tierBonus + politicsBonus + Random.nextInt(-10, 11)).coerceIn(0, 100)
    }

    fun calculateSalaryGrowth(career: CareerEntity): Double {
        val base = career.baseSalary * inflationRate
        val performanceBonus = career.currentSalary * (career.performanceScore - 50) / 100.0
        val tierMultiplier = 1.0 + (career.jobTier * 0.05)
        return (base + performanceBonus) * tierMultiplier
    }

    fun shouldPromote(career: CareerEntity): Boolean {
        return career.performanceScore > 80 && career.promotionProgress >= 100 && career.yearsEmployed >= 1
    }

    fun shouldFire(career: CareerEntity): Boolean {
        if (career.performanceScore < 25 && career.yearsEmployed > 1) return Random.nextFloat() < 0.5f
        if (career.performanceScore < 15) return Random.nextFloat() < 0.8f
        return false
    }

    fun promote(career: CareerEntity): CareerEntity {
        val newTier = (career.jobTier + 1).coerceIn(1, 10)
        val newSalary = career.currentSalary * 1.15
        return career.copy(
            jobTier = newTier,
            jobTitle = generatePromotionTitle(career.jobTitle, newTier),
            currentSalary = newSalary,
            promotionProgress = 0,
            performanceScore = (career.performanceScore * 0.8).roundToInt().coerceIn(0, 100)
        )
    }

    fun fire(career: CareerEntity, currentYear: Int = 2024): CareerEntity {
        return career.copy(isActive = false, endYear = currentYear, reasonForLeaving = "fired", firedCount = career.firedCount + 1)
    }

    fun calculateBusinessProfit(baseRevenue: Double, operatingCost: Double, employeeSatisfaction: Int, marketDemand: Double = 1.0): Double {
        val demandMultiplier = (1.0 + marketTrend) * marketDemand
        val satisfactionPenalty = 1.0 - (employeeSatisfaction / 100.0) * 0.3
        val inflationMultiplier = 1.0 + inflationRate
        return (baseRevenue * demandMultiplier * satisfactionPenalty) - (operatingCost * inflationMultiplier)
    }

    fun stockMarketSimulation(portfolioValue: Double, riskLevel: Int = 5): Double {
        val volatility = riskLevel * 0.02
        val trendComponent = marketTrend * 0.5
        val randomComponent = java.util.Random().nextGaussian() * volatility
        val change = (trendComponent + randomComponent).coerceIn(-0.3, 0.3)
        return portfolioValue * (1.0 + change)
    }

    fun realEstateSimulation(propertyValue: Double, neighborhoodQuality: Int, propertyTax: Double): Double {
        val appreciation = (marketTrend * 0.8 + neighborhoodQuality / 100.0 * 0.05).coerceIn(-0.1, 0.2)
        val newValue = propertyValue * (1.0 + appreciation) - propertyTax
        return newValue.coerceAtLeast(propertyValue * 0.5)
    }

    private fun generatePromotionTitle(currentTitle: String, newTier: Int): String {
        val titles = listOf("Junior", "", "Senior", "Lead", "Principal", "Director", "VP", "SVP", "EVP", "CEO")
        val currentIdx = titles.indexOfFirst { it.lowercase() in currentTitle.lowercase() }
        val newIdx = (currentIdx + 1).coerceAtMost(titles.size - 1)
        return if (newIdx == 0) "$currentTitle I" else "${titles[newIdx]} ${currentTitle.split(" ").last()}"
    }
}
