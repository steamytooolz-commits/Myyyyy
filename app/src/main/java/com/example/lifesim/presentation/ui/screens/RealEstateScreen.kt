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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.components.StatLine
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun RealEstateScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Real Estate", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Property Portfolio", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatLine("Total Net Worth", "$${String.format("%,.0f", c.totalNetWorth)}")
                StatLine("Cash Available", "$${String.format("%,.0f", c.cash)}")
                StatLine("Retirement Savings", "$${String.format("%,.0f", c.retirementSavings)}")
                StatLine("Stock Portfolio", "$${String.format("%,.0f", c.stockPortfolioValue)}")
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Real Estate Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Available Activities", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("• Buy Property — invest in real estate", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text("• Sell Property — liquidate your holdings", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text("• Renovate Home — increase property value", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text("• Hire Contractor — professional repairs", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Text("• Property Inspection — assess condition", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text("Perform these through the Activities tab.", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
