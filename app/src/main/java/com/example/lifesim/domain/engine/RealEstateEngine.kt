// =========================================
// File: domain/engine/RealEstateEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

data class Property(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val propertyType: PropertyType,
    val purchasePrice: Double,
    var currentValue: Double,
    val neighborhood: NeighborhoodType,
    val bedrooms: Int = 1,
    val bathrooms: Int = 1,
    val squareFeet: Int = 800,
    val yearBuilt: Int = 2020,
    val mortgageBalance: Double = 0.0,
    val mortgageRate: Double = 0.06,
    val monthlyMortgage: Double = 0.0,
    val propertyTaxRate: Double = 0.012,
    val monthlyHoa: Double = 0.0,
    var conditionScore: Int = 100,
    val hasTenant: Boolean = false,
    val monthlyRent: Double = 0.0,
    val isPrimaryResidence: Boolean = true
)

enum class PropertyType { HOUSE, CONDO, APARTMENT, TOWNHOUSE, MANSION, ESTATE, COMMERCIAL, LAND }
enum class NeighborhoodType { RURAL, SUBURBAN, URBAN, DOWNTOWN, LUXURY, SLUM }

@Singleton
class RealEstateEngine @Inject constructor() {

    private var marketTrend = 0.0

    fun simulateMarket(): Double {
        marketTrend = java.util.Random().nextGaussian() * 0.08 // -20% to +20% yearly swing
        return marketTrend
    }

    fun generateProperty(budget: Double): Property {
        val propType = when {
            budget > 2000000 -> PropertyType.MANSION
            budget > 500000 -> PropertyType.HOUSE
            budget > 200000 -> PropertyType.CONDO
            else -> PropertyType.APARTMENT
        }
        val neighborhood = when {
            budget > 1000000 -> NeighborhoodType.LUXURY
            budget > 300000 -> NeighborhoodType.SUBURBAN
            else -> if (Random.nextFloat() < 0.3f) NeighborhoodType.SLUM else NeighborhoodType.URBAN
        }
        val name = when (propType) {
            PropertyType.MANSION -> listOf("The Pemberley Estate", "Rosewood Manor", "Beverly Hills Villa", "Hampton's Retreat", "Bel Air Residence").random()
            PropertyType.HOUSE -> listOf("Colonial Revival", "Victorian Home", "Ranch Style", "Craftsman Bungalow", "Tudor House").random()
            PropertyType.CONDO -> listOf("Skyline Condo", "Harbor View", "City Lights Suite", "Park Avenue Unit", "Riverfront Loft").random()
            PropertyType.APARTMENT -> listOf("Studio Unit", "Garden Apartment", "Walk-up Flat", "Brownstone Unit", "Basement Suite").random()
            else -> "Property"
        }
        val bedrooms = when (propType) { PropertyType.MANSION -> 5..8; PropertyType.HOUSE -> 3..5; PropertyType.CONDO -> 1..3; else -> 1..2 }.random()
        val bathrooms = (bedrooms * 0.7).roundToInt().coerceAtLeast(1)

        val balance = if (Random.nextFloat() < 0.7f) budget * 0.8 else 0.0
        val rate = 0.05 + Random.nextDouble() * 0.04
        val taxRate = 0.008 + Random.nextDouble() * 0.012
        val mortgage = calculateMortgage(balance, rate)

        return Property(
            name = name, propertyType = propType, purchasePrice = budget, currentValue = budget,
            neighborhood = neighborhood, bedrooms = bedrooms, bathrooms = bathrooms,
            conditionScore = 80 + Random.nextInt(20),
            mortgageBalance = balance,
            mortgageRate = rate,
            monthlyMortgage = mortgage,
            propertyTaxRate = taxRate
        )
    }

    fun appreciateProperty(property: Property): Property {
        val appreciation = marketTrend * (1.0 + property.neighborhood.ordinal * 0.03) + java.util.Random().nextGaussian() * 0.02
        val newValue = (property.currentValue * (1.0 + appreciation)).coerceAtLeast(property.purchasePrice * 0.5)
        val conditionChange = if (property.conditionScore > 30) -Random.nextInt(1, 5) else 0
        return property.copy(currentValue = newValue, conditionScore = (property.conditionScore + conditionChange).coerceIn(0, 100))
    }

    fun generateRentalIncome(property: Property): Double = when (property.neighborhood) {
        NeighborhoodType.LUXURY -> property.currentValue * 0.004
        NeighborhoodType.SUBURBAN -> property.currentValue * 0.005
        NeighborhoodType.URBAN -> property.currentValue * 0.006
        NeighborhoodType.DOWNTOWN -> property.currentValue * 0.0055
        NeighborhoodType.RURAL -> property.currentValue * 0.003
        NeighborhoodType.SLUM -> property.currentValue * 0.008
    }

    fun calculatePropertyTax(property: Property): Double = property.currentValue * property.propertyTaxRate * 0.0833

    fun propertyEvent(property: Property): Pair<String, Property>? {
        if (Random.nextFloat() > 0.08f) return null
        val events = listOf(
            Pair("A pipe burst! Costly repairs.", property.copy(conditionScore = (property.conditionScore - 15).coerceIn(0, 100), currentValue = property.currentValue * 0.95)),
            Pair("The roof needs replacing.", property.copy(conditionScore = (property.conditionScore - 20).coerceIn(0, 100))),
            Pair("Property value increased due to new developments!", property.copy(currentValue = property.currentValue * 1.1)),
            Pair("Neighbor complaint about noise.", property.copy()),
            Pair("Squatters were found on the property.", property.copy(conditionScore = (property.conditionScore - 10).coerceIn(0, 100))),
            Pair("New school district rating boosted property values!", property.copy(currentValue = property.currentValue * 1.08)),
            Pair("Burglary! Some valuables were taken.", property.copy())
        )
        val (desc, updated) = events.random()
        return desc to updated
    }

    fun calculateRepairCost(property: Property): Double = (100 - property.conditionScore) * 50.0

    private fun calculateMortgage(principal: Double, rate: Double, years: Int = 30): Double {
        if (principal <= 0) return 0.0
        val monthlyRate = rate / 12.0
        val payments = years * 12.0
        return principal * (monthlyRate * Math.pow(1 + monthlyRate, payments)) / (Math.pow(1 + monthlyRate, payments) - 1)
    }
}
