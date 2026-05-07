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
                                evaluateJavascript("setTheme($isDark); setLatex('$escaped');") {}
                                if (shouldFocus) {
                                    evaluateJavascript("document.querySelector('math-field')?.focus();") {}
                                }
                            }
                        }
                    }, "Android")

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            val escaped = latex.replace("\\", "\\\\").replace("'", "\\'")
                            evaluateJavascript("setTheme($isDark); setLatex('$escaped');") {}
                        }
                    }
                    
                    loadUrl("file:///android_asset/mathlive.html")
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
