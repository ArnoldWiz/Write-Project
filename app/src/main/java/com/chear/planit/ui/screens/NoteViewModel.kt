package com.chear.planit.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chear.planit.data.Attachment
import com.chear.planit.data.Note
import com.chear.planit.data.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _notes = repository.getAll()
    val notes: StateFlow<List<Note>> = searchQuery
        .debounce(500)
        .combine(_notes) { query, notes ->
            if (query.isBlank()) {
                notes
            } else {
                notes.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.body.contains(query, ignoreCase = true)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    // Estado para los campos de la nota
    private val _noteTitle = mutableStateOf("")
    val noteTitle: State<String> = _noteTitle

    private val _noteBody = mutableStateOf("")
    val noteBody: State<String> = _noteBody

    // Ahora usamos una lista de Attachment
    private val _attachmentUris = mutableStateOf<List<Attachment>>(emptyList())
    val attachmentUris: State<List<Attachment>> = _attachmentUris

    // Cargar los datos de una nota existente en el estado del ViewModel
    fun loadNote(note: Note?) {
        if (note != null) {
            _noteTitle.value = note.title
            _noteBody.value = note.body
            _attachmentUris.value = note.attachments
        } else {
            clearNoteFields()
        }
    }

    // Limpiar los campos del estado
    fun clearNoteFields() {
        _noteTitle.value = ""
        _noteBody.value = ""
        _attachmentUris.value = emptyList()
    }

    // Actualizadores de estado
    fun onTitleChange(newTitle: String) {
        _noteTitle.value = newTitle
    }

    fun onBodyChange(newBody: String) {
        _noteBody.value = newBody
    }

    // Funciones para añadir y eliminar adjuntos
    fun addAttachment(newUri: String?) {
        newUri?.let {
            val currentList = _attachmentUris.value.toMutableList()
            currentList.add(Attachment(uri = it))
            _attachmentUris.value = currentList
        }
    }

    fun removeAttachment(uriToRemove: String) {
        val currentList = _attachmentUris.value.toMutableList()
        currentList.removeAll { it.uri == uriToRemove }
        _attachmentUris.value = currentList
    }

    fun updateAttachmentDescription(uri: String, newDescription: String) {
        val currentList = _attachmentUris.value.map {
            if (it.uri == uri) {
                it.copy(description = newDescription)
            } else {
                it
            }
        }
        _attachmentUris.value = currentList
    }

    // Operaciones de la base de datos
    fun addNote() = viewModelScope.launch {
        val newNote = Note(
            title = _noteTitle.value,
            body = _noteBody.value,
            attachments = _attachmentUris.value
        )
        repository.insert(newNote)
    }

    fun update(noteToUpdate: Note) = viewModelScope.launch {
        val updatedNote = noteToUpdate.copy(
            title = _noteTitle.value,
            body = _noteBody.value,
            attachments = _attachmentUris.value
        )
        repository.update(updatedNote)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }
}
