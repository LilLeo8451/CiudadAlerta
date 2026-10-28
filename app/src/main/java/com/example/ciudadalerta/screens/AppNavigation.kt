package com.example.ciudadalerta.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ciudadalerta.R
import com.example.ciudadalerta.model.Rol
import com.example.ciudadalerta.model.Usuario
import com.example.ciudadalerta.viewmodel.AuthViewModel

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val FORMULARIO = "formulario"
    const val LISTA = "lista"
    const val ADMIN_LISTA = "admin_lista"
    const val PERFIL = "perfil"
    const val DETALLE = "detalle/{id}"
    fun detalle(id: Int) = "detalle/$id"
}

private data class ItemBarra(
    val ruta: String,
    val icono: ImageVector,
    @StringRes val etiqueta: Int
)

private val itemsCiudadano = listOf(
    ItemBarra(Rutas.FORMULARIO, Icons.Filled.Add, R.string.nav_nuevo),
    ItemBarra(Rutas.LISTA, Icons.AutoMirrored.Filled.List, R.string.nav_mis_reportes),
    ItemBarra(Rutas.PERFIL, Icons.Filled.Person, R.string.nav_perfil)
)

private val itemsAdmin = listOf(
    ItemBarra(Rutas.ADMIN_LISTA, Icons.AutoMirrored.Filled.List, R.string.nav_reportes),
    ItemBarra(Rutas.PERFIL, Icons.Filled.Person, R.string.nav_perfil)
)

private val rutasConBarra = setOf(Rutas.FORMULARIO, Rutas.LISTA, Rutas.ADMIN_LISTA, Rutas.PERFIL)

private fun rutaInicioPara(usuario: Usuario): String =
    if (usuario.rol == Rol.ADMIN) Rutas.ADMIN_LISTA else Rutas.FORMULARIO

@Composable
fun AppNavigation(authViewModel: AuthViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val usuario by authViewModel.usuarioActual.collectAsState()
    val esAdmin = usuario?.rol == Rol.ADMIN
    val rutaInicio = if (esAdmin) Rutas.ADMIN_LISTA else Rutas.FORMULARIO

    // Al iniciar sesión / registrarse: ir a la pantalla principal del rol y sacar login del historial
    val irAInicio: (Usuario) -> Unit = { u ->
        navController.navigate(rutaInicioPara(u)) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (rutaActual in rutasConBarra) {
                NavigationBar {
                    val items = if (esAdmin) itemsAdmin else itemsCiudadano
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icono, contentDescription = null) },
                            label = { Text(stringResource(item.etiqueta)) },
                            selected = rutaActual == item.ruta,
                            onClick = {
                                navController.navigate(item.ruta) {
                                    popUpTo(rutaInicio) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Rutas.LOGIN,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Rutas.LOGIN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginExitoso = irAInicio,
                    onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    authViewModel = authViewModel,
                    onRegistroExitoso = irAInicio,
                    onVolver = { navController.popBackStack() }
                )
            }

            // --- Ciudadano ---
            composable(Rutas.FORMULARIO) {
                FormularioScreen(usuarioId = usuario?.id ?: 0)
            }
            composable(Rutas.LISTA) {
                ListaScreen(
                    usuarioId = usuario?.id ?: 0,
                    onReporteClick = { id -> navController.navigate(Rutas.detalle(id)) }
                )
            }

            // --- Administrador / trabajador ---
            composable(Rutas.ADMIN_LISTA) {
                AdminListaScreen(
                    onReporteClick = { id -> navController.navigate(Rutas.detalle(id)) }
                )
            }

            // --- Compartidas ---
            composable(
                route = Rutas.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                DetalleReporteScreen(
                    reporteId = id,
                    esAdmin = esAdmin,
                    onVolver = { navController.popBackStack() }
                )
            }
            composable(Rutas.PERFIL) {
                PerfilScreen(
                    usuario = usuario,
                    onCerrarSesion = {
                        authViewModel.logout()
                        navController.navigate(Rutas.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
