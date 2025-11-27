package com.chear.planit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.chear.planit.data.Reminder
import com.chear.planit.ui.components.AttachmentItem
import com.chear.planit.utils.AudioRecorder
import com.chear.planit.utils.FileUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDetailScreen(
    reminderId: String?,
    onNavigateBack: () -> Unit,
    reminderViewModel: ReminderViewModel
) {
    val context = LocalContext.current
    val isEditing = reminderId != null
    val reminders by reminderViewModel.reminders.collectAsState()

    val reminderToEdit: Reminder? = reminderId?.toIntOrNull()?.let { id ->
        reminders.find { it.id == id }
    }

    LaunchedEffect(key1 = reminderToEdit) {
        reminderViewModel.loadReminder(reminderToEdit)
    }

    val reminderTitle by reminderViewModel.reminderTitle
    val reminderDescription by reminderViewModel.reminderDescription
    val reminderDateTime by reminderViewModel.reminderDateTime
    val additionalDates by reminderViewModel.additionalDates
    val reminderCompleted by reminderViewModel.reminderCompleted
    val attachmentUris by reminderViewModel.attachmentUris

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Estados para controlar permisos y contadores de rechazo
    var cameraPermissionDeniedCount by remember { mutableIntStateOf(0) }
    var audioPermissionDeniedCount by remember { mutableIntStateOf(0) }
    var pendingCameraAction by remember { mutableStateOf<String?>(null) }

    // Función de comprobación de permisos
    val hasPermission: (String) -> Boolean = { permission ->
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // Inicializamos dateTime con el momento actual si es null
    LaunchedEffect(Unit) {
        if (reminderDateTime == null) {
            reminderViewModel.onDateTimeChange(System.currentTimeMillis())
        }
    }

    // Selector de Archivos
    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                reminderViewModel.addAttachment(it.toString())
            }
        }
    )

    // Cámara (Foto)
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                reminderViewModel.addAttachment(tempPhotoUri.toString())
            }
        }
    )

    // Cámara (Video)
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success && tempVideoUri != null) {
                reminderViewModel.addAttachment(tempVideoUri.toString())
            }
        }
    )

    // Launcher de permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (pendingCameraAction == "PHOTO") {
                    val uri = FileUtils.createImageFile(context)
                    tempPhotoUri = uri
                    cameraLauncher.launch(uri)
                } else if (pendingCameraAction == "VIDEO") {
                    val uri = FileUtils.createVideoFile(context)
                    tempVideoUri = uri
                    videoLauncher.launch(uri)
                }
            } else {
                cameraPermissionDeniedCount++
                if (cameraPermissionDeniedCount >= 2) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Debe activar los permisos en la configuración")
                    }
                }
            }
            pendingCameraAction = null
        }
    )

    // Audio
    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (isRecording) {
                    recorder.stopRecording()
                    isRecording = false
                    isPaused = false
                } else {
                    val file = recorder.startRecording()
                    if (file != null) {
                        isRecording = true
                        isPaused = false
                        reminderViewModel.addAttachment(file.toUri().toString())
                    }
                }
            } else {
                audioPermissionDeniedCount++
                if (audioPermissionDeniedCount >= 2) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Debe activar los permisos en la configuración")
                    }
                }
            }
        }
    )

    var showMenu by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Variables para el selector de fechas adicionales
    var showAdditionalDatePicker by remember { mutableStateOf(false) }
    var showAdditionalTimePicker by remember { mutableStateOf(false) }
    val additionalCalendar = remember { Calendar.getInstance() }

    val calendar = remember { Calendar.getInstance() }
    reminderDateTime?.let { calendar.timeInMillis = it }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val fullDateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                                    reminderViewModel.update(reminderToEdit, context)
                                } else {
                                    reminderViewModel.addReminder(context)
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
                .padding(16.dp)
                .verticalScroll(scrollState),
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

            // Sección de Fechas Adicionales (Cambiado a "Avisos")
            Text("Avisos:", style = MaterialTheme.typography.titleSmall)
            
            // Usamos Column en lugar de LazyColumn dentro de un scrollable Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                additionalDates.forEach { date ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fullDateFormatter.format(Date(date)))
                        IconButton(onClick = { reminderViewModel.removeAdditionalDate(date) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar aviso")
                        }
                    }
                }
                
                Button(
                    onClick = { showAdditionalDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar aviso")
                }
            }

            // Date Picker Principal
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = reminderDateTime ?: System.currentTimeMillis())
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    utcCalendar.timeInMillis = millis
                                    calendar.set(
                                        utcCalendar.get(Calendar.YEAR),
                                        utcCalendar.get(Calendar.MONTH),
                                        utcCalendar.get(Calendar.DAY_OF_MONTH)
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

            // Time Picker Principal
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
                                calendar.set(Calendar.SECOND, 0)
                                calendar.set(Calendar.MILLISECOND, 0)
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

            // Date Picker Adicional (Avisos)
             if (showAdditionalDatePicker) {
                val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
                DatePickerDialog(
                    onDismissRequest = { showAdditionalDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    utcCalendar.timeInMillis = millis
                                    additionalCalendar.set(
                                        utcCalendar.get(Calendar.YEAR),
                                        utcCalendar.get(Calendar.MONTH),
                                        utcCalendar.get(Calendar.DAY_OF_MONTH)
                                    )
                                    // Una vez seleccionada la fecha, mostramos el selector de hora
                                    showAdditionalDatePicker = false
                                    showAdditionalTimePicker = true
                                }
                            }
                        ) { Text("Siguiente") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdditionalDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // Time Picker Adicional (Avisos)
            if (showAdditionalTimePicker) {
                val timePickerState = rememberTimePickerState(
                    initialHour = additionalCalendar.get(Calendar.HOUR_OF_DAY),
                    initialMinute = additionalCalendar.get(Calendar.MINUTE),
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showAdditionalTimePicker = false },
                    modifier = Modifier.fillMaxWidth(),
                    title = { Text("Seleccionar Hora del Aviso", style = MaterialTheme.typography.titleLarge) },
                    text = {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            TimePicker(state = timePickerState)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                additionalCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                additionalCalendar.set(Calendar.MINUTE, timePickerState.minute)
                                additionalCalendar.set(Calendar.SECOND, 0)
                                additionalCalendar.set(Calendar.MILLISECOND, 0)
                                reminderViewModel.addAdditionalDate(additionalCalendar.timeInMillis)
                                showAdditionalTimePicker = false
                            }
                        ) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAdditionalTimePicker = false }) { Text("Cancelar") }
                    }
                )
            }

            OutlinedTextField(
                value = reminderDescription,
                onValueChange = { reminderViewModel.onDescriptionChange(it) },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp) // Altura mínima para el contenido
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

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isPaused) "Pausado" else "Grabando...",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                IconButton(
                                    onClick = {
                                        if (isPaused) {
                                            recorder.resumeRecording()
                                            isPaused = false
                                        } else {
                                            recorder.pauseRecording()
                                            isPaused = true
                                        }
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(4.dp)
                                ) {
                                    if (isPaused) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Reanudar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            "||",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    recorder.stopRecording()
                                    isRecording = false
                                    isPaused = false
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Detener grabación")
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { showMenu = true },
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Multimedia")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Adjuntar archivo") },
                            onClick = {
                                showMenu = false
                                pickAttachmentLauncher.launch(arrayOf("*/*"))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Tomar foto") },
                            onClick = {
                                showMenu = false
                                if (hasPermission(Manifest.permission.CAMERA)) {
                                    val uri = FileUtils.createImageFile(context)
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    if (cameraPermissionDeniedCount >= 2) {
                                        scope.launch { snackbarHostState.showSnackbar("Debe activar los permisos en la configuración") }
                                    } else {
                                        pendingCameraAction = "PHOTO"
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grabar video") },
                            onClick = {
                                showMenu = false
                                if (hasPermission(Manifest.permission.CAMERA)) {
                                    val uri = FileUtils.createVideoFile(context)
                                    tempVideoUri = uri
                                    videoLauncher.launch(uri)
                                } else {
                                    if (cameraPermissionDeniedCount >= 2) {
                                        scope.launch { snackbarHostState.showSnackbar("Debe activar los permisos en la configuración") }
                                    } else {
                                        pendingCameraAction = "VIDEO"
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grabar audio") },
                            onClick = {
                                showMenu = false
                                if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    if (audioPermissionDeniedCount >= 2) {
                                        scope.launch { snackbarHostState.showSnackbar("Debe activar los permisos en la configuración") }
                                    } else {
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (attachmentUris.isNotEmpty()) {
                Text("Archivos adjuntos:", style = MaterialTheme.typography.titleSmall)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachmentUris.forEach { uri ->
                        AttachmentItem(
                            uriString = uri,
                            onRemove = { reminderViewModel.removeAttachment(uri) }
                        )
                    }
                }
            }
        }
    }
}
