package com.chear.planit.ui.screens

import androidx.compose.runtime.State
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

    // Estado para los campos de la nota
    private val _noteTitle = mutableStateOf("")
    val noteTitle: State<String> = _noteTitle

    private val _noteBody = mutableStateOf("")
    val noteBody: State<String> = _noteBody

    private val _attachmentUri = mutableStateOf<String?>(null)
    val attachmentUri: State<String?> = _attachmentUri

    // Cargar los datos de una nota existente en el estado del ViewModel
    fun loadNote(note: Note?) {
        if (note != null) {
            _noteTitle.value = note.title
            _noteBody.value = note.body
            _attachmentUri.value = note.attachmentUri
        } else {
            clearNoteFields()
        }
    }

    // Limpiar los campos del estado
    fun clearNoteFields() {
        _noteTitle.value = ""
        _noteBody.value = ""
        _attachmentUri.value = null
    }

    // Actualizadores de estado
    fun onTitleChange(newTitle: String) {
        _noteTitle.value = newTitle
    }

    fun onBodyChange(newBody: String) {
        _noteBody.value = newBody
    }

    fun onAttachmentChange(newUri: String?) {
        _attachmentUri.value = newUri
    }

    // Operaciones de la base de datos
    fun addNote() = viewModelScope.launch {
        val newNote = Note(
            title = _noteTitle.value,
            body = _noteBody.value,
            attachmentUri = _attachmentUri.value
            // La fecha se establece por defecto en el constructor de Note
        )
        repository.insert(newNote)
    }

    fun update(noteToUpdate: Note) = viewModelScope.launch {
        val updatedNote = noteToUpdate.copy(
            title = _noteTitle.value,
            body = _noteBody.value,
            attachmentUri = _attachmentUri.value
            // La fecha original de la nota no se modifica
        )
        repository.update(updatedNote)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
