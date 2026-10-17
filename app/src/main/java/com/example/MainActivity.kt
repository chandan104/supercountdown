package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.MainAppScreen
import com.example.ui.SuperCountdownViewModel
import com.example.ui.theme.SuperCountdownTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SuperCountdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperCountdownTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
