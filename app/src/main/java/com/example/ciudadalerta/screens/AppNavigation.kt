package com.example.ciudadalerta.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val configuracion = LocalConfiguration.current
    val esPantallaGrande = configuracion.screenWidthDp >= 600

    Scaffold(
        bottomBar = {
            if (!esPantallaGrande) {
                /* TODO: Agregar BottomNavigationBar */
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            if (esPantallaGrande) {
                /* TODO: Agregar NavigationRail */
            }

            NavHost(
                navController = navController,
                startDestination = "formulario", // Pantalla de inicio
                modifier = Modifier.weight(1f)
            ) {
                composable("formulario") { FormularioScreen() }
                composable("lista") { ListaScreen() }
            }
        }
    }
}