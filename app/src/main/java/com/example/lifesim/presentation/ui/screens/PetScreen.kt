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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.domain.engine.PetBreed
import com.example.lifesim.domain.engine.PetType
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

private data class PetStat(val label: String, val value: Double, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Pets", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return

        val hasPets = c.petsData.isNotBlank() && c.petsData.contains("|")
        val petEntries = if (hasPets) c.petsData.split("|") else emptyList()

        // Active Pets
        if (petEntries.isNotEmpty()) {
            petEntries.forEach { entry ->
                val parts = entry.split(",")
                if (parts.size >= 5) {
                    val id = parts[0]; val breed = parts[1]; val name = parts[2]
                    val petBreed = PetBreed.entries.find { it.name == breed }
                    val displayName = petBreed?.displayName ?: breed
                    val isDog = petBreed?.type == PetType.DOG
                    val isCat = petBreed?.type == PetType.CAT
                    val isBirdOrFish = petBreed?.type == PetType.FISH || petBreed?.type == PetType.BIRD
                    val age = parts[3].toIntOrNull() ?: 0; val health = parts[4].toDoubleOrNull() ?: 50.0
                    val happiness = parts[5].toDoubleOrNull() ?: 50.0; val hunger = parts[6].toDoubleOrNull() ?: 50.0
                    val energy = parts[7].toDoubleOrNull() ?: 50.0
                    val cleanliness = parts[8].toDoubleOrNull() ?: 50.0
                    val obedience = parts[9].toDoubleOrNull() ?: 30.0
                    val isAlive = parts.getOrNull(10)?.toBooleanStrictOrNull() ?: true

                    if (!isAlive) return@forEach

                    val typeColor = when {
                        isDog || isCat || petBreed?.type == PetType.HORSE -> Gold
                        isBirdOrFish -> SmartsBlue
                        else -> KarmaPurple
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (isDog) Icons.Rounded.Pets else if (isCat) Icons.Rounded.CrueltyFree else Icons.Rounded.Favorite,
                                    null, tint = typeColor, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("$displayName · ${age}yr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            // Pet stats row
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MiniStat(health, HealthGreen)
                                MiniStat(happiness, HappinessYellow)
                                MiniStat(hunger, StressOrange)
                                MiniStat(obedience, SmartsBlue)
                            }
                            Spacer(Modifier.height(8.dp))
                            // Care buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                ActionIcon(Icons.Rounded.Restaurant, "Feed", HealthGreen) { onAction("pet_feed_$id") }
                                ActionIcon(Icons.Rounded.SportsEsports, "Play", HappinessYellow) { onAction("pet_play_$id") }
                                ActionIcon(Icons.Rounded.LocalHospital, "Vet", Error) { onAction("pet_vet_$id") }
                                ActionIcon(Icons.Rounded.School, "Train", SmartsBlue) { onAction("pet_train_$id") }
                                if (isDog) {
                                    ActionIcon(Icons.Rounded.DirectionsWalk, "Walk", Gold) { onAction("pet_walk_$id") }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // Adoption Section
        Text("Adopt a Pet", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp, top = 4.dp))

        var adoptBreed by remember { mutableStateOf<String?>(null) }
        var adoptCost by remember { mutableStateOf(0) }
        var showAdoptDialog by remember { mutableStateOf(false) }

        val adoptionOptions = listOf(
            "Dog" to listOf("Golden Retriever" to 500, "Labrador" to 500, "Bulldog" to 500, "Poodle" to 500, "German Shepherd" to 600),
            "Cat" to listOf("Persian Cat" to 300, "Siamese Cat" to 300, "Maine Coon" to 400, "Bengal Cat" to 500),
            "Small" to listOf("Budgie" to 100, "Goldfish" to 50, "Syrian Hamster" to 80, "Lop Rabbit" to 150),
            "Exotic" to listOf("Bearded Dragon" to 400, "Ball Python" to 400, "Thoroughbred" to 3000, "Fennec Fox" to 5000)
        )

        adoptionOptions.forEach { (category, breeds) ->
            Text(category, style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                breeds.forEach { (breedName, cost) ->
                    Card(
                        onClick = { adoptBreed = breedName; adoptCost = cost; showAdoptDialog = true },
                        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(120.dp).padding(end = 6.dp)
                    ) {
                        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Pets, null, tint = Gold, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(4.dp))
                            Text(breedName, style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("$${cost}", style = MaterialTheme.typography.labelSmall, color = if (c.cash >= cost) HealthGreen else Error)
                        }
                    }
                }
            }
        }

        // Single adoption dialog
        if (showAdoptDialog && adoptBreed != null) {
            var petName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAdoptDialog = false; petName = "" },
                title = { Text("Name your ${adoptBreed}", color = TextPrimary) },
                text = {
                    OutlinedTextField(value = petName, onValueChange = { petName = it.take(20) },
                        label = { Text("Pet Name") }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent, cursorColor = Accent))
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (petName.isNotBlank()) {
                            onAction("pet_adopt_${adoptBreed}__${petName}")
                            showAdoptDialog = false; petName = ""
                        }
                    }) { Text("Adopt ($${adoptCost})", color = Accent) }
                },
                dismissButton = { TextButton(onClick = { showAdoptDialog = false; petName = "" }) { Text("Cancel", color = TextSecondary) } },
                containerColor = BackgroundCard
            )
        }

        Spacer(Modifier.height(12.dp))
        Text("Tip: Feed and play with pets regularly. Take them to the vet when sick. Up to 4 pets max.", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MiniStat(value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toInt().toString(), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        Box(Modifier.width(24.dp).height(3.dp).background(color.copy(alpha = 0.3f)).padding(0.dp)) {
            Box(Modifier.fillMaxWidth(((value / 100.0).coerceIn(0.0, 1.0)).toFloat()).fillMaxHeight().background(color))
        }
    }
}

@Composable
private fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
        IconButton(onClick = onClick, modifier = Modifier.size(30.dp)) {
            Icon(icon, label, tint = color, modifier = Modifier.size(20.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = MaterialTheme.typography.labelSmall.fontSize)
    }
}
