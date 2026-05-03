package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bignerdranch.android.reshalaalfa01.data.remote.dto.RecognitionTaskData
import com.bignerdranch.android.reshalaalfa01.ui.components.MathKey
import com.bignerdranch.android.reshalaalfa01.ui.components.MathKeyboard
import com.bignerdranch.android.reshalaalfa01.ui.util.LatexText

@Composable
fun RecognitionDialog(
    state: RecognitionState,
    onDismiss: () -> Unit,
    onAccept: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onSuccessFinished: () -> Unit
) {
    if (state is RecognitionState.Idle) return

    var isEditingMode by remember { mutableStateOf(false) }
    
    // Сбрасываем режим редактирования при переходе в другие состояния
    LaunchedEffect(state) {
        if (state is RecognitionState.Processing || state is RecognitionState.Success) {
            isEditingMode = false
        }
    }

    Dialog(
        onDismissRequest = { if (state is RecognitionState.Error || state is RecognitionState.Success) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = state is RecognitionState.Error || state is RecognitionState.Success,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = !isEditingMode
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isEditingMode) 0.dp else 16.dp)
                .then(if (isEditingMode) Modifier.fillMaxHeight(0.95f) else Modifier),
            shape = if (isEditingMode) RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp) else MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "dialog_content"
            ) { currentState ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isEditingMode) 0.dp else 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (isEditingMode) 0.dp else 20.dp)
                ) {
                    when (currentState) {
                        is RecognitionState.Processing -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                                Text("Processing...", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                        is RecognitionState.ReadyForFeedback -> {
                            FeedbackView(
                                data = currentState.taskData,
                                onAccept = onAccept,
                                onEdit = onEdit,
                                isEditingMode = isEditingMode,
                                onEditingModeChange = { isEditingMode = it }
                            )
                        }
                        is RecognitionState.Success -> {
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(1500)
                                onSuccessFinished()
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(64.dp))
                                Text("Solved!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        is RecognitionState.Error -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                                Text("Error", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(currentState.message, textAlign = TextAlign.Center)
                                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Dismiss") }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackView(
    data: RecognitionTaskData,
    onAccept: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    isEditingMode: Boolean,
    onEditingModeChange: (Boolean) -> Unit
) {
    var editedLatex by remember { mutableStateOf(data.originalResult ?: "") }

    if (!isEditingMode) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Is this correct?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    LatexText(latex = data.originalResult ?: "", isDisplayMode = true, showBackground = false)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onEditingModeChange(true) }, 
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", maxLines = 1)
                }
                Button(
                    onClick = { onAccept(data.id) }, 
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Accept", maxLines = 1)
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header for Edit Mode
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Edit Equation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onEditingModeChange(false) }) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Visual Preview Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                LatexText(
                    latex = editedLatex.ifEmpty { " " }, 
                    isDisplayMode = true, 
                    showBackground = false,
                    fontSize = 24
                )
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onEdit(data.id, editedLatex) },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Solve")
                }
            }

            // The Math Keyboard
            MathKeyboard(
                onKeyClick = { key ->
                    when (key) {
                        is MathKey.Symbol -> editedLatex += key.latex
                        is MathKey.Structure -> editedLatex += key.template
                        is MathKey.Clear -> editedLatex = ""
                        is MathKey.Backspace -> if (editedLatex.isNotEmpty()) {
                            editedLatex = editedLatex.dropLast(1)
                        }
                    }
                }
            )
        }
    }
}
