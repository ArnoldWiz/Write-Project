package com.chear.planit.ui.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chear.planit.data.Reminder
import com.chear.planit.data.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repository.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Estado para los campos del recordatorio
    private val _reminderTitle = mutableStateOf("")
    val reminderTitle: State<String> = _reminderTitle

    private val _reminderDescription = mutableStateOf("")
    val reminderDescription: State<String> = _reminderDescription

    private val _reminderDateTime = mutableStateOf<Long?>(null)
    val reminderDateTime: State<Long?> = _reminderDateTime

    private val _reminderCompleted = mutableStateOf(false)
    val reminderCompleted: State<Boolean> = _reminderCompleted

    // CAMBIO: Ahora usamos una lista de Strings para múltiples adjuntos
    private val _attachmentUris = mutableStateOf<List<String>>(emptyList())
    val attachmentUris: State<List<String>> = _attachmentUris

    fun loadReminder(reminder: Reminder?) {
        if (reminder != null) {
            _reminderTitle.value = reminder.title
            _reminderDescription.value = reminder.description
            _reminderDateTime.value = reminder.dateTime
            _reminderCompleted.value = reminder.isCompleted
            _attachmentUris.value = reminder.attachmentUris
        } else {
            clearReminderFields()
        }
    }

    fun clearReminderFields() {
        _reminderTitle.value = ""
        _reminderDescription.value = ""
        _reminderDateTime.value = null
        _reminderCompleted.value = false
        _attachmentUris.value = emptyList()
    }

    fun onTitleChange(newTitle: String) {
        _reminderTitle.value = newTitle
    }

    fun onDescriptionChange(newDescription: String) {
        _reminderDescription.value = newDescription
    }

    fun onDateTimeChange(newDateTime: Long?) {
        _reminderDateTime.value = newDateTime
    }

    fun onCompletedChange(isCompleted: Boolean) {
        _reminderCompleted.value = isCompleted
    }

    // CAMBIO: Funciones para añadir y eliminar adjuntos a la lista
    fun addAttachment(newUri: String?) {
        newUri?.let {
            val currentList = _attachmentUris.value.toMutableList()
            currentList.add(it)
            _attachmentUris.value = currentList
        }
    }

    fun removeAttachment(uriToRemove: String) {
        val currentList = _attachmentUris.value.toMutableList()
        currentList.remove(uriToRemove)
        _attachmentUris.value = currentList
    }

    fun addReminder() = viewModelScope.launch {
        val newReminder = Reminder(
            title = _reminderTitle.value,
            description = _reminderDescription.value,
            dateTime = _reminderDateTime.value ?: System.currentTimeMillis(),
            isCompleted = _reminderCompleted.value,
            attachmentUris = _attachmentUris.value
        )
        repository.insert(newReminder)
    }

    fun update(reminderToUpdate: Reminder) = viewModelScope.launch {
        val updatedReminder = reminderToUpdate.copy(
            title = _reminderTitle.value,
            description = _reminderDescription.value,
            dateTime = _reminderDateTime.value ?: reminderToUpdate.dateTime,
            isCompleted = _reminderCompleted.value,
            attachmentUris = _attachmentUris.value
        )
        repository.update(updatedReminder)
    }

    fun delete(reminder: Reminder) = viewModelScope.launch {
        repository.delete(reminder)
    }
}
