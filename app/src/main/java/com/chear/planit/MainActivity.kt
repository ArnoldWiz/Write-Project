package com.chear.planit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.chear.planit.ui.theme.AppTheme

object Rutas {
    const val PANTALLA_NOTAS = "notas"
    const val PANTALLA_RECORDATORIOS = "recordatorios"
    const val PANTALLA_DETALLE_NOTA = "detalle_nota"
    const val PANTALLA_DETALLE_RECORDATORIO = "detalle_recordatorio"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                PlanItApp()
            }
        }
    }
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


@Composable
fun PantallaNotas(alHacerClickEnNota: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("NOTAS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(items = (0..3).toList(), key = { it }) { idNota ->
            ElementoDeLista(
                esRecordatorio = false,
                alHacerClick = { alHacerClickEnNota(idNota.toString()) }
            )
        }
    }
}

@Composable
fun PantallaRecordatorios(alHacerClickEnRecordatorio: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("RECORDATORIOS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(items = (0..3).toList(), key = { it }) { idRecordatorio ->
            ElementoDeLista(
                esRecordatorio = true,
                alHacerClick = { alHacerClickEnRecordatorio(idRecordatorio.toString()) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleNota(idDeLaNota: String?, alNavegarAtras: () -> Unit) {
    val estaEditando = idDeLaNota != null
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estaEditando) "Editar Nota" else "Crear Nota") },
                navigationIcon = {
                    IconButton(onClick = alNavegarAtras) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = alNavegarAtras) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val titulo = if (estaEditando) "Título de la nota $idDeLaNota" else ""
            val cuerpo = if (estaEditando) "Contenido de la nota $idDeLaNota..." else ""

            OutlinedTextField(
                value = titulo,
                onValueChange = {},
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cuerpo,
                onValueChange = {},
                label = { Text("Cuerpo de la nota...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(onClick = {  }) {
                Icon(Icons.Default.Add, contentDescription = "Adjuntar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adjuntar Archivo")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleRecordatorio(idDelRecordatorio: String?, alNavegarAtras: () -> Unit) {
    val estaEditando = idDelRecordatorio != null
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estaEditando) "Editar Recordatorio" else "Crear Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = alNavegarAtras) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = alNavegarAtras) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val titulo = if (estaEditando) "Título del recordatorio $idDelRecordatorio" else ""

            OutlinedTextField(
                value = titulo,
                onValueChange = {},
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Fecha Límite (DD/MM/AAAA)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Cuerpo del recordatorio...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "Adjuntar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adjuntar Archivo")
            }
        }
    }
}

@Composable
fun ElementoDeLista(esRecordatorio: Boolean, alHacerClick: () -> Unit) {
    var estaMarcado by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alHacerClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (esRecordatorio) {
                Checkbox(
                    checked = estaMarcado,
                    onCheckedChange = { estaMarcado = it }
                )
            }
            Column(modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)) {
                Text("Título", fontWeight = FontWeight.Bold)
                Text("Contenido", style = MaterialTheme.typography.bodySmall)
            }
            Text("Fecha", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { }) {
                Icon(Icons.Default.Close, contentDescription = "Borrar")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun VistaPreviaApp() {
    AppTheme {
        PlanItApp()
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaDetalleNota() {
    AppTheme {
        PantallaDetalleNota(idDeLaNota = "123", alNavegarAtras = {})
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaDetalleRecordatorio() {
    AppTheme {
        PantallaDetalleRecordatorio (idDelRecordatorio = null, alNavegarAtras = {})
    }
}
