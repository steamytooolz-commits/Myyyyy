package com.example.lifesim.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifesim.presentation.ui.components.StatBar
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.Screen
import com.example.lifesim.presentation.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: UiState,
    onStartNewGame: () -> Unit,
    onAgeUp: () -> Unit,
    onScreenChange: (Screen) -> Unit
) {
    val character = uiState.character
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        if (character == null || !character.isAlive) {
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (character == null) {
                        Text("Welcome to Aeterna", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                    } else {
                        Text("Your Journey Has Ended", style = MaterialTheme.typography.headlineMedium, color = Error, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        val age = (uiState.currentYear - (character.dateOfBirth / 31557600000L).toInt()).coerceAtLeast(0)
                        Text("${character.firstName} ${character.lastName} passed away at age $age.", style = MaterialTheme.typography.bodyLarge, color = TextPrimary, textAlign = TextAlign.Center)
                        uiState.deathMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text("Cause of death: $it", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    Button(onClick = onStartNewGame, colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Rounded.PlayArrow, null); Spacer(Modifier.width(8.dp))
                        Text("Start New Life")
                    }
                }
            }
        } else {
            // Character header
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(30.dp)).background(Accent.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Person, null, tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("${character.firstName} ${character.lastName}", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Year ${uiState.currentYear} · Age ${(uiState.currentYear - (character.dateOfBirth / 31557600000L).toInt()).coerceAtLeast(0)}",
                            style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text("Cash: $${String.format("%,.0f", character.cash)}", style = MaterialTheme.typography.bodySmall, color = Gold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Stats
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Core Stats", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    StatBar("Health", character.health, color = HealthGreen)
                    StatBar("Happiness", character.happiness, color = HappinessYellow)
                    StatBar("Smarts", character.smarts, color = SmartsBlue)
                    StatBar("Looks", character.looks, color = LooksPink)
                    StatBar("Stress", character.stress, color = StressOrange, inverted = true)
                    StatBar("Energy", character.energy, color = Gold)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Addiction Status
            if (character.isAddicted || character.isInWithdrawal || character.addictionRecoveryProgress > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, null, tint = if (character.isInWithdrawal) Error else StressOrange, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        val status = when {
                            character.isInWithdrawal -> "Withdrawal: ${character.addictionType?.replaceFirstChar { it.uppercase() } ?: "Unknown"}"
                            character.addictionRecoveryProgress > 0 -> "In Recovery: ${character.addictionType?.replaceFirstChar { it.uppercase() } ?: "Unknown"}"
                            else -> "Addicted: ${character.addictionType?.replaceFirstChar { it.uppercase() } ?: "Unknown"} (Severity: ${character.addictionSeverity}/10)"
                        }
                        Text(status, style = MaterialTheme.typography.labelSmall, color = if (character.isInWithdrawal) Error else StressOrange)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // World Events Status
            if (uiState.activeWorldEvents.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Public, null, tint = Gold, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("World Events", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        uiState.activeWorldEvents.forEach { event ->
                            val iconColor = when {
                                event.severity >= 8 -> Error
                                event.severity >= 5 -> StressOrange
                                else -> HealthGreen
                            }
                            Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Circle, null, tint = iconColor, modifier = Modifier.size(8.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("${event.title} (${event.duration}yr)", style = MaterialTheme.typography.labelSmall, color = TextPrimary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Age Up Button
            Button(onClick = onAgeUp, modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)) {
                Icon(Icons.Rounded.AutoAwesome, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text("AGE UP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(16.dp))

            // Event display is handled by EventModalScreen overlay in NavGraph
        }
    }
}
