package com.chear.planit.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chear.planit.ui.screens.PantallaDetalleNota
import com.chear.planit.ui.screens.PantallaDetalleRecordatorio
import com.chear.planit.ui.screens.PantallaNotas
import com.chear.planit.ui.screens.PantallaRecordatorios

object Rutas {
    const val PANTALLA_NOTAS = "notas"
    const val PANTALLA_RECORDATORIOS = "recordatorios"
    const val PANTALLA_DETALLE_NOTA = "detalle_nota"
    const val PANTALLA_DETALLE_RECORDATORIO = "detalle_recordatorio"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanItApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val esPantallaPrincipal = rutaActual == Rutas.PANTALLA_NOTAS || rutaActual == Rutas.PANTALLA_RECORDATORIOS

    Scaffold(
        topBar = {
            if (esPantallaPrincipal) {
                TopAppBar(
                    title = {
                        Text(
                            text = "PlanIt",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (esPantallaPrincipal) {
                FloatingActionButton(
                    onClick = {
                        if (rutaActual == Rutas.PANTALLA_NOTAS) {
                            navController.navigate(Rutas.PANTALLA_DETALLE_NOTA)
                        } else {
                            navController.navigate(Rutas.PANTALLA_DETALLE_RECORDATORIO)
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Filled.Add, "Añadir")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            if (esPantallaPrincipal) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NavigationBarItem(
                        selected = rutaActual == Rutas.PANTALLA_NOTAS,
                        onClick = { navController.navigate(Rutas.PANTALLA_NOTAS) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Notas") },
                        label = { Text("Notas") }
                    )
                    NavigationBarItem(
                        selected = rutaActual == Rutas.PANTALLA_RECORDATORIOS,
                        onClick = { navController.navigate(Rutas.PANTALLA_RECORDATORIOS) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Recordatorios") },
                        label = { Text("Recordatorios") }
                    )
                }
            }
        }
    ) { paddingInterno ->
        NavHost(
            navController = navController,
            startDestination = Rutas.PANTALLA_NOTAS,
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable(Rutas.PANTALLA_NOTAS) {
                PantallaNotas(
                    alHacerClickEnNota = { idDeLaNota ->
                        navController.navigate("${Rutas.PANTALLA_DETALLE_NOTA}/$idDeLaNota")
                    }
                )
            }
            composable(Rutas.PANTALLA_RECORDATORIOS) {
                PantallaRecordatorios(
                    alHacerClickEnRecordatorio = { idDelRecordatorio ->
                        navController.navigate("${Rutas.PANTALLA_DETALLE_RECORDATORIO}/$idDelRecordatorio")
                    }
                )
            }
            composable(
                route = "${Rutas.PANTALLA_DETALLE_NOTA}/{idNota}",
                arguments = listOf(navArgument("idNota") { type = NavType.StringType; nullable = true })
            ) { navBackStackEntry ->
                val idNota = navBackStackEntry.arguments?.getString("idNota")
                PantallaDetalleNota(idDeLaNota = idNota, alNavegarAtras = { navController.popBackStack() })
            }
            composable(Rutas.PANTALLA_DETALLE_NOTA) {
                PantallaDetalleNota(idDeLaNota = null, alNavegarAtras = { navController.popBackStack() })
            }
            composable(
                route = "${Rutas.PANTALLA_DETALLE_RECORDATORIO}/{idRecordatorio}",
                arguments = listOf(navArgument("idRecordatorio") { type = NavType.StringType; nullable = true })
            ) { navBackStackEntry ->
                val idRecordatorio = navBackStackEntry.arguments?.getString("idRecordatorio")
                PantallaDetalleRecordatorio(idDelRecordatorio = idRecordatorio, alNavegarAtras = { navController.popBackStack() })
            }
            composable(Rutas.PANTALLA_DETALLE_RECORDATORIO) {
                PantallaDetalleRecordatorio(idDelRecordatorio = null, alNavegarAtras = { navController.popBackStack() })
            }
        }
    }
}