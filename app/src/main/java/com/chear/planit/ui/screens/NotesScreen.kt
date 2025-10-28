package com.chear.planit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chear.planit.ui.components.ListElement

@Composable
fun NotesScreen(
    noteViewModel: NoteViewModel,
    onNoteClick: (String) -> Unit
) {
    val notes by noteViewModel.notes.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("NOTAS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (notes.isEmpty()) {
            item {
                Text("No hay notas todavía", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            items(notes, key = { it.id }) { note ->
                ListElement(
                    note = note,
                    isReminder = false,
                    alHacerClick = { onNoteClick(note.id.toString()) },
                    onDeleteClick = { noteViewModel.delete(note) }
                )

        }
        }
    }
}
