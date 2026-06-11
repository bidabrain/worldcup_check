package com.worldcup2026

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.worldcup2026.ui.AppNavigation
import com.worldcup2026.ui.theme.WorldCup2026Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as WorldCupApp).repository

        setContent {
            WorldCup2026Theme {
                AppNavigation(repository = repository)
            }
        }
    }
}
