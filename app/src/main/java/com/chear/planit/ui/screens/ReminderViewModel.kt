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

    private val _attachmentUri = mutableStateOf<String?>(null)
    val attachmentUri: State<String?> = _attachmentUri

    fun loadReminder(reminder: Reminder?) {
        if (reminder != null) {
            _reminderTitle.value = reminder.title
            _reminderDescription.value = reminder.description
            _reminderDateTime.value = reminder.dateTime
            _reminderCompleted.value = reminder.isCompleted
            _attachmentUri.value = reminder.attachmentUri
        } else {
            clearReminderFields()
        }
    }

    fun clearReminderFields() {
        _reminderTitle.value = ""
        _reminderDescription.value = ""
        _reminderDateTime.value = null
        _reminderCompleted.value = false
        _attachmentUri.value = null
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

    fun onAttachmentChange(newUri: String?) {
        _attachmentUri.value = newUri
    }

    fun addReminder() = viewModelScope.launch {
        val newReminder = Reminder(
            title = _reminderTitle.value,
            description = _reminderDescription.value,
            dateTime = _reminderDateTime.value ?: System.currentTimeMillis(),
            isCompleted = _reminderCompleted.value,
            attachmentUri = _attachmentUri.value
        )
        repository.insert(newReminder)
    }

    fun update(reminderToUpdate: Reminder) = viewModelScope.launch {
        val updatedReminder = reminderToUpdate.copy(
            title = _reminderTitle.value,
            description = _reminderDescription.value,
            dateTime = _reminderDateTime.value ?: reminderToUpdate.dateTime,
            isCompleted = _reminderCompleted.value,
            attachmentUri = _attachmentUri.value
        )
        repository.update(updatedReminder)
    }

    fun delete(reminder: Reminder) = viewModelScope.launch {
        repository.delete(reminder)
    }
}
