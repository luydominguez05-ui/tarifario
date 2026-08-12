package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.navigation.MainNavHost
import com.example.ui.theme.NetTarifaTheme
import com.example.ui.viewmodel.NetTarifaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetTarifaTheme {
                val viewModel: NetTarifaViewModel = viewModel()
                MainNavHost(viewModel = viewModel)
            }
        }
    }
}
