package com.example.lifesim.presentation.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

data class HobbyDisplay(
    val id: String,
    val name: String,
    val category: String,
    val level: Int,
    val xp: Int,
    val xpMax: Int,
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val has: Boolean
)

private val categoryIcons = mapOf(
    "CREATIVE" to Icons.Rounded.Palette,
    "PHYSICAL" to Icons.Rounded.DirectionsRun,
    "INTELLECTUAL" to Icons.Rounded.Psychology,
    "SOCIAL" to Icons.Rounded.Groups,
    "DOMESTIC" to Icons.Rounded.Home,
    "OUTDOOR" to Icons.Rounded.Forest
)

private val hobbyNames = mapOf(
    "painting" to "Painting", "writing" to "Writing", "music" to "Music", "photography" to "Photography",
    "pottery" to "Pottery", "running" to "Running", "yoga" to "Yoga", "dance" to "Dance",
    "martial_arts" to "Martial Arts", "swimming" to "Swimming", "cycling" to "Cycling",
    "chess" to "Chess", "programming" to "Programming", "reading" to "Reading",
    "languages" to "Languages", "puzzles" to "Puzzles", "board_games" to "Board Games",
    "volunteering" to "Volunteering", "cooking" to "Cooking", "baking" to "Baking",
    "gardening" to "Gardening", "woodworking" to "Woodworking", "hiking" to "Hiking",
    "fishing" to "Fishing", "camping" to "Camping", "astronomy" to "Astronomy"
)

private val hobbyIcons = mapOf(
    "painting" to Icons.Rounded.Palette, "writing" to Icons.Rounded.Edit, "music" to Icons.Rounded.MusicNote,
    "photography" to Icons.Rounded.CameraAlt, "pottery" to Icons.Rounded.Handyman,
    "running" to Icons.Rounded.DirectionsRun, "yoga" to Icons.Rounded.SelfImprovement,
    "dance" to Icons.Rounded.MusicNote, "martial_arts" to Icons.Rounded.SportsMartialArts,
    "swimming" to Icons.Rounded.Pool, "cycling" to Icons.Rounded.PedalBike,
    "chess" to Icons.Rounded.Castle, "programming" to Icons.Rounded.Code,
    "reading" to Icons.Rounded.MenuBook, "languages" to Icons.Rounded.Translate,
    "puzzles" to Icons.Rounded.Extension, "board_games" to Icons.Rounded.Games,
    "volunteering" to Icons.Rounded.VolunteerActivism,
    "cooking" to Icons.Rounded.Restaurant, "baking" to Icons.Rounded.BakeryDining,
    "gardening" to Icons.Rounded.Yard, "woodworking" to Icons.Rounded.Handyman,
    "hiking" to Icons.Rounded.Hiking, "fishing" to Icons.Rounded.SetMeal,
    "camping" to Icons.Rounded.NightShelter, "astronomy" to Icons.Rounded.Star
)

private val categoryColors = mapOf(
    "CREATIVE" to LooksPink, "PHYSICAL" to HealthGreen, "INTELLECTUAL" to SmartsBlue,
    "SOCIAL" to HappinessYellow, "DOMESTIC" to StressOrange, "OUTDOOR" to Gold
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HobbyScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Hobbies & Skills", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return

        // Check if hobby data is parseable
        val hobbiesList = c.hobbies
        val hobbyDataRaw = c.hobbyData

        // Active Hobbies Card
        if (hobbiesList.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Your Hobbies", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    val hobbyEntries = hobbyDataRaw.split(",").filter { it.isNotBlank() }
                    hobbyEntries.take(8).forEach { entry ->
                        val parts = entry.split(":")
                        if (parts.size >= 2) {
                            val id = parts[0]; val level = parts[1].toIntOrNull() ?: 1; val xp = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
                            val name = id.replace("_", " ").replaceFirstChar { it.uppercase() }
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(hobbyIcons[id] ?: Icons.Rounded.Favorite, null, tint = categoryColors.entries.firstOrNull { e -> e.key == id }?.value ?: Accent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(name, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        Text("Lv.$level", style = MaterialTheme.typography.labelSmall, color = when { level >= 8 -> Gold; level >= 5 -> Accent; else -> TextSecondary })
                                    }
                                    LinearProgressIndicator(
                                        progress = (xp / 100.0).toFloat().coerceIn(0f, 1f),
                                        modifier = Modifier.fillMaxWidth().height(3.dp).padding(top = 1.dp),
                                        color = Accent, trackColor = BackgroundDark
                                    )
                                }
                            }
                        }
                    }
                    if (hobbyEntries.size > 8) {
                        Text("...and ${hobbyEntries.size - 8} more", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Category Tabs
        Text("Discover Hobbies", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        val categories = listOf("CREATIVE", "PHYSICAL", "INTELLECTUAL", "SOCIAL", "DOMESTIC", "OUTDOOR")
        var selectedCategory by remember { mutableStateOf(categories.first()) }

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 8.dp)) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { Icon(categoryIcons[cat] ?: Icons.Rounded.Favorite, null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = categoryColors[cat]?.copy(alpha = 0.2f) ?: Accent.copy(alpha = 0.2f), selectedLabelColor = categoryColors[cat] ?: Accent),
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        // Hobby Grid for selected category
        val categoryHobbies = listOf(
            "CREATIVE" to listOf("painting", "writing", "music", "photography", "pottery"),
            "PHYSICAL" to listOf("running", "yoga", "dance", "martial_arts", "swimming", "cycling"),
            "INTELLECTUAL" to listOf("chess", "programming", "reading", "languages", "puzzles"),
            "SOCIAL" to listOf("board_games", "volunteering"),
            "DOMESTIC" to listOf("cooking", "baking", "gardening", "woodworking"),
            "OUTDOOR" to listOf("hiking", "fishing", "camping", "astronomy")
        ).toMap()

        categoryHobbies[selectedCategory]?.forEach { hobbyId ->
            val hasHobby = hobbiesList.contains(hobbyId)
            val entry = hobbyDataRaw.split(",").find { it.startsWith("$hobbyId:") }
            val parts = entry?.split(":") ?: emptyList()
            val level = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val xp = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0

            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(hobbyIcons[hobbyId] ?: Icons.Rounded.Favorite, null,
                        tint = categoryColors[selectedCategory] ?: Accent, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(hobbyNames[hobbyId] ?: hobbyId, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                        if (hasHobby) {
                            Text("Level $level · ${xp.toInt()}/100 XP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            LinearProgressIndicator(
                                progress = (xp / 100.0).toFloat().coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth().height(3.dp).padding(top = 2.dp),
                                color = Accent, trackColor = BackgroundDark
                            )
                        } else {
                            Text("Not yet started", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                    }
                    if (hasHobby) {
                        Column {
                            IconButton(onClick = { onAction("hobby_practice_$hobbyId") }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Rounded.PlayArrow, "Practice", tint = HealthGreen, modifier = Modifier.size(20.dp))
                            }
                            if (level >= 3) {
                                IconButton(onClick = { onAction("hobby_sell_$hobbyId") }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Rounded.AttachMoney, "Sell", tint = Gold, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    } else {
                        IconButton(onClick = { onAction("hobby_start_$hobbyId") }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Rounded.Add, "Start", tint = Accent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Tip: Practice hobbies to gain mastery. Reach level 3+ to sell your work for cash!", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
    }
}
