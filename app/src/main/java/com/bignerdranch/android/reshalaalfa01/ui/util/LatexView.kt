package com.bignerdranch.android.reshalaalfa01.ui.util

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LatexText(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 16,
    isDisplayMode: Boolean = false,
    showBackground: Boolean = true,
    color: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = if (isDisplayMode && showBackground) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
) {
    if (latex.isBlank()) return

    val textColorHex = String.format("#%06X", 0xFFFFFF and color.toArgb())
    
    // Используем rgba для корректной поддержки прозрачности
    val backgroundColorArgb = backgroundColor.toArgb()
    val r = (backgroundColorArgb shr 16) and 0xFF
    val g = (backgroundColorArgb shr 8) and 0xFF
    val b = backgroundColorArgb and 0xFF
    val a = backgroundColor.alpha
    val bgColorStyle = "rgba($r, $g, $b, $a)"

    // Предварительная обработка: исправляем степени и экранируем слэши для JS
    val processedLatex = latex
        .replace("**", "^")
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", " ")

    val htmlData = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.css">
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/katex.min.js"></script>
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.8/dist/contrib/auto-render.min.js"></script>
            <style>
                html, body { 
                    margin: 0; 
                    padding: 0; 
                    background-color: transparent; 
                    width: 100%;
                }
                body { 
                    padding: ${if (isDisplayMode && showBackground) "12px" else "4px"} 8px; 
                    background-color: $bgColorStyle;
                    color: $textColorHex; 
                    font-size: ${fontSize}px;
                    border-radius: 8px;
                    box-sizing: border-box;
                }
                #math { 
                    width: 100%; 
                    text-align: ${if (isDisplayMode) "center" else "left"};
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                }
                .katex-display { 
                    margin: 0; 
                    overflow-x: auto;
                    overflow-y: hidden;
                }
                ::-webkit-scrollbar { display: none; }
                .katex { 
                    white-space: normal; 
                }
            </style>
        </head>
        <body>
            <div id="math"></div>
            <script>
                document.addEventListener("DOMContentLoaded", function() {
                    var el = document.getElementById("math");
                    var rawTex = '$processedLatex';
                    
                    try {
                        if (${isDisplayMode}) {
                            katex.render(rawTex, el, { 
                                displayMode: true, 
                                throwOnError: false,
                                trust: true 
                            });
                        } else {
                            el.innerHTML = rawTex;
                            renderMathInElement(el, {
                                delimiters: [
                                    {left: "\\(", right: "\\)", display: false},
                                    {left: "$$", right: "$$", display: true},
                                    {left: "$", right: "$", display: false}
                                ],
                                throwOnError: false
                            });
                        }
                    } catch (e) {
                        el.textContent = rawTex;
                    }
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                settings.javaScriptEnabled = true
                setBackgroundColor(0) 
                webViewClient = WebViewClient()
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://localhost", htmlData, "text/html", "UTF-8", null)
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (isDisplayMode && showBackground) 60.dp else 24.dp, max = 500.dp)
    )
}
