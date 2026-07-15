package com.noamlewkowicz.carchecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.noamlewkowicz.carchecker.ui.screen.CarCheckerRoute
import com.noamlewkowicz.carchecker.ui.theme.CarCheckerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CarCheckerTheme {
                CarCheckerRoute()
            }
        }
    }
}