package com.bignerdranch.android.reshalaalfa01.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class MathKey(val label: String, val latex: String) {
    class Symbol(label: String, latex: String = label) : MathKey(label, latex)
    class Structure(label: String, val template: String) : MathKey(label, template)
    object Backspace : MathKey("DEL", "")
    object Clear : MathKey("AC", "")
    object Left : MathKey("←", "")
    object Right : MathKey("→", "")
}

@Composable
fun MathKeyboard(
    onKeyClick: (MathKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val keys = listOf(
        MathKey.Symbol("x"), MathKey.Symbol("y"), MathKey.Symbol("z"), MathKey.Symbol("="),
        MathKey.Symbol("1"), MathKey.Symbol("2"), MathKey.Symbol("3"), MathKey.Symbol("+"),
        MathKey.Symbol("4"), MathKey.Symbol("5"), MathKey.Symbol("6"), MathKey.Symbol("-"),
        MathKey.Symbol("7"), MathKey.Symbol("8"), MathKey.Symbol("9"), MathKey.Symbol("*"),
        MathKey.Symbol("0"), MathKey.Symbol("."), MathKey.Symbol("("), MathKey.Symbol(")"),
        MathKey.Symbol("/"),
        MathKey.Symbol("^2"),
        MathKey.Symbol("^"),
        MathKey.Symbol("√"),
        MathKey.Left,
        MathKey.Right,
        MathKey.Clear,
        MathKey.Backspace
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(keys) { key ->
                    KeyButton(key = key, onClick = { onKeyClick(key) })
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    key: MathKey,
    onClick: () -> Unit
) {
    val containerColor = when (key) {
        is MathKey.Clear -> MaterialTheme.colorScheme.errorContainer
        is MathKey.Backspace -> MaterialTheme.colorScheme.secondaryContainer
        is MathKey.Structure -> MaterialTheme.colorScheme.primaryContainer
        is MathKey.Left, is MathKey.Right -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when (key) {
        is MathKey.Clear -> MaterialTheme.colorScheme.onErrorContainer
        is MathKey.Structure -> MaterialTheme.colorScheme.onPrimaryContainer
        is MathKey.Left, is MathKey.Right -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (key is MathKey.Backspace) {
            Icon(Icons.Default.Backspace, contentDescription = "Backspace", tint = contentColor)
        } else {
            Text(
                text = key.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}
