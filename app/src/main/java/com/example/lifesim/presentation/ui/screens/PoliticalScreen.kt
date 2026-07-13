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
fun PoliticalScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Politics & Royalty", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Government & Titles", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatLine("Office", c.politicalOfficeTitle ?: "None")
                StatLine("Approval Rating", "${c.approvalRating}%")
                StatLine("Royal Title", c.royalTitle ?: "None")
                Spacer(Modifier.height(8.dp))
                Text("Run for office and gain royal titles through special actions.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        Button(
            onClick = { onAction("political_run") },
            colors = ButtonDefaults.buttonColors(containerColor = KarmaPurple),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Run for Office", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.height(8.dp))
        Text("Costs money to campaign. Higher charisma/reputation = better odds.", color = TextSecondary, style = MaterialTheme.typography.labelSmall)

        if (c.royalTitle == null) {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onAction("royalty_grant") },
                colors = ButtonDefaults.buttonColors(containerColor = Gold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Seek Royal Title", fontWeight = FontWeight.Bold) }
        } else {
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Political Activities", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("• Campaign Rally — boost visibility", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Fundraiser — raise money", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Public Speech — inspire the masses", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Political Debate — challenge opponents", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Perform these through the Activities tab.", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
