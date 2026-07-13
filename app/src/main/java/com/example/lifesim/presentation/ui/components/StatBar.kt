package com.example.lifesim.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*

@Composable
fun StatBar(
    label: String,
    value: Double,
    maxValue: Double = 100.0,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    color: Color = getStatColor(value, maxValue),
    showLabel: Boolean = true,
    inverted: Boolean = false
) {
    val displayValue = if (inverted) maxValue - value else value
    val animatedProgress by animateFloatAsState(
        targetValue = (displayValue / maxValue).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500)
    )
    Column(modifier = modifier.padding(vertical = 3.dp)) {
        if (showLabel) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Text(
                    text = "${value.toInt()}/${maxValue.toInt()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(height / 2)),
            color = color,
            trackColor = BackgroundElevated,
        )
    }
}

fun getStatColor(value: Double, maxValue: Double): Color {
    val ratio = value / maxValue
    return when {
        ratio > 0.7 -> Success
        ratio > 0.4 -> Warning
        else -> Error
    }
}
