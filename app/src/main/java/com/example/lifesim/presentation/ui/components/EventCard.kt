// =========================================
// File: presentation/ui/components/EventCard.kt
// =========================================
package com.example.lifesim.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*

@Composable
fun EventCard(
    title: String,
    description: String,
    category: String,
    isAI: Boolean,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(500))
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = BackgroundCard),
            shape = RoundedCornerShape(20.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Surface(color = when (category) { "health" -> HealthGreen; "career" -> SmartsBlue; "relationship" -> LooksPink; "financial" -> Gold; "legal" -> StressOrange; "social" -> HappinessYellow; else -> Accent }.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)) {
                    Text(category.uppercase(), style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                TypewriterText(text = description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                if (isAI) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Psychology, null, tint = KarmaPurple, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AI Generated", style = MaterialTheme.typography.labelSmall, color = KarmaPurple)
                    }
                }
            }
        }
    }
}

@Composable
fun TypewriterText(
    text: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    color: androidx.compose.ui.graphics.Color = TextSecondary,
    modifier: Modifier = Modifier
) {
    var visibleChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(text) {
        visibleChars = 0
        text.forEachIndexed { index, _ ->
            kotlinx.coroutines.delay(15)
            visibleChars = index + 1
        }
    }

    Text(
        text = text.take(visibleChars),
        style = style,
        color = color,
        modifier = modifier
    )
}
