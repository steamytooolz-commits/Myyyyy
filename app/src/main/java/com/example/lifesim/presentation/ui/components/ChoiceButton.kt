// =========================================
// File: presentation/ui/components/ChoiceButton.kt
// =========================================
package com.example.lifesim.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceButton(
    text: String,
    statChanges: Map<String, Double>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                if (statChanges.isNotEmpty()) {
                    val netChange = statChanges.values.sum()
                    val arrowColor = if (netChange > 0) Success else if (netChange < 0) Error else TextSecondary
                    Text(
                        text = if (netChange > 0) "↑" else if (netChange < 0) "↓" else "→",
                        color = arrowColor, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            if (statChanges.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    statChanges.entries.take(4).forEach { (stat, change) ->
                        if (change != 0.0) {
                            val chipColor = when(stat.lowercase()) {
                                "health" -> HealthGreen; "happiness" -> HappinessYellow; "smarts" -> SmartsBlue
                                "looks" -> LooksPink; "karma" -> KarmaPurple; "stress" -> StressOrange
                                "reputation" -> ReputationTeal; "money", "cash" -> Gold; else -> TextSecondary
                            }
                            Surface(color = chipColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "${if(change > 0)"+" else ""}${change.toInt()} ${stat.take(3).uppercase()}",
                                    style = MaterialTheme.typography.labelSmall, color = chipColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
