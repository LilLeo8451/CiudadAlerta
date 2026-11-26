package com.example.ciudadalerta.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.example.ciudadalerta.viewmodel.NotificacionViewModel

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val FORMULARIO = "formulario"
    const val LISTA = "lista"
    const val CIUDAD = "ciudad"
    const val NOTIFICACIONES = "notificaciones"
    const val ADMIN_LISTA = "admin_lista"
    const val PERFIL = "perfil"
    const val DETALLE = "detalle/{id}"
    fun detalle(id: Int) = "detalle/$id"
    const val EDITAR = "editar/{id}"
    fun editar(id: Int) = "editar/$id"
}

private data class ItemBarra(
    val ruta: String,
    val icono: ImageVector,
    @param:StringRes val etiqueta: Int
)

private val itemsCiudadano = listOf(
    ItemBarra(Rutas.FORMULARIO, Icons.Filled.Add, R.string.nav_nuevo),
    ItemBarra(Rutas.LISTA, Icons.AutoMirrored.Filled.List, R.string.nav_mis_reportes),
    ItemBarra(Rutas.CIUDAD, Icons.Filled.LocationCity, R.string.nav_ciudad),
    ItemBarra(Rutas.NOTIFICACIONES, Icons.Filled.Notifications, R.string.nav_avisos),
    ItemBarra(Rutas.PERFIL, Icons.Filled.Person, R.string.nav_perfil)
)

private val itemsAdmin = listOf(
    ItemBarra(Rutas.ADMIN_LISTA, Icons.AutoMirrored.Filled.List, R.string.nav_reportes),
    ItemBarra(Rutas.CIUDAD, Icons.Filled.LocationCity, R.string.nav_ciudad),
    ItemBarra(Rutas.PERFIL, Icons.Filled.Person, R.string.nav_perfil)
)

private val rutasConBarra = setOf(
    Rutas.FORMULARIO, Rutas.LISTA, Rutas.CIUDAD, Rutas.NOTIFICACIONES, Rutas.ADMIN_LISTA, Rutas.PERFIL
)

