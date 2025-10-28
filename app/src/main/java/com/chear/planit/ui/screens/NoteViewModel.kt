package com.chear.planit.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chear.planit.data.Note
import com.chear.planit.data.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var noteTitle = mutableStateOf("")
        private set
    var noteBody = mutableStateOf("")
        private set
    var attachmentUri = mutableStateOf<String?>(null)
        private set

    fun onTitleChange(newTitle: String) {
        noteTitle.value = newTitle
    }

    fun onBodyChange(newBody: String) {
        noteBody.value = newBody
    }

    fun onAttachmentChange(newUri: String?) {
        attachmentUri.value = newUri
    }

    fun loadNote(note: Note?) {
        if (note != null) {
            noteTitle.value = note.title
            noteBody.value = note.body
            attachmentUri.value = note.attachmentUri
        } else {
            clearNoteFields()
        }
    }

    fun clearNoteFields() {
        noteTitle.value = ""
        noteBody.value = ""
        attachmentUri.value = null
    }

    fun addNote() = viewModelScope.launch {
        if (noteTitle.value.isNotBlank() || noteBody.value.isNotBlank()) {
            repository.insert(
                Note(
                    title = noteTitle.value,
                    body = noteBody.value,
                    attachmentUri = attachmentUri.value
                )
            )
            clearNoteFields()
        }
    }

    fun update(existingNote: Note) = viewModelScope.launch {
        val updated = existingNote.copy(
            title = noteTitle.value,
            body = noteBody.value,
            attachmentUri = attachmentUri.value
        )
        repository.update(updated)
        clearNoteFields()
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
