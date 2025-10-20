package com.chear.planit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chear.planit.data.Reminder
import com.chear.planit.ui.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    reminderId: String?,
    onNavigateBack: () -> Unit,
    reminderViewModel: ReminderViewModel
) {
    val isEditing = reminderId != null

    // Obtenemos la lista de recordatorios
    val reminders by reminderViewModel.reminders.collectAsState()

    // Buscamos el recordatorio a editar si aplica
    val reminderToEdit: Reminder? = reminderId?.toIntOrNull()?.let { id ->
        reminders.find { it.id == id }
    }

    // Estados para los campos
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var fechaHora by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }

    // Si estamos editando, cargamos los datos existentes
    LaunchedEffect(key1 = reminderToEdit) {
        reminderToEdit?.let {
            titulo = it.title
            descripcion = it.description
            fechaHora = it.dateTime
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Recordatorio" else "Nuevo Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (titulo.isNotBlank() || descripcion.isNotBlank()) {
                                if (isEditing && reminderToEdit != null) {
                                    // Editamos
                                    val updated = reminderToEdit.copy(
                                        title = titulo,
                                        description = descripcion,
                                        dateTime = fechaHora
                                    )
                                    reminderViewModel.updateReminder(updated)
                                } else {
                                    // Creamos nuevo
                                    val nuevo = Reminder(
                                        title = titulo,
                                        description = descripcion,
                                        dateTime = fechaHora
                                    )
                                    reminderViewModel.addReminder(nuevo)
                                }
                            }
                            onNavigateBack()
                        }
                    ) {
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
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
