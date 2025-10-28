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
import com.chear.planit.data.Note

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

    var noteTitle by rememberSaveable { mutableStateOf("") }
    var noteBody by rememberSaveable { mutableStateOf("") }
    var attachmentUri by rememberSaveable { mutableStateOf<String?>(null) }

    val pickAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            attachmentUri = uri?.toString()
        }
    )

    LaunchedEffect(key1 = noteToEdit) {
        noteToEdit?.let {
            noteTitle = it.title
            noteBody = it.body
            attachmentUri = it.attachmentUri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Nota" else "Nueva Nota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (noteTitle.isNotBlank() || noteBody.isNotBlank()) {
                                if (isEditing && noteToEdit != null) {
                                    val updated = noteToEdit.copy(
                                        title = noteTitle,
                                        body = noteBody,
                                        attachmentUri = attachmentUri
                                    )
                                    noteViewModel.update(updated)
                                } else {
                                    val nueva = Note(
                                        title = noteTitle,
                                        body = noteBody,
                                        attachmentUri = attachmentUri
                                    )
                                    noteViewModel.addNote(nueva)
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
                value = noteTitle,
                onValueChange = { noteTitle = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = noteBody,
                onValueChange = { noteBody = it },
                label = { Text("Cuerpo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
