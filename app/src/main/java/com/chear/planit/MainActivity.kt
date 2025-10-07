package com.chear.planit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.compose.*

import com.chear.planit.ui.theme.AppTheme

// Objeto para centralizar todas las rutas de navegación
object Rutas {
    const val NOTAS_SCREEN = "notas"
    const val RECORDATORIOS_SCREEN = "recordatorios"
    const val AGREGAR_NOTAS_SCREEN = "agregar_nota"
    const val AGREGAR_RECORDATORIO_SCREEN = "agregar_recordatorio"
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
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute == Rutas.NOTAS_SCREEN || currentRoute == Rutas.RECORDATORIOS_SCREEN) {
                TopAppBar(
                    title = {
                        Text(
                            text = "PlanIt",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentRoute == Rutas.NOTAS_SCREEN || currentRoute == Rutas.RECORDATORIOS_SCREEN) {
                FloatingActionButton(
                    onClick = {
                        if (currentRoute == Rutas.NOTAS_SCREEN) {
                            navController.navigate(Rutas.AGREGAR_NOTAS_SCREEN)
                        } else {
                            navController.navigate(Rutas.AGREGAR_RECORDATORIO_SCREEN)
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
            if (currentRoute == Rutas.NOTAS_SCREEN || currentRoute == Rutas.RECORDATORIOS_SCREEN) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NavigationBarItem(
                        selected = currentRoute == Rutas.NOTAS_SCREEN,
                        onClick = { navController.navigate(Rutas.NOTAS_SCREEN) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Notas") },
                        label = { Text("Notas") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Rutas.RECORDATORIOS_SCREEN,
                        onClick = { navController.navigate(Rutas.RECORDATORIOS_SCREEN) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Recordatorios") },
                        label = { Text("Recordatorios") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rutas.NOTAS_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Rutas.NOTAS_SCREEN) {
                NotasScreen()
            }
            composable(Rutas.RECORDATORIOS_SCREEN) {
                RecordatoriosScreen()
            }
            composable(Rutas.AGREGAR_NOTAS_SCREEN) {
                AgregarNotaScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Rutas.AGREGAR_RECORDATORIO_SCREEN) {
                AgregarRecordatorioScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}


@Composable
fun NotasScreen() {
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
        items(4) {
            NoteItem(isReminder = false)
        }
    }
}

@Composable
fun RecordatoriosScreen() {
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
        items(4) {
            NoteItem(isReminder = true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarNotaScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Nota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // Botón para volver atrás
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = { onNavigateBack() }) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
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
fun AgregarRecordatorioScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = { onNavigateBack() }) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = "",
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
fun NoteItem(isReminder: Boolean) {
    var isChecked by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isReminder) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it }
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
fun PlanItAppPreview() {
    AppTheme {
        PlanItApp()
    }
}

@Preview(showBackground = true)
@Composable
fun AddNoteScreenPreview() {
    AppTheme {
        AgregarNotaScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AddReminderScreenPreview() {
    AppTheme {
        AgregarRecordatorioScreen (onNavigateBack = {})
    }
}
