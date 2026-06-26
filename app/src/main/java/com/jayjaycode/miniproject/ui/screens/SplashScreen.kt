package com.jayjaycode.miniproject.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.jayjaycode.miniproject.R

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.splash_screen),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}
