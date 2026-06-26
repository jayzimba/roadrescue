package com.jayjaycode.miniproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jayjaycode.miniproject.ui.RoadRescueRoot
import com.jayjaycode.miniproject.ui.theme.RoadRescueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoadRescueTheme {
                RoadRescueRoot()
            }
        }
    }
}
