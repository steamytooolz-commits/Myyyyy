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
fun PrisonScreen(
    uiState: UiState,
    onPrisonAction: (String) -> Unit = {}
) {
    val character = uiState.character

    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState())) {
        // Prison Header
        Box(Modifier.fillMaxWidth().background(BackgroundSurface).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Lock, null, tint = Error, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Incarcerated", style = MaterialTheme.typography.titleLarge, color = Error, fontWeight = FontWeight.Bold)
                    if (character != null) {
                        Text("Year ${character.prisonYearsServed} of your sentence", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }

        Column(Modifier.padding(16.dp)) {
            if (character == null) return

            // Status Card
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Prison Status", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    PrisonInfo("Health", "${character.health.toInt()}/100", "Gang", character.prisonGang ?: "None")
                    PrisonInfo("Sanity", "${character.sanity.toInt()}/100", "Job", character.prisonJob ?: "None")
                    PrisonInfo("Record", "${character.prisonDisciplinaryRecord} infractions", "Escape Progress", "${character.prisonEscapeProgress}/10")
                    PrisonInfo("Allies", if (character.hasPrisonAllies) "Yes" else "No", "Enemies", if (character.hasPrisonEnemies) "Yes" else "No")
                    if (character.prisonContraband.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text("Contraband: ${character.prisonContraband.joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = StressOrange)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Prison Activities
            Text("Prison Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp))

            PrisonActionCard("Join Gang", "Seek protection and power", Icons.Rounded.Groups, StressOrange,
                if (character.prisonGang == null) "Join" else "In ${character.prisonGang}",
                enabled = character.prisonGang == null) { onPrisonAction("prison_join_gang") }

            PrisonActionCard("Take Prison Job", "Earn money and build discipline", Icons.Rounded.Work, SmartsBlue,
                if (character.prisonJob == null) "Apply" else "Working", enabled = character.prisonJob == null) { onPrisonAction("prison_take_job") }

            PrisonActionCard("Plan Escape", "Risk everything for freedom", Icons.Rounded.FlightTakeoff, Gold,
                if (character.prisonEscapeProgress > 0) "Progress: ${character.prisonEscapeProgress}/10" else "Plan", enabled = true) { onPrisonAction("prison_escape") }

            PrisonActionCard("Request Parole", "Appeal for early release", Icons.Rounded.Gavel, KarmaPurple,
                "Appeal", enabled = character.prisonYearsServed > 2) { onPrisonAction("prison_parole") }

            PrisonActionCard("Acquire Contraband", "Get items on the black market", Icons.Rounded.Inventory2, StressOrange,
                "Trade", enabled = true) { onPrisonAction("prison_contraband") }

            Spacer(Modifier.height(16.dp))
            Text("Tip: Join a gang for protection, get a job for income, and study in your free time.", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
    }
}

@Composable
private fun PrisonInfo(label1: String, value1: String, label2: String, value2: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label1, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(value1, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(label2, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.weight(1f))
        Text(value2, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun PrisonActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            if (enabled) {
                TextButton(onClick = onClick) {
                    Text(if (buttonText == "Join" || buttonText == "Apply" || buttonText == "Plan" || buttonText == "Appeal" || buttonText == "Trade") buttonText else "Info",
                        color = if (enabled) Accent else TextTertiary)
                }
            } else {
                Text(buttonText, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}
