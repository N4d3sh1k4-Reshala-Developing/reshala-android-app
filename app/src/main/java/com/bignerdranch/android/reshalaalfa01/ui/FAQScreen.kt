package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.reshalaalfa01.R
import com.bignerdranch.android.reshalaalfa01.ui.util.LatexText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.faq_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.faq_description),
                style = MaterialTheme.typography.bodyLarge
            )

            FAQSection(
                title = stringResource(R.string.faq_expressions),
                examples = listOf(
                    "(x + 1)^2 - x^2",
                    "\\frac{x^2 - 1}{x - 1}",
                    "\\sin^2(x) + \\cos^2(x)"
                )
            )
            FAQSection(
                title = stringResource(R.string.faq_linear),
                examples = listOf(
                    "2x + 10 = 0",
                    "3x - 5 = x + 3",
                    "x + y = 10"
                )
            )
            FAQSection(
                title = stringResource(R.string.faq_quadratic),
                examples = listOf(
                    "x^2 - 5x + 6 = 0",
                    "x^2 - 4x + 4 = 0",
                    "x^2 + x + 5 = 0",
                    "x^2 - 9 = 0"
                )
            )
            FAQSection(
                title = stringResource(R.string.faq_special),
                examples = listOf(
                    "(x - 2)(x + 2) = 0",
                    "(x + 1)^2 = (x - 1)^2",
                    "2x + \\sqrt{16} = 10",
                    "\\frac{1}{\\sqrt{3}}"
                )
            )
            FAQSection(
                title = stringResource(R.string.faq_polynomials),
                examples = listOf(
                    "x^3 - 8 = 0",
                    "x^3 - x = 0"
                )
            )
            FAQSection(
                title = stringResource(R.string.faq_trigonometry),
                examples = listOf(
                    "\\sin(x) - \\frac{1}{2} = 0",
                    "\\cos(x) = 1"
                )
            )
            FAQSection(
                title = stringResource(R.string.faq_exponential),
                examples = listOf(
                    "2^x = 1024",
                    "3^{x+1} = 27",
                    "2^x = 10"
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FAQSection(title: String, examples: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        examples.forEach { latex ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    LatexText(latex = latex, isDisplayMode = true, showBackground = false)
                }
            }
        }
    }
}
