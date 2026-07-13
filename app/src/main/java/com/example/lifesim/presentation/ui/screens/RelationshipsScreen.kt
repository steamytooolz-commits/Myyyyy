package com.example.lifesim.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState
import com.example.lifesim.data.local.entity.RelationType
import com.example.lifesim.data.local.entity.Gender
import com.example.lifesim.domain.engine.InteractionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipsScreen(
    uiState: UiState,
    onInteraction: (String, InteractionType) -> Unit = { _, _ -> }
) {
    val character = uiState.character
    val scrollState = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Relationships",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (character == null) {
            Text("Start a new life to build relationships.", color = TextSecondary)
            return
        }

        // Active notification message
        if (uiState.message != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Accent.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Info, null, tint = Accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(uiState.message, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
            }
        }

        // Family Overview section
        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.FamilyRestroom, null, tint = Accent, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Family Summary", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                if (character.parentsIds.isNotEmpty()) {
                    Text("Parents: ${character.parentsIds.size}", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("No known parents", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
                if (character.spouseId != null) {
                    Spacer(Modifier.height(4.dp))
                    Text("Spouse: Connected", color = LooksPink, style = MaterialTheme.typography.bodySmall)
                }
                if (character.childIds.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("Children: ${character.childIds.size}", color = Gold, style = MaterialTheme.typography.bodySmall)
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text("No children yet", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Social Stats
        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundCard),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Diversity3, null, tint = HappinessYellow, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Social Attributes", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                RelationshipStat("Charisma", character.charisma, HappinessYellow)
                RelationshipStat("Reputation", character.reputation.toDouble(), ReputationTeal)
                RelationshipStat("Notoriety", character.notoriety.toDouble(), Error)
                RelationshipStat("Empathy", character.empathy, KarmaPurple)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Active Relationships List
        Text(
            text = "Active Social Circle (${uiState.relationships.size})",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (uiState.relationships.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Rounded.Diversity1, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Your social circle is empty.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Age up, start school, or take a job to meet friends and colleagues automatically!", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            uiState.relationships.forEach { relationship ->
                val targetChar = uiState.socialCharacters[relationship.targetCharacterId]
                RelationshipCard(
                    relationship = relationship,
                    targetChar = targetChar,
                    currentYear = uiState.currentYear,
                    onInteraction = onInteraction
                )
            }
        }
    }
}

@Composable
private fun RelationshipCard(
    relationship: com.example.lifesim.data.local.entity.RelationshipEntity,
    targetChar: com.example.lifesim.data.local.entity.CharacterEntity?,
    currentYear: Int,
    onInteraction: (String, InteractionType) -> Unit
) {
    if (targetChar == null) return
    val birthYear = (targetChar.dateOfBirth / 31557600000L).toInt()
    val age = (currentYear - birthYear).coerceAtLeast(0)

    val relationColor = when (relationship.relationType) {
        RelationType.PARENT -> Accent
        RelationType.SIBLING -> Info
        RelationType.SPOUSE -> LooksPink
        RelationType.PARTNER -> LooksPink
        RelationType.CHILD -> Gold
        RelationType.FRIEND -> Success
        RelationType.COLLEAGUE -> ReputationTeal
        RelationType.ENEMY -> Error
        else -> TextSecondary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${targetChar.firstName} ${targetChar.lastName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${relationship.relationType.name.toLowerCase().capitalize()} · Age $age · ${targetChar.gender.name.toLowerCase()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = relationColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(relationColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (relationship.relationType) {
                            RelationType.SPOUSE, RelationType.PARTNER -> Icons.Rounded.Favorite
                            RelationType.CHILD -> Icons.Rounded.ChildCare
                            RelationType.PARENT -> Icons.Rounded.FamilyRestroom
                            RelationType.FRIEND -> Icons.Rounded.Diversity1
                            RelationType.COLLEAGUE -> Icons.Rounded.Work
                            else -> Icons.Rounded.Person
                        },
                        contentDescription = null,
                        tint = relationColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress Bars for Affection & Trust
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Affection", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(64.dp))
                LinearProgressIndicator(
                    progress = relationship.affection / 100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = LooksPink,
                    trackColor = BackgroundSurface
                )
                Spacer(Modifier.width(8.dp))
                Text("${relationship.affection}%", style = MaterialTheme.typography.bodySmall, color = LooksPink, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Trust", style = MaterialTheme.typography.bodySmall, color = TextSecondary, modifier = Modifier.width(64.dp))
                LinearProgressIndicator(
                    progress = relationship.trust / 100f,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = ReputationTeal,
                    trackColor = BackgroundSurface
                )
                Spacer(Modifier.width(8.dp))
                Text("${relationship.trust}%", style = MaterialTheme.typography.bodySmall, color = ReputationTeal, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            // Action Buttons
            Text(
                text = "INTERACT",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            
            // Build actions list
            val actions = mutableListOf<@Composable () -> Unit>()
            
            actions.add {
                InteractionChip(label = "Spend Time", icon = Icons.Rounded.Schedule, color = Accent) {
                    onInteraction(relationship.relationshipId, InteractionType.SPEND_TIME)
                }
            }
            actions.add {
                InteractionChip(label = "Chat", icon = Icons.Rounded.Chat, color = Info) {
                    onInteraction(relationship.relationshipId, InteractionType.CHAT)
                }
            }
            actions.add {
                InteractionChip(label = "Gift ($100)", icon = Icons.Rounded.CardGiftcard, color = Gold) {
                    onInteraction(relationship.relationshipId, InteractionType.GIFT)
                }
            }
            
            val isRomantic = relationship.relationType == RelationType.SPOUSE || relationship.relationType == RelationType.PARTNER
            if (!isRomantic && age >= 12) {
                actions.add {
                    InteractionChip(label = "Flirt", icon = Icons.Rounded.FavoriteBorder, color = LooksPink) {
                        onInteraction(relationship.relationshipId, InteractionType.FLIRT)
                    }
                }
                actions.add {
                    InteractionChip(label = "Ask Out", icon = Icons.Rounded.Favorite, color = LooksPink) {
                        onInteraction(relationship.relationshipId, InteractionType.DATE)
                    }
                }
            }
            
            if (relationship.relationType == RelationType.PARTNER) {
                actions.add {
                    InteractionChip(label = "Propose", icon = Icons.Rounded.VolunteerActivism, color = Gold) {
                        onInteraction(relationship.relationshipId, InteractionType.PROPOSE)
                    }
                }
                actions.add {
                    InteractionChip(label = "Break Up", icon = Icons.Rounded.HeartBroken, color = Error) {
                        onInteraction(relationship.relationshipId, InteractionType.BREAK_UP)
                    }
                }
            }

            if (isRomantic && age in 18..45) {
                actions.add {
                    InteractionChip(label = "Have Baby", icon = Icons.Rounded.ChildCare, color = HappinessYellow) {
                        onInteraction(relationship.relationshipId, InteractionType.HAVE_KID)
                    }
                }
            }

            if (relationship.relationType == RelationType.SPOUSE) {
                actions.add {
                    InteractionChip(label = "Divorce", icon = Icons.Rounded.BrokenImage, color = Error) {
                        onInteraction(relationship.relationshipId, InteractionType.DIVORCE)
                    }
                }
            }

            actions.add {
                InteractionChip(label = "Argue", icon = Icons.Rounded.Warning, color = Warning) {
                    onInteraction(relationship.relationshipId, InteractionType.ARGUE)
                }
            }
            actions.add {
                InteractionChip(label = "Betray", icon = Icons.Rounded.Dangerous, color = Error) {
                    onInteraction(relationship.relationshipId, InteractionType.BETRAY)
                }
            }

            // Render chunked grid of actions (2 per row for high density and great fit on all screen sizes)
            val chunks = actions.chunked(2)
            chunks.forEach { rowActions ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowActions.forEach { actionComposable ->
                        Box(Modifier.weight(1f)) {
                            actionComposable()
                        }
                    }
                    if (rowActions.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractionChip(
    label: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = BackgroundSurface,
            contentColor = TextPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun RelationshipStat(label: String, value: Double, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text("${value.toInt()}/100", style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.Bold)
    }
}
