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
import java.text.SimpleDateFormat
import java.util.*

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

    // Cargar el recordatorio en el ViewModel
    LaunchedEffect(key1 = reminderToEdit) {
        reminderViewModel.loadReminder(reminderToEdit)
    }

    val reminderTitle by reminderViewModel.reminderTitle
    val reminderDescription by reminderViewModel.reminderDescription
    val reminderDateTime by reminderViewModel.reminderDateTime
    val reminderCompleted by reminderViewModel.reminderCompleted
    val attachmentUri by reminderViewModel.attachmentUri

    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            reminderViewModel.onAttachmentChange(uri?.toString())
        }
    )

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val calendar = remember { Calendar.getInstance() }
    reminderDateTime?.let { calendar.timeInMillis = it }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { showDatePicker = true }) {
                    Text("Fecha")
                }
                Text(dateFormatter.format(calendar.time))
                Button(onClick = { showTimePicker = true }) {
                    Text("Hora")
                }
                Text(timeFormatter.format(calendar.time))
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = reminderDateTime ?: System.currentTimeMillis())
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val newCal = Calendar.getInstance().apply { timeInMillis = millis }
                                    calendar.set(
                                        newCal.get(Calendar.YEAR),
                                        newCal.get(Calendar.MONTH),
                                        newCal.get(Calendar.DAY_OF_MONTH)
                                    )
                                    reminderViewModel.onDateTimeChange(calendar.timeInMillis)
                                }
                                showDatePicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                    initialMinute = calendar.get(Calendar.MINUTE),
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text("Seleccionar Hora", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TimePicker(state = timePickerState)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                calendar.set(Calendar.MINUTE, timePickerState.minute)
                                reminderViewModel.onDateTimeChange(calendar.timeInMillis)
                                showTimePicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                    }
                )
            }

            OutlinedTextField(
                value = reminderDescription,
                onValueChange = { reminderViewModel.onDescriptionChange(it) },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

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
