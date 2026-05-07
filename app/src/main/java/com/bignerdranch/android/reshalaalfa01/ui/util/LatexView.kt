package com.bignerdranch.android.reshalaalfa01.ui.util

import android.graphics.Color as AndroidColor
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin

@Composable
fun LatexText(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 16,
    isDisplayMode: Boolean = false,
    showBackground: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = Color.Transparent
) {
    if (latex.isBlank()) return

    val context = LocalContext.current
    
    val processedText = remember(latex, isDisplayMode) {
        // 1. Предварительная очистка текста
        var cleaned = latex
            .replace("\\\\", "\\") // \\frac -> \frac
            .replace("\\$", "$")   // \$ -> $

        // 2. Markwon JLatexMathPlugin использует $$ для инлайна.
        // Одиночный $ не поддерживается.
        if (cleaned.contains("$")) {
            // Заменяем $formula$ на $$formula$$, не трогая уже существующие $$
            cleaned = cleaned.replace(Regex("(?<!\\$)\\$([^$]+)\\$(?!\\$)")) { match ->
                "$$" + match.groupValues[1] + "$$"
            }
        } else if (!isDisplayMode) {
            // Если это не чистая формула и нет знаков $, возвращаем как есть
        } else {
            // Чистая формула в блочном режиме
            cleaned = "$$\n$cleaned\n$$"
        }
        cleaned
    }

    val markwon = remember(fontSize, color) {
        val densityFontSize = fontSize.toFloat() * context.resources.displayMetrics.density
        Markwon.builder(context)
            .usePlugin(MarkwonInlineParserPlugin.create())
            .usePlugin(JLatexMathPlugin.create(densityFontSize) { builder ->
                builder.inlinesEnabled(true)
            })
            .build()
    }

    AndroidView(
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(color.toArgb())
            }
        },
        update = { textView ->
            textView.setTextColor(color.toArgb())
            if (showBackground && backgroundColor != Color.Transparent) {
                textView.setBackgroundColor(backgroundColor.toArgb())
            } else {
                textView.setBackgroundColor(AndroidColor.TRANSPARENT)
            }
            markwon.setMarkdown(textView, processedText)
        },
        modifier = modifier.fillMaxWidth()
    )
}
