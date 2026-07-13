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
fun CareerScreen(uiState: UiState) {
    val character = uiState.character

    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Career", style = MaterialTheme.typography.headlineMedium, color = TextPrimary,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (character == null) {
            Text("Start a new life to begin your career.", color = TextSecondary)
            return
        }

        // Current Job Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Work, null, tint = SmartsBlue, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Current Job", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                if (character.currentCareerId != null) {
                    Text("Employed", color = Success, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Work in the Activities tab to earn money.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Unemployed", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text("Use the Work activity to earn money and build experience.", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Skills Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AutoGraph, null, tint = Gold, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Skills & Attributes", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                CareerStat("Smarts", character.smarts, SmartsBlue)
                CareerStat("Discipline", character.discipline, KarmaPurple)
                CareerStat("Creativity", character.creativity, LooksPink)
                CareerStat("Stress", character.stress, StressOrange)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Actions
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TipsAndUpdates, null, tint = HappinessYellow, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Career Tips", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text("• Study to increase Smarts for better jobs", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("• Work hard for promotions and raises", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("• Manage Stress to avoid burnout", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text("• Build Charisma through Socializing", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Work stats
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccountBalance, null, tint = Gold, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Finances", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                InfoDetail("Cash", "$${String.format("%,.0f", character.cash)}")
                InfoDetail("Net Worth", "$${String.format("%,.0f", character.totalNetWorth)}")
            }
        }
    }
}

@Composable
private fun CareerStat(label: String, value: Double, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text("${value.toInt()}/100", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
