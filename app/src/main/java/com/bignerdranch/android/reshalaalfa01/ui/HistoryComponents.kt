package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaAlfa01Theme
import com.bignerdranch.android.reshalaalfa01.ui.util.LatexText
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HistoryItemCard(item: RecognitionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDateTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                StatusBadge(item.status)
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (!item.originalResult.isNullOrBlank()) {
                Text(
                    text = "Original:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                LatexText(
                    latex = item.originalResult,
                    isDisplayMode = true,
                    showBackground = false, // Минималистичный вид без фона
                    modifier = Modifier.heightIn(min = 30.dp, max = 80.dp)
                )
            }
            
            if (!item.editedResult.isNullOrBlank() && item.editedResult != item.originalResult) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Edited:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                LatexText(
                    latex = item.editedResult,
                    isDisplayMode = true,
                    showBackground = false,
                    modifier = Modifier.heightIn(min = 30.dp, max = 80.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "RECOGNIZING" -> "Recognizing" to Color(0xFF2196F3)
        "READY_FOR_FEEDBACK" -> "Feedback" to Color(0xFFFF9800)
        "COMPLETED_AUTO" -> "Completed" to Color(0xFF4CAF50)
        "COMPLETED_EDITED" -> "Completed" to Color(0xFF4CAF50)
        "SOLVING_EQUATION" -> "Solving" to Color(0xFF9C27B0)
        "SOLUTION_READY" -> "Ready" to Color(0xFF4CAF50)
        "FAILED" -> "Failed" to Color(0xFFF44336)
        else -> status to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

fun formatDateTime(isoString: String): String {
    return try {
        val dt = ZonedDateTime.parse(isoString + "Z")
        dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
    } catch (e: Exception) {
        isoString.replace("T", " ").take(16)
    }
}

@Preview(showBackground = true)
@Composable
fun StatusBadgePreview() {
    ReshalaAlfa01Theme {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            StatusBadge("RECOGNIZING")
            StatusBadge("READY_FOR_FEEDBACK")
            StatusBadge("COMPLETED_AUTO")
            StatusBadge("FAILED")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryItemCardPreview() {
    ReshalaAlfa01Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            HistoryItemCard(
                item = RecognitionEntity(
                    id = "1",
                    originalResult = "x^2 + 5x + 6 = 0",
                    editedResult = null,
                    status = "READY_FOR_FEEDBACK",
                    createdAt = "2024-05-25T14:00:00",
                    solutionResult = null
                )
            )
        }
    }
}
