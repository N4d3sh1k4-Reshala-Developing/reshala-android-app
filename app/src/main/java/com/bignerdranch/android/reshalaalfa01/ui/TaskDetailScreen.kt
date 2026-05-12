package com.bignerdranch.android.reshalaalfa01.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.bignerdranch.android.reshalaalfa01.R
import com.bignerdranch.android.reshalaalfa01.BuildConfig
import com.bignerdranch.android.reshalaalfa01.data.local.RecognitionEntity
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.SolutionStep
import com.bignerdranch.android.reshalaalfa01.ui.util.LatexText
import kotlinx.serialization.json.Json
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: RecognitionEntity?,
    onFeedbackClick: (RecognitionEntity) -> Unit,
    onDeleteClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val json = remember { Json { ignoreUnknownKeys = true } }
    val solutionSteps = remember(task?.solutionResult) {
        try {
            task?.solutionResult?.let {
                json.decodeFromString<List<SolutionStep>>(it)
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { 
                    val solutionTitle = stringResource(R.string.solution_title)
                    val alfa = stringResource(R.string.alfa)
                    Text(
                        text = buildAnnotatedString {
                            append(solutionTitle)
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    baselineShift = BaselineShift.Superscript,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(alfa)
                            }
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (task != null) {
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text(stringResource(R.string.delete_solution_title)) },
                                text = { Text(stringResource(R.string.delete_solution_msg)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showDeleteDialog = false
                                            onDeleteClick(task.id)
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text(stringResource(R.string.delete))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text(stringResource(R.string.cancel))
                                    }
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (task == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.task_not_found), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formatToDate(task.createdAt),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = formatToTime(task.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                            StatusBadge(task.status)
                        }
                    }
                }

                if (task.status == "READY_FOR_FEEDBACK") {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    stringResource(R.string.recognition_ready),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    stringResource(R.string.recognition_ready_msg),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { onFeedbackClick(task) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.give_feedback))
                                }
                            }
                        }
                    }
                }

                if (!task.originalResult.isNullOrBlank()) {
                    val isError = task.status == "FAILED" || task.originalResult.startsWith("Error:")
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                               else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            shape = MaterialTheme.shapes.large,
                            border = if (isError) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (!isError) {
                                    Text(
                                        text = stringResource(R.string.original_equation), 
                                        fontWeight = FontWeight.Bold, 
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                LatexText(
                                    latex = task.originalResult,
                                    isDisplayMode = true,
                                    showBackground = false
                                )
                            }
                        }
                    }
                }

                if (!task.editedResult.isNullOrBlank() && task.editedResult != task.originalResult) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                            shape = MaterialTheme.shapes.large,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.edited_equation), 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.primary, 
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LatexText(
                                    latex = task.editedResult,
                                    isDisplayMode = true,
                                    showBackground = false
                                )
                            }
                        }
                    }
                }

                if (solutionSteps.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.step_by_step),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(solutionSteps) { step ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = step.title, 
                                    fontWeight = FontWeight.Bold, 
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                LatexText(latex = step.explanation, fontSize = 15)
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Box(modifier = Modifier.padding(8.dp)) {
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

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = "mailto:${BuildConfig.SUPPORT_EMAIL}".toUri()
                                        putExtra(Intent.EXTRA_SUBJECT, "Feedback on Solution: ${task.id}")
                                        putExtra(Intent.EXTRA_TEXT, "Hi Team,\n\nI have a question regarding the solution for the following equation:\n\nID: ${task.id}\nEquation: ${task.editedResult ?: task.originalResult}\n\nMy feedback:\n")
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Send feedback"))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Don't like the solution?")
                            }
                        }
                        Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom nav
                    }
                }
            }
        }
    }
}