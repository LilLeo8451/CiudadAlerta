package com.example.ciudadalerta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.ciudadalerta.screens.AppNavigation
import com.example.ciudadalerta.ui.theme.CiudadAlertaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CiudadAlertaTheme {
                AppNavigation()
            }
        }
    }
}
