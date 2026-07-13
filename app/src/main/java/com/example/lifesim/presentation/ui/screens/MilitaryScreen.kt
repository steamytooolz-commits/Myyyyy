package com.example.lifesim.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.components.StatLine
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun MilitaryScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Military", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Service Record", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatLine("Status", if (c.isInMilitary) "Active Duty" else "Civilian")
                StatLine("Rank", c.militaryRankTitle ?: "N/A")
                StatLine("Years Served", c.militaryYearsServed.toString())
                StatLine("Combat Deployments", c.militaryCombatDeployments.toString())
                if (c.militaryMedals.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text("Medals: ${c.militaryMedals.joinToString(", ")}", color = Gold, style = MaterialTheme.typography.labelSmall) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        if (!c.isInMilitary) {
            Button(
                onClick = { onAction("military_enlist") },
                colors = ButtonDefaults.buttonColors(containerColor = HealthGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enlist in Military", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            Text("Requirements: Age 18+ — Builds discipline, athleticism, and character.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Active Duty Options", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("• Combat Training — hone combat skills", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Military Exercise — physical readiness", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Guard Duty — discipline and vigilance", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Weapons Training — marksmanship", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Perform these through the Activities tab.", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onAction("military_discharge") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Request Discharge", fontWeight = FontWeight.Bold) }
        }
    }
}
