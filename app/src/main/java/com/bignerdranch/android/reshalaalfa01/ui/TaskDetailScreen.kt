package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.SolutionStep
import com.bignerdranch.android.reshalaalfa01.ui.util.LatexText
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: RecognitionEntity?,
    onBackClick: () -> Unit
) {
    val json = remember { Json { ignoreUnknownKeys = true } }
    val solutionSteps = remember(task?.solutionResult) {
        try {
            task?.solutionResult?.let {
                json.decodeFromString<List<SolutionStep>>(it)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Task not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDateTime(task.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        StatusBadge(task.status)
                    }
                }

                if (!task.originalResult.isNullOrBlank()) {
                    item {
                        Text(text = "Original Equation:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        LatexText(
                            latex = task.originalResult,
                            isDisplayMode = true,
                            showBackground = false
                        )
                    }
                }

                if (!task.editedResult.isNullOrBlank() && task.editedResult != task.originalResult) {
                    item {
                        Text(text = "Edited Equation:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        LatexText(
                            latex = task.editedResult,
                            isDisplayMode = true,
                            showBackground = false
                        )
                    }
                }

                if (solutionSteps.isNotEmpty()) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(text = "Step-by-step Solution:", style = MaterialTheme.typography.titleLarge)
                    }

                    items(solutionSteps) { step ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = step.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Используем LatexText для объяснения, чтобы отрендерить инлайновые формулы типа \( x**2 \)
                                LatexText(latex = step.explanation, fontSize = 14)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Основная формула шага по центру на выделенном фоне
                                LatexText(
                                    latex = step.latex,
                                    isDisplayMode = true,
                                    showBackground = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
