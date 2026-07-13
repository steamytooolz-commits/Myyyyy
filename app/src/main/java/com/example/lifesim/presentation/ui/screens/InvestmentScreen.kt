package com.example.lifesim.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.components.StatLine
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun InvestmentScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Investments", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return

        // Portfolio Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Your Portfolio", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatLine("Cash", "$${String.format("%,.0f", c.cash)}")
                StatLine("Stock Portfolio", "$${String.format("%,.0f", c.stockPortfolioValue)}")
                StatLine("Retirement Savings", "$${String.format("%,.0f", c.retirementSavings)}")
                StatLine("Total Invested", "$${String.format("%,.0f", c.stockPortfolioValue + c.retirementSavings)}")
            }
        }
        Spacer(Modifier.height(12.dp))

        // Market Advice Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrendingUp, null, tint = Gold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                Text("Markets fluctuate yearly. Diversify to reduce risk.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Action Buttons
        Text("Investment Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        InvestActionCard("Buy Stocks", "Invest cash into the stock market (min $1,000)", Icons.Rounded.TrendingUp, Gold, c.cash >= 1000) { onAction("invest_stocks") }
        InvestActionCard("Buy Crypto", "High-risk digital assets (min $500)", Icons.Rounded.MonetizationOn, StressOrange, c.cash >= 500) { onAction("invest_crypto") }
        InvestActionCard("Retirement Contribution", "Tax-advantaged savings for the future", Icons.Rounded.Savings, SmartsBlue, c.cash >= 500) { onAction("invest_retirement") }
        InvestActionCard("Research Stocks", "Study the market for smarter decisions", Icons.Rounded.MenuBook, KarmaPurple, true) { onAction("research_stocks") }
        InvestActionCard("Day Trade", "High risk, high reward quick trading", Icons.Rounded.FlashOn, Gold, c.stockPortfolioValue > 1000) { onAction("day_trade") }
        InvestActionCard("Diversify Portfolio", "Balance your investments across sectors", Icons.Rounded.AccountBalance, HealthGreen, c.stockPortfolioValue > 2000) { onAction("diversify_investments") }

        Spacer(Modifier.height(16.dp))
        Text("Tip: Retirement accounts grow ~5-8% yearly with compound interest. Start early!", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestActionCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: androidx.compose.ui.graphics.Color, enabled: Boolean, onClick: () -> Unit) {
    Card(
        onClick = { if (enabled) onClick() },
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (enabled) iconColor else TextTertiary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = if (enabled) TextPrimary else TextTertiary, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = if (enabled) Accent else TextTertiary)
        }
    }
}