private fun rutaInicioPara(usuario: Usuario): String =
    if (usuario.rol == Rol.ADMIN) Rutas.ADMIN_LISTA else Rutas.FORMULARIO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    notificacionViewModel: NotificacionViewModel = viewModel(factory = NotificacionViewModel.Factory)
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = backStackEntry?.destination?.route

    val usuario by authViewModel.usuarioActual.collectAsState()
    val esAdmin = usuario?.rol == Rol.ADMIN
    val rutaInicio = if (esAdmin) Rutas.ADMIN_LISTA else Rutas.FORMULARIO
    val noLeidas by notificacionViewModel.noLeidas.collectAsState()

    // Los avisos son solo para ciudadanos: se le dice al ViewModel de quién son
    LaunchedEffect(usuario?.id, esAdmin) {
        notificacionViewModel.setUsuario(if (esAdmin) null else usuario?.id)
    }

    // Al iniciar sesión / registrarse: ir a la pantalla principal del rol y sacar login del historial
    val irAInicio: (Usuario) -> Unit = { u ->
        navController.navigate(rutaInicioPara(u)) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
    }

    // Pantalla pequeña: barra inferior. Pantalla grande: menú lateral y lista + detalle
    val tamano = rememberTamanoPantalla()
    val usarMenuLateral = tamano != TamanoPantalla.COMPACTA
    val dosPaneles = tamano == TamanoPantalla.EXPANDIDA
    val mostrarMenu = rutaActual in rutasConBarra
    val items = if (esAdmin) itemsAdmin else itemsCiudadano
    val usuarioId = usuario?.id ?: 0

    val irASeccion: (String) -> Unit = { ruta ->
        navController.navigate(ruta) {
            popUpTo(rutaInicio) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val abrirDetalle: (Int) -> Unit = { id -> navController.navigate(Rutas.detalle(id)) }
    val abrirEditar: (Int) -> Unit = { id -> navController.navigate(Rutas.editar(id)) }

    Scaffold(
        bottomBar = {
            if (mostrarMenu && !usarMenuLateral) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            icon = { IconoMenu(item, noLeidas) },
                            label = { Text(stringResource(item.etiqueta)) },
                            selected = rutaActual == item.ruta,
                            onClick = { irASeccion(item.ruta) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (mostrarMenu && usarMenuLateral) {
                NavigationRail {
                    items.forEach { item ->
                        NavigationRailItem(
                            icon = { IconoMenu(item, noLeidas) },
                            label = { Text(stringResource(item.etiqueta)) },
                            selected = rutaActual == item.ruta,
                            onClick = { irASeccion(item.ruta) }
                        )
                    }
                }
            }

            NavHost(
                navController = navController,
                startDestination = Rutas.LOGIN,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                composable(Rutas.LOGIN) {
                    ContenidoCentrado(anchoMaximo = 480.dp) {
                        LoginScreen(
                            authViewModel = authViewModel,
                            onLoginExitoso = irAInicio,
                            onIrARegistro = { navController.navigate(Rutas.REGISTRO) }
                        )
                    }
                }
                composable(Rutas.REGISTRO) {
                    ContenidoCentrado(anchoMaximo = 480.dp) {
                        RegistroScreen(
                            authViewModel = authViewModel,
                            onRegistroExitoso = irAInicio,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }

                // --- Ciudadano ---
                composable(Rutas.FORMULARIO) {
                    ContenidoCentrado {
                        FormularioScreen(usuarioId = usuarioId)
                    }
                }
                composable(Rutas.LISTA) {
                    ListaConDetalle(
                        dosPaneles = dosPaneles,
                        esAdmin = esAdmin,
                        usuarioId = usuarioId,
                        onAbrirDetalle = abrirDetalle,
                        onEditar = abrirEditar
                    ) { seleccionado, onClick ->
                        ListaScreen(
                            usuarioId = usuarioId,
                            onReporteClick = onClick,
                            reporteSeleccionado = seleccionado
                        )
                    }
                }
                composable(Rutas.NOTIFICACIONES) {
                    ContenidoCentrado {
                        NotificacionesScreen(
                            notificacionViewModel = notificacionViewModel,
                            onAbrirReporte = abrirDetalle
                        )
                    }
                }

                // --- Administrador / trabajador ---
                composable(Rutas.ADMIN_LISTA) {
                    ListaConDetalle(
                        dosPaneles = dosPaneles,
                        esAdmin = esAdmin,
                        usuarioId = usuarioId,
                        onAbrirDetalle = abrirDetalle,
                        onEditar = abrirEditar
                    ) { seleccionado, onClick ->
                        AdminListaScreen(
                            onReporteClick = onClick,
                            reporteSeleccionado = seleccionado
                        )
                    }
                }

                // --- Compartidas ---
                // Reportes de la ciudad
                composable(Rutas.CIUDAD) {
                    ListaConDetalle(
                        dosPaneles = dosPaneles,
                        esAdmin = esAdmin,
                        usuarioId = usuarioId,
                        onAbrirDetalle = abrirDetalle,
                        onEditar = abrirEditar
                    ) { seleccionado, onClick ->
                        ReportesCiudadScreen(
                            usuarioActualId = usuarioId,
                            onReporteClick = onClick,
                            reporteSeleccionado = seleccionado
                        )
                    }
                }
                composable(
                    route = Rutas.DETALLE,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entry ->
                    val id = entry.arguments?.getInt("id") ?: 0
                    ContenidoCentrado(anchoMaximo = 720.dp) {
                        DetalleReporteScreen(
                            reporteId = id,
                            esAdmin = esAdmin,
                            usuarioActualId = usuarioId,
                            onVolver = { navController.popBackStack() },
                            onEditar = { abrirEditar(id) }
                        )
                    }
                }
                composable(
                    route = Rutas.EDITAR,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entry ->
                    val id = entry.arguments?.getInt("id") ?: 0
                    ContenidoCentrado {
                        EditarReporteScreen(
                            reporteId = id,
                            onVolver = { navController.popBackStack() }
                        )
                    }
                }
                composable(Rutas.PERFIL) {
                    ContenidoCentrado {
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
    }
}

// Icono del menú (con el contador de avisos sin leer)
@Composable
private fun IconoMenu(item: ItemBarra, noLeidas: Int) {
    if (item.ruta == Rutas.NOTIFICACIONES && noLeidas > 0) {
        BadgedBox(badge = { Badge { Text(noLeidas.toString()) } }) {
            Icon(item.icono, contentDescription = null)
        }
    } else {
        Icon(item.icono, contentDescription = null)
    }
}

// Celular: al tocar un reporte se abre el detalle en otra pantalla.
// Pantalla grande: la lista y el detalle se ven lado a lado
@Composable
private fun ListaConDetalle(
    dosPaneles: Boolean,
    esAdmin: Boolean,
    usuarioId: Int,
    onAbrirDetalle: (Int) -> Unit,
    onEditar: (Int) -> Unit,
    lista: @Composable (seleccionado: Int?, onClick: (Int) -> Unit) -> Unit
) {
    var seleccionado by rememberSaveable { mutableStateOf<Int?>(null) }

    if (!dosPaneles) {
        lista(null, onAbrirDetalle)
        return
    }

    DosPaneles(
        izquierda = { lista(seleccionado) { id -> seleccionado = id } },
        derecha = {
            val id = seleccionado
            if (id == null) {
                PanelSinSeleccion()
            } else {
                key(id) {
                    DetalleReporteScreen(
                        reporteId = id,
                        esAdmin = esAdmin,
                        usuarioActualId = usuarioId,
                        onVolver = { seleccionado = null },
                        onEditar = { onEditar(id) },
                        mostrarVolver = false
                    )
                }
            }
        }
    )
}
