package com.chear.planit.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.chear.planit.data.Note
import com.chear.planit.ui.components.AttachmentItem
import com.chear.planit.utils.AudioRecorder
import com.chear.planit.utils.FileUtils
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

    // Selector de Archivos
    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                // SOLUCIÓN: Pedir permiso persistente para leer el archivo a largo plazo
                try {
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(it, takeFlags)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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

    // Audio
    val recorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                if (isRecording) {
                    recorder.stopRecording()
                    isRecording = false
                } else {
                    val file = recorder.startRecording()
                    if (file != null) {
                        isRecording = true
                        // Guardamos el archivo de audio en la lista
                        noteViewModel.addAttachment(file.toUri().toString())
                    }
                }
            }
        }
    )

    // Estado para el menú desplegable
    var showMenu by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Nota" else "Nueva Nota") },
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
                .padding(16.dp),
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
                    .weight(1f)
            )

            if (isEditing && noteToEdit != null) {
                val formattedDate = remember(noteToEdit.date) {
                    dateFormatter.format(Date(noteToEdit.date))
                }
                Text("Fecha de creación: $formattedDate")
            }

            // Botón Multimedia unificado
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Button(
                        onClick = {
                            recorder.stopRecording()
                            isRecording = false
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = "Detener grabación")
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
                                val uri = FileUtils.createImageFile(context)
                                tempPhotoUri = uri
                                cameraLauncher.launch(uri)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grabar video") },
                            onClick = {
                                showMenu = false
                                val uri = FileUtils.createVideoFile(context)
                                tempVideoUri = uri
                                videoLauncher.launch(uri)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Grabar audio") },
                            onClick = {
                                showMenu = false
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        )
                    }
                }
            }

            if (attachmentUris.isNotEmpty()) {
                Text("Archivos adjuntos:", style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(attachmentUris) { uri ->
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
