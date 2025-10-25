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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.chear.planit.data.Reminder
import com.chear.planit.ui.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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

    var reminderTitle by rememberSaveable { mutableStateOf("") }
    var reminderBody by rememberSaveable { mutableStateOf("") }
    var reminderTimestamp by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var attachmentUri by rememberSaveable { mutableStateOf<String?>(null) }

    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            attachmentUri = uri?.toString()
        }
    )

    LaunchedEffect(key1 = reminderToEdit) {
        reminderToEdit?.let {
            reminderTitle = it.title
            reminderBody = it.description
            reminderTimestamp = it.dateTime
            attachmentUri = it.attachmentUri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Recordatorio" else "Nuevo Recordatorio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (reminderTitle.isNotBlank() || reminderBody.isNotBlank()) {
                                if (isEditing && reminderToEdit != null) {
                                    val updated = reminderToEdit.copy(
                                        title = reminderTitle,
                                        description = reminderBody,
                                        dateTime = reminderTimestamp,
                                        attachmentUri = attachmentUri
                                    )
                                    reminderViewModel.updateReminder(updated)
                                } else {
                                    val nuevo = Reminder(
                                        title = reminderTitle,
                                        description = reminderBody,
                                        dateTime = reminderTimestamp,
                                        attachmentUri = attachmentUri
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
            var showDatePicker by remember { mutableStateOf(false) }
            var showTimePicker by remember { mutableStateOf(false) }

            val calendar = remember { Calendar.getInstance() }
            calendar.timeInMillis = reminderTimestamp

            val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
            val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

            OutlinedTextField(
                value = reminderTitle,
                onValueChange = { reminderTitle = it },
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
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = reminderTimestamp)
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
                                    reminderTimestamp = calendar.timeInMillis
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
                                reminderTimestamp = calendar.timeInMillis
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
                value = reminderBody,
                onValueChange = { reminderBody = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Button(onClick = { pickAttachmentLauncher.launch(arrayOf("*/*")) }) {
                Text("Adjuntar archivo")
            }

            attachmentUri?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Adjunto: ${it.toUri().lastPathSegment}", modifier = Modifier.weight(1f))
                    IconButton(onClick = { attachmentUri = null }) {
                        Icon(Icons.Default.Clear, contentDescription = "Quitar adjunto")
                    }
                }
            }
        }
    }
}
