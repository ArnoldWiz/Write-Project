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
import com.chear.planit.data.Note
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    onNavigateBack: () -> Unit,
    noteViewModel: NoteViewModel
) {
    val isEditing = noteId != null
    val notes by noteViewModel.notes.collectAsState()

    val noteToEdit: Note? = noteId?.toIntOrNull()?.let { id ->
        notes.find { it.id == id }
    }

    // Cargar la nota en el ViewModel cuando la pantalla se compone por primera vez o la nota a editar cambia.
    LaunchedEffect(key1 = noteToEdit) {
        noteViewModel.loadNote(noteToEdit)
    }

    // Obtener el estado actual de los campos desde el ViewModel
    val noteTitle by noteViewModel.noteTitle
    val noteBody by noteViewModel.noteBody
    val attachmentUri by noteViewModel.attachmentUri

    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            noteViewModel.onAttachmentChange(uri?.toString())
        }
    )

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Nota" else "Nueva Nota") },
                navigationIcon = {
                    IconButton(onClick = {
                        noteViewModel.clearNoteFields() // Limpiar campos antes de navegar hacia atrás
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
                            noteViewModel.clearNoteFields() // Limpiar campos después de guardar
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

            // Mostrar la fecha de creación (si la nota existe)
            if (isEditing && noteToEdit != null) {
                val formattedDate = remember(noteToEdit.date) {
                    dateFormatter.format(Date(noteToEdit.date))
                }
                Text("Fecha de creación: $formattedDate")
            }

            Button(onClick = { pickAttachmentLauncher.launch(arrayOf("*/*")) }) {
                Text("Adjuntar archivo")
            }

            attachmentUri?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Adjunto: ${it.toUri().lastPathSegment}", modifier = Modifier.weight(1f))
                    IconButton(onClick = { noteViewModel.onAttachmentChange(null) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Quitar adjunto")
                    }
                }
            }
        }
    }
}
