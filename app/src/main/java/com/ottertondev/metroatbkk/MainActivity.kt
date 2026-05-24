package com.ottertondev.metroatbkk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ottertondev.metroatbkk.ui.MetroExplorerRoute
import com.ottertondev.metroatbkk.ui.theme.MetroAtBKKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MetroAtBKKTheme {
                MetroExplorerRoute()
            }
        }
    }
}
