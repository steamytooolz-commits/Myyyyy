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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.components.StatLine
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

@Composable
fun EducationScreen(uiState: UiState, onAction: (String) -> Unit = {}) {
    val c = uiState.character
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Education", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
        if (c == null) return
        Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Academic Record", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                StatLine("GPA", if (c.gpa > 0) String.format("%.2f", c.gpa) else "N/A")
                StatLine("Enrolled", c.currentEducationId ?: "Not enrolled")
                StatLine("Student Loan", "$${String.format("%,.0f", c.studentLoan)}")
                if (c.degrees.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text("Degrees: ${c.degrees.joinToString(", ")}", color = SmartsBlue, style = MaterialTheme.typography.labelSmall) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Actions", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        if (c.currentEducationId == null) {
            Button(
                onClick = { onAction("education_enroll") },
                colors = ButtonDefaults.buttonColors(containerColor = SmartsBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enroll in School", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Available Schools", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("• Community College — Low tuition, easy entry", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• State University — Balanced cost and prestige", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Ivy League — Top-tier education (hard to enter)", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = BackgroundCard), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Student Activities", style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("• Attend Class — core learning", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Do Homework — reinforce learning", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Research — deep dive into topics", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Join Study Group — collaborative learning", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text("• Take Exam — prove your knowledge", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Perform these through the Activities tab.", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                }
            }
            if (c.gpa >= 2.0) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onAction("education_graduate") },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Graduate!", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
