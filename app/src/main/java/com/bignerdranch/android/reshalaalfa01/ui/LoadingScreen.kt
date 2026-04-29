package com.bignerdranch.android.reshalaalfa01.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bignerdranch.android.reshalaalfa01.R
import com.bignerdranch.android.reshalaalfa01.ui.theme.ReshalaAlfa01Theme

@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val logoRes = if (isSystemInDarkTheme()) {
            R.drawable.ic_reshala_logo_dark
        } else {
            R.drawable.ic_reshala_logo_light
        }
        
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoadingScreenPreview() {
    ReshalaAlfa01Theme {
        LoadingScreen()
    }
}
