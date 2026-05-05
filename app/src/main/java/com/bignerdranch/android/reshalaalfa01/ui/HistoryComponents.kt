package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
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
fun HistoryItemCard(
    item: RecognitionEntity,
    onActionClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large, // Более скругленные углы (modern)
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Убираем стандартную тень для \"плоского\" вида
        border = androidx.compose.foundation.BorderStroke(
            width = if (isDark) 1.dp else 0.5.dp,
            color = if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Увеличили внутренние отступы
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatToTime(item.createdAt),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(item.status)
                    if (item.status != "READY_FOR_FEEDBACK") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "View details",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val displayLatex = if (!item.editedResult.isNullOrBlank()) item.editedResult else item.originalResult
            
            if (!displayLatex.isNullOrBlank()) {
                val isError = item.status == "FAILED" || displayLatex.startsWith("Error:")
                val isEdited = !item.editedResult.isNullOrBlank()
                
                Surface(
                    color = when {
                        isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                        isEdited -> MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    border = if (isEdited && !isError) 
                        androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        else null
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        LatexText(
                            latex = displayLatex,
                            isDisplayMode = true,
                            showBackground = false,
                            modifier = Modifier.heightIn(min = 40.dp, max = 100.dp)
                        )
                    }
                }
            }

            if (item.status == "READY_FOR_FEEDBACK" && onActionClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Complete Recognition")
                }
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
        color = color.copy(alpha = 0.12f),
        shape = CircleShape, // Полностью круглые бейджи - современный стандарт
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
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

fun formatToDate(isoString: String): String {
    return try {
        val dt = ZonedDateTime.parse(isoString + "Z")
        dt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    } catch (e: Exception) {
        isoString.take(10)
    }
}

fun formatToTime(isoString: String): String {
    return try {
        val dt = ZonedDateTime.parse(isoString + "Z")
        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        isoString.substringAfter("T").take(5)
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
