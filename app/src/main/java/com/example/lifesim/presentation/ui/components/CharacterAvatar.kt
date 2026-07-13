// =========================================
// File: presentation/ui/components/CharacterAvatar.kt
// =========================================
package com.example.lifesim.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Face2
import androidx.compose.material.icons.rounded.Face3
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lifesim.data.local.entity.Gender
import com.example.lifesim.presentation.ui.theme.*

@Composable
fun CharacterAvatar(
    age: Int,
    gender: Gender,
    looks: Double,
    modifier: Modifier = Modifier,
    size: Dp = 60.dp
) {
    val avatarColor = getLooksColor(looks)
    val isAdult = age >= 18
    val isElderly = age >= 60

    val infiniteTransition = rememberInfiniteTransition(label = "avatarGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "glow"
    )

    val icon: ImageVector = when {
        isElderly -> Icons.Rounded.Person
        gender == Gender.FEMALE -> Icons.Rounded.Face
        else -> Icons.Rounded.Face2
    }

    Box(
        modifier = modifier.size(size).clip(CircleShape)
            .background(avatarColor.copy(alpha = glowAlpha)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isAdult) icon else Icons.Rounded.Face3,
            contentDescription = "Character avatar",
            tint = TextPrimary,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun getLooksColor(looks: Double): Color = when {
    looks > 70 -> LooksPink
    looks > 40 -> HappinessYellow
    else -> StressOrange
}
