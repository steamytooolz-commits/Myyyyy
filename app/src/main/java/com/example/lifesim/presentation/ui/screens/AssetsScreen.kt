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
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun AssetsScreen(uiState: UiState) {
    val character = uiState.character

    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Assets", style = MaterialTheme.typography.headlineMedium, color = TextPrimary,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (character == null) {
            Text("Start a new life to manage assets.", color = TextSecondary)
            return
        }

        // Net Worth Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Gold, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Net Worth", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text("$${String.format("%,.0f", character.totalNetWorth)}",
                    style = MaterialTheme.typography.headlineLarge, color = Gold, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Cash: $${String.format("%,.0f", character.cash)}",
                    style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Portfolio
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PieChart, null, tint = SmartsBlue, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Portfolio", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text("Your investment portfolio and properties.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text("• Cash: $${String.format("%,.0f", character.cash)}", color = Gold, style = MaterialTheme.typography.bodySmall)
                Text("• Total Assets: $${String.format("%,.0f", character.totalNetWorth)}", color = TextPrimary, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Investment tips
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = HappinessYellow, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Wealth Tips", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text("• Work to earn cash and build wealth", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("• Study to qualify for higher-paying jobs", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("• Build your dynasty for generational wealth", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("• Manage stress to maintain performance", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
