// =========================================
// File: domain/engine/InvestmentEngine.kt
// =========================================
package com.example.lifesim.domain.engine

import com.example.lifesim.data.local.entity.CharacterEntity
import kotlin.math.roundToInt
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

data class Investment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: InvestmentType,
    val amountInvested: Double,
    var currentValue: Double,
    val riskLevel: Int, // 1-10
    val dividendYield: Double = 0.0,
    val purchaseYear: Int
)

enum class InvestmentType { STOCK, BOND, CRYPTO, MUTUAL_FUND, ETF, RETIREMENT_401K, RETIREMENT_IRA, REAL_ESTATE_FUND, COMMODITY }

@Singleton
class InvestmentEngine @Inject constructor() {

    private var marketPhase = MarketPhase.BULL
    private var marketVolatility = 0.15

    enum class MarketPhase { BEAR, BULL, STAGNANT, CRASH, RECOVERY }

    fun simulateMarket(): MarketPhase {
        marketPhase = when {
            Random.nextFloat() < 0.05f -> MarketPhase.CRASH
            Random.nextFloat() < 0.15f -> MarketPhase.BEAR
            Random.nextFloat() < 0.5f -> MarketPhase.BULL
            Random.nextFloat() < 0.75f -> MarketPhase.STAGNANT
            else -> MarketPhase.RECOVERY
        }
        marketVolatility = when (marketPhase) {
            MarketPhase.CRASH -> 0.4
            MarketPhase.BEAR -> 0.25
            MarketPhase.BULL -> 0.12
            MarketPhase.STAGNANT -> 0.05
            MarketPhase.RECOVERY -> 0.2
        }
        return marketPhase
    }

    fun createInvestment(character: CharacterEntity, type: InvestmentType, amount: Double, year: Int): Investment? {
        if (amount > character.cash) return null
        val name = when (type) {
            InvestmentType.STOCK -> listOf("Apple Inc.", "Amazon.com", "Microsoft Corp", "Tesla Inc", "NVIDIA Corp", "Berkshire Hathaway", "JPMorgan Chase", "Johnson & Johnson").random()
            InvestmentType.CRYPTO -> listOf("Bitcoin", "Ethereum", "Solana", "Cardano", "Polkadot", "Chainlink").random()
            InvestmentType.BOND -> listOf("US Treasury 10yr", "Corporate Bond AAA", "Municipal Bond", "TIPS").random()
            InvestmentType.MUTUAL_FUND -> listOf("Vanguard S&P 500", "Fidelity Growth", "T. Rowe Price Blue Chip", "American Funds Growth").random()
            InvestmentType.ETF -> listOf("SPY (S&P 500 ETF)", "QQQ (Nasdaq ETF)", "VTI (Total Market)", "BND (Bond ETF)").random()
            InvestmentType.RETIREMENT_401K -> listOf("401(k) Target Date 2050", "401(k) Growth Portfolio").random()
            InvestmentType.RETIREMENT_IRA -> listOf("Roth IRA Growth", "Traditional IRA Bond Mix").random()
            else -> return null
        }
        val risk = when (type) { InvestmentType.CRYPTO -> 9; InvestmentType.STOCK -> 6; InvestmentType.BOND -> 2; InvestmentType.ETF -> 4; InvestmentType.MUTUAL_FUND -> 5; InvestmentType.RETIREMENT_401K -> 4; InvestmentType.RETIREMENT_IRA -> 3; else -> 5 }
        return Investment(name = name, type = type, amountInvested = amount, currentValue = amount, riskLevel = risk, purchaseYear = year)
    }

    fun simulateReturn(investment: Investment): Double {
        val baseReturn = when (marketPhase) {
            MarketPhase.BULL -> 0.08 + java.util.Random().nextGaussian() * marketVolatility
            MarketPhase.BEAR -> -0.12 + java.util.Random().nextGaussian() * marketVolatility
            MarketPhase.CRASH -> -0.3 + java.util.Random().nextGaussian() * marketVolatility
            MarketPhase.STAGNANT -> 0.02 + java.util.Random().nextGaussian() * marketVolatility
            MarketPhase.RECOVERY -> 0.15 + java.util.Random().nextGaussian() * marketVolatility
        }
        val riskAdjusted = baseReturn * (1.0 + investment.riskLevel * 0.05)
        val maxChange = when (marketPhase) { MarketPhase.CRASH -> -0.5; else -> 0.3 }
        return riskAdjusted.coerceIn(maxChange, 0.3)
    }

    fun calculateDividends(investment: Investment): Double = investment.currentValue * investment.dividendYield * 0.25

    fun marketCrashWarning(): Boolean = marketPhase == MarketPhase.CRASH

    fun getAdvice(character: CharacterEntity): String = when (marketPhase) {
        MarketPhase.CRASH -> "The market is crashing! Consider holding or buying the dip if you have cash."
        MarketPhase.BEAR -> "Bear market. Good time to dollar-cost average into index funds."
        MarketPhase.BULL -> "Bull market! Your portfolio is growing. Consider taking some profits."
        MarketPhase.STAGNANT -> "Market is flat. Dividend stocks are your best bet."
        MarketPhase.RECOVERY -> "Recovery phase! Growth stocks are rebounding."
    }
}
