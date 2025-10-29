package com.example.lab10_maxi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.lab10_maxi.ui.theme.Lab10MaxiTheme
import com.example.lab10_maxi.view.SeriesApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab10MaxiTheme {
                SeriesApp()   // ✅ carga directamente tu app
            }
        }
    }
}
