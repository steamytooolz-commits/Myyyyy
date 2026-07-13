package com.example.lifesim.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun HistoryScreen(uiState: UiState) {
    val memoriesByYear = uiState.memories.groupBy { it.yearOccurred }.toSortedMap(reverseOrder())

    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Life Timeline",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (memoriesByYear.isEmpty()) {
            Text("No history yet. Actions will appear here.", color = TextSecondary)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                memoriesByYear.forEach { (year, memories) ->
                    item {
                        Text(
                            text = "Year $year",
                            style = MaterialTheme.typography.titleMedium,
                            color = Accent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(memories) { memory ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(memory.description, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Age ${memory.ageOccurred}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(memory.eventType.name.capitalize(), style = MaterialTheme.typography.bodySmall, color = KarmaPurple)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
