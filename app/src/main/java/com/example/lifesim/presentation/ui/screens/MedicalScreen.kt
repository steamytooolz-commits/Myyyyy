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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.components.StatLine
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Health & Medical", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return

        // Health Overview
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Vitals", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatLine("Health", "${c.health.toInt()}/100")
                StatLine("Sanity", "${c.sanity.toInt()}/100")
                StatLine("Stress", "${c.stress.toInt()}/100")
                StatLine("Insurance", insuranceLabel(c.insurancePlan))
                if (c.isInHospital) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalHospital, null, tint = Error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Hospitalized", color = Error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Active Conditions
        val conditions = parseConditionsInline(c.medicalConditions)
        if (conditions.isNotEmpty()) {
            Text("Active Conditions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            conditions.forEach { (id, name, severity, remaining) ->
                val typeColor = when {
                    severity >= 7 -> Error; severity >= 4 -> StressOrange; else -> HealthGreen
                }
                Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MedicalServices, null, tint = typeColor, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("Severity: $severity/10 · Recovery: ${remaining}yr", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        TextButton(onClick = { onAction("medical_treat_$id") }) {
                            Text("Treat", color = HealthGreen)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Insurance Plans
        Text("Insurance Plans", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        val plans = listOf(
            "NONE" to "Uninsured" to 0, "BASIC" to "Basic Plan" to 200, "STANDARD" to "Standard Plan" to 500,
            "PREMIUM" to "Premium Plan" to 1200, "GOLD" to "Gold Plan" to 2500
        )
        plans.forEach { (keyLabel, cost) ->
            val (key, label) = keyLabel
            val active = c.insurancePlan == key
            Card(
                onClick = { if (!active) onAction("medical_insurance_$key") },
                colors = CardDefaults.cardColors(containerColor = if (active) Accent.copy(alpha = 0.15f) else BackgroundCard),
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (active) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked, null,
                        tint = if (active) Accent else TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.titleSmall, color = if (active) Accent else TextPrimary, fontWeight = FontWeight.Bold)
                        Text("$${cost}/mo", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                    if (active) Text("Active", style = MaterialTheme.typography.labelSmall, color = HealthGreen)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick Actions
        Text("Quick Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction("medical_checkup") }, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.MedicalServices, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Checkup ($200)")
            }
        }

        Spacer(Modifier.height(12.dp))
        Text("Tip: Regular checkups catch conditions early. Higher insurance = lower treatment costs.", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
    }
}

private data class ConditionDisplay(val id: String, val name: String, val severity: Int, val remainingYears: Int)

private val conditionNames = mapOf(
    "common_cold" to "Common Cold", "flu" to "Influenza", "pneumonia" to "Pneumonia",
    "strep_throat" to "Strep Throat", "appendicitis" to "Appendicitis",
    "cancer_early" to "Early Stage Cancer", "diabetes_t2" to "Type 2 Diabetes",
    "heart_disease" to "Heart Disease", "asthma" to "Asthma", "arthritis" to "Arthritis",
    "fracture_arm" to "Arm Fracture", "fracture_leg" to "Leg Fracture",
    "concussion" to "Concussion", "internal_bleeding" to "Internal Bleeding",
    "food_poisoning" to "Food Poisoning", "allergic_reaction" to "Allergic Reaction",
    "depression" to "Depression", "anxiety_disorder" to "Anxiety Disorder",
    "kidney_stones" to "Kidney Stones", "skin_infection" to "Skin Infection",
    "migraine_chronic" to "Chronic Migraines", "hernia" to "Hernia",
    "hepatitis" to "Hepatitis", "stroke" to "Stroke"
)

private val conditionSeverity = mapOf(
    "common_cold" to 2, "flu" to 4, "pneumonia" to 7, "strep_throat" to 3,
    "appendicitis" to 8, "cancer_early" to 9, "diabetes_t2" to 5,
    "heart_disease" to 7, "asthma" to 4, "arthritis" to 5,
    "fracture_arm" to 4, "fracture_leg" to 6, "concussion" to 5,
    "internal_bleeding" to 9, "food_poisoning" to 3, "allergic_reaction" to 4,
    "depression" to 6, "anxiety_disorder" to 5, "kidney_stones" to 6,
    "skin_infection" to 3, "migraine_chronic" to 4, "hernia" to 5,
    "hepatitis" to 6, "stroke" to 9
)

private fun insuranceLabel(plan: String): String = when (plan) {
    "NONE" -> "Uninsured (0% coverage)"
    "BASIC" -> "Basic Plan (40% coverage)"
    "STANDARD" -> "Standard Plan (65% coverage)"
    "PREMIUM" -> "Premium Plan (85% coverage)"
    "GOLD" -> "Gold Plan (95% coverage)"
    else -> "Uninsured"
}

private fun parseConditionsInline(data: String): List<ConditionDisplay> {
    if (data.isBlank()) return emptyList()
    return data.split("|").mapNotNull { entry ->
        val parts = entry.split(":")
        if (parts.size < 2) return@mapNotNull null
        val id = parts[0]
        val remaining = parts[1].toIntOrNull() ?: 1
        ConditionDisplay(id, conditionNames[id] ?: id, conditionSeverity[id] ?: 5, remaining)
    }
}
