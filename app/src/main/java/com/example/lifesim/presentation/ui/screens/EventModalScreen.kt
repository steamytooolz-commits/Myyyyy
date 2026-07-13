package com.example.lifesim.presentation.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifesim.domain.ai.AIEventResult
import com.example.lifesim.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventModalScreen(
    event: AIEventResult,
    onChoice: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        Modifier.fillMaxSize().background(Overlay),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn()
        ) {
            Card(
                Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.85f).clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
                ) {
                    // Header with gradient
                    Box(
                        Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(16.dp))
                            .background(brush = Brush.linearGradient(colors = listOf(Accent, Secondary))),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.AutoAwesome, null, tint = TextPrimary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(event.title, style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text(event.description, style = MaterialTheme.typography.bodyLarge, color = TextSecondary, lineHeight = 28.sp)
                    Spacer(Modifier.height(8.dp))

                    AssistChip(onClick = {}, label = { Text(event.category.uppercase(), style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = BackgroundCard, labelColor = TextSecondary))

                    if (event.isAI) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Rounded.Psychology, null, tint = KarmaPurple, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp)); Text("AI Generated", style = MaterialTheme.typography.labelSmall, color = KarmaPurple)
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Text("What do you do?", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    event.choices.forEachIndexed { i, choice ->
                        Card(
                            onClick = { onChoice(i) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.ChevronRight, null, tint = Accent, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(choice.text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
                                }
                                if (choice.statChanges.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        choice.statChanges.entries.forEach { (stat, change) ->
                                            if (change != 0.0) {
                                                val chipColor = when(stat.lowercase()) {
                                                    "health"->HealthGreen; "happiness"->HappinessYellow; "smarts"->SmartsBlue
                                                    "looks"->LooksPink; "karma"->KarmaPurple; "stress"->StressOrange
                                                    "repuation"->ReputationTeal; "money"->Gold; else -> TextSecondary
                                                }
                                                Surface(color = chipColor.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                                                    Text("${if(change>0)"+" else ""}${change.toInt()} ${stat.uppercase()}",
                                                        style = MaterialTheme.typography.labelSmall, color = chipColor, fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Skip", color = TextSecondary)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
