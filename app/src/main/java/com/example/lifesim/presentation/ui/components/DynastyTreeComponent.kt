// =========================================
// File: presentation/ui/components/DynastyTreeComponent.kt
// =========================================
package com.example.lifesim.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*

data class FamilyTreeNode(
    val id: String,
    val name: String,
    val age: Int,
    val isAlive: Boolean,
    val parentIds: List<String> = emptyList(),
    val spouseId: String? = null
)

@Composable
fun DynastyTreeComponent(
    nodes: List<FamilyTreeNode>,
    modifier: Modifier = Modifier
) {
    if (nodes.isEmpty()) {
        Text("No family tree data available.", style = MaterialTheme.typography.bodySmall,
            color = TextSecondary, modifier = modifier.padding(16.dp))
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        nodes.forEach { node ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                val statusColor = if (node.isAlive) Success else TextTertiary
                Text("●", color = statusColor, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("${node.name} (${node.age})", style = MaterialTheme.typography.bodySmall,
                    color = if (node.isAlive) TextPrimary else TextTertiary)
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp)) {
        val strokeStyle = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        drawLine(color = Divider, start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2), strokeWidth = strokeStyle.width)
    }
}
