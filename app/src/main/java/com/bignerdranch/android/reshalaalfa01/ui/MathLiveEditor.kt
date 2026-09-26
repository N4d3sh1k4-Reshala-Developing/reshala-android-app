package com.bignerdranch.android.reshalaalfa01.ui

import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun MathLiveEditor(
    latex: String,
    onLatexChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    shouldFocus: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onContentChanged(newLatex: String) {
                            onLatexChanged(newLatex)
                        }
                        @JavascriptInterface
                        fun onReady() {
                            isLoaded = true
                            post {
                                val escaped = latex.replace("\\", "\\\\").replace("'", "\\'")
                                evaluateJavascript("if (typeof setTheme === 'function') setTheme($isDark); if (typeof setLatex === 'function') setLatex('$escaped');") {}
                                if (shouldFocus) {
                                    evaluateJavascript("var mf = document.querySelector('math-field'); if (mf) { mf.focus(); }") {}
                                }
                            }
                        }
                    }, "Android")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            val escaped = latex.replace("\\", "\\\\").replace("'", "\\'")
                            evaluateJavascript("if (typeof setTheme === 'function') setTheme($isDark); if (typeof setLatex === 'function') setLatex('$escaped');") {}
                        }
                    }
                    
                    val htmlContent = try {
                        ctx.assets.open("mathlive.html").bufferedReader().use { it.readText() }
                    } catch (e: Exception) {
                        "<!DOCTYPE html><html><body>Error loading editor</body></html>"
                    }

                    loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
                    webViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (!isLoaded) {
            Box(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        }
    }

    LaunchedEffect(latex) {
        if (isLoaded) {
            val escaped = latex.replace("\\", "\\\\").replace("'", "\\'")
            webViewRef?.evaluateJavascript("if (getLatex() !== '$escaped') setLatex('$escaped')") {}
        }
    }

    LaunchedEffect(isDark) {
        webViewRef?.evaluateJavascript("setTheme($isDark)") {}
    }
}
