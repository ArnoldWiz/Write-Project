package com.chear.planit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chear.planit.data.Note
import com.chear.planit.ui.NoteViewModel

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

    var titulo by rememberSaveable { mutableStateOf("") }
    var cuerpo by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(key1 = noteToEdit) {
        noteToEdit?.let {
            titulo = it.title
            cuerpo = it.body
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Nota" else "Nueva Nota") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (titulo.isNotBlank() || cuerpo.isNotBlank()) {
                                if (isEditing && noteToEdit != null) {
                                    val updated = noteToEdit.copy(
                                        title = titulo,
                                        body = cuerpo
                                    )
                                    noteViewModel.update(updated)
                                } else {
                                    val nueva = Note(
                                        title = titulo,
                                        body = cuerpo
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
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cuerpo,
                onValueChange = { cuerpo = it },
                label = { Text("Cuerpo") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}
