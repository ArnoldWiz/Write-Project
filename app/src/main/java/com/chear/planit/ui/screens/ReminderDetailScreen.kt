package com.chear.planit.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.chear.planit.data.Reminder
import com.chear.planit.ui.screens.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.State

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    reminderId: String?,
    onNavigateBack: () -> Unit,
    reminderViewModel: ReminderViewModel
) {
    val isEditing = reminderId != null
    val reminders by reminderViewModel.reminders.collectAsState()

    val reminderToEdit: Reminder? = reminderId?.toIntOrNull()?.let { id ->
        reminders.find { it.id == id }
    }
    val reminderTitle by reminderViewModel.reminderTitle
    val reminderDescription by reminderViewModel.reminderDescription
    val reminderDateTime by reminderViewModel.reminderDateTime
    val reminderCompleted by reminderViewModel.reminderCompleted
    val attachmentUri by reminderViewModel.attachmentUri

    LaunchedEffect(key1 = reminderToEdit) {
        reminderViewModel.loadReminder(reminderToEdit)
    }

    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            reminderViewModel.onAttachmentChange(uri?.toString())
        }
    )

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = reminderDateTime?.let { dateFormatter.format(Date(it)) } ?: "Sin fecha"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Recordatorio" else "Nuevo Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = {
                        reminderViewModel.clearReminderFields()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (reminderTitle.isNotBlank() || reminderDescription.isNotBlank()) {
                                if (isEditing && reminderToEdit != null) {
                                    reminderViewModel.update(reminderToEdit)
                                } else {
                                    reminderViewModel.addReminder()
                                }
                            }
                            reminderViewModel.clearReminderFields()
                            onNavigateBack()
                        }
                    ) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = reminderTitle,
                onValueChange = { reminderViewModel.onTitleChange(it) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = reminderDescription,
                onValueChange = { reminderViewModel.onDescriptionChange(it) },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Text("Fecha y hora: $formattedDate")

            Button(onClick = {
                reminderViewModel.onDateTimeChange(System.currentTimeMillis())
            }) {
                Text("Usar fecha/hora actual")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = reminderCompleted,
                    onCheckedChange = { reminderViewModel.onCompletedChange(it) }
                )
                Text("Completado")
            }

            Button(onClick = { pickAttachmentLauncher.launch(arrayOf("*/*")) }) {
                Text("Adjuntar archivo")
            }

            attachmentUri?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Adjunto: ${it.toUri().lastPathSegment}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { reminderViewModel.onAttachmentChange(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Quitar adjunto")
                    }
                }
            }
        }
    }
}
