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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun DynastyScreen(uiState: UiState) {
    val character = uiState.character

    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Dynasty & Legacy", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp))

        // Family Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AccountTree, null, tint = Gold, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Family", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                InfoRow("Head", "${character?.firstName ?: "?"} ${character?.lastName ?: "?"}")
                InfoRow("Generation", character?.generationNumber?.toString() ?: "1")
                InfoRow("Children", character?.childIds?.size?.toString() ?: "0")
                InfoRow("Spouse", if (character?.spouseId != null) "Yes" else "None")
                InfoRow("Cash", "$${String.format("%,.0f", character?.cash ?: 0.0)}")
            }
        }
        Spacer(Modifier.height(12.dp))

        // Legacy Card
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.StarBorder, null, tint = Diamond, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Legacy", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                if (character?.childIds?.isNotEmpty() == true) {
                    Text("Your legacy will live on through your children.",
                        style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
                    Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.Description, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Draft Will & Testament")
                    }
                } else {
                    Text("Build your legacy by starting a family!", style = MaterialTheme.typography.bodyMedium, color = TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
