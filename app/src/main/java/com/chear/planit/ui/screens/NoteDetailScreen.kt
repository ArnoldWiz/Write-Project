package com.chear.planit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Clear
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
import com.chear.planit.R
import com.chear.planit.data.Note
import com.chear.planit.ui.components.AttachmentItem
import com.chear.planit.utils.AudioRecorder
import com.chear.planit.utils.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    onNavigateBack: () -> Unit,
    noteViewModel: NoteViewModel
) {
    val context = LocalContext.current
    val isEditing = noteId != null
    val notes by noteViewModel.notes.collectAsState()

    val noteToEdit: Note? = noteId?.toIntOrNull()?.let { id ->
        notes.find { it.id == id }
    }

    LaunchedEffect(key1 = noteToEdit) {
        noteViewModel.loadNote(noteToEdit)
    }

    val noteTitle by noteViewModel.noteTitle
    val noteBody by noteViewModel.noteBody
    val attachmentUris by noteViewModel.attachmentUris

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

    // Selector de Archivos
    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (_: Exception) {}
                noteViewModel.addAttachment(it.toString())
            }
        }
    )

    // Cámara (Foto)
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempPhotoUri != null) {
                noteViewModel.addAttachment(tempPhotoUri.toString())
            }
        }
    )

    // Cámara (Video)
    var tempVideoUri by remember { mutableStateOf<Uri?>(null) }
    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo(),
        onResult = { success ->
            if (success && tempVideoUri != null) {
                noteViewModel.addAttachment(tempVideoUri.toString())
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
                        delay(1000)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
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
                        noteViewModel.addAttachment(file.toUri().toString())
                    }
                }
            } else {
                audioPermissionDeniedCount++
                if (audioPermissionDeniedCount >= 2) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Debe activar los permisos en la configuración")
                        delay(1000)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                }
            }
        }
    )

    var showMenu by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) context.getString(R.string.edit_Note) else context.getString(R.string.newNote)) },
                navigationIcon = {
                    IconButton(onClick = {
                        noteViewModel.clearNoteFields()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (noteTitle.isNotBlank() || noteBody.isNotBlank()) {
                                if (isEditing && noteToEdit != null) {
                                    noteViewModel.update(noteToEdit)
                                } else {
                                    noteViewModel.addNote()
                                }
                            }
                            noteViewModel.clearNoteFields()
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
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = noteTitle,
                onValueChange = { noteViewModel.onTitleChange(it) },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = noteBody,
                onValueChange = { noteViewModel.onBodyChange(it) },
                label = { Text("Contenido") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
            )

            if (isEditing && noteToEdit != null) {
                val formattedDate = remember(noteToEdit.date) {
                    dateFormatter.format(Date(noteToEdit.date))
                }
                Text("Fecha de creación: $formattedDate")
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
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Debe activar los permisos en la configuración")
                                            delay(5000)
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.data = Uri.fromParts("package", context.packageName, null)
                                            context.startActivity(intent)
                                        }
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
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Debe activar los permisos en la configuración")
                                            delay(5000)
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.data = Uri.fromParts("package", context.packageName, null)
                                            context.startActivity(intent)
                                        }
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
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Debe activar los permisos en la configuración")
                                            delay(5000)
                                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.data = Uri.fromParts("package", context.packageName, null)
                                            context.startActivity(intent)
                                        }
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
                // No usamos LazyColumn aquí porque estamos dentro de un Column con scroll
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachmentUris.forEach { uri ->
                        AttachmentItem(
                            uriString = uri,
                            onRemove = { noteViewModel.removeAttachment(uri) }
                        )
                    }
                }
            }
        }
    }
}
