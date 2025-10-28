package com.chear.planit.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chear.planit.data.Note
import com.chear.planit.data.Reminder
import com.chear.planit.data.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {


    var reminderTitle = mutableStateOf("")
        private set
    var reminderBody = mutableStateOf("")
        private set
    var reminderTimestamp = mutableLongStateOf(System.currentTimeMillis())
        private set
    var attachmentUri = mutableStateOf<String?>(null)
        private set
    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Edición
    fun onTitleChange(newTitle: String) {
        reminderTitle.value = newTitle
    }

    fun onBodyChange(newBody: String) {
        reminderBody.value = newBody
    }

    fun onTimestampChange(newTimestamp: Long) {
        reminderTimestamp.value = newTimestamp
    }

    fun onAttachmentChange(newUri: String?) {
        attachmentUri.value = newUri
    }

    // Cargar el recordatorio
    fun loadReminder(reminder: Reminder?) {
        if (reminder != null) {
            reminderTitle.value = reminder.title
            reminderBody.value = reminder.description
            reminderTimestamp.value = reminder.dateTime
            attachmentUri.value = reminder.attachmentUri
        } else {
            clearNoteFields()
        }
    }

    fun clearNoteFields() {
        reminderTitle.value = ""
        reminderBody.value = ""
        reminderTimestamp.value = System.currentTimeMillis()
        attachmentUri.value = null
    }

    fun addReminder() = viewModelScope.launch {
        if (reminderTitle.value.isNotBlank() || reminderBody.value.isNotBlank()) {
            repository.insert(
                Reminder(
                    title = reminderTitle.value,
                    description = reminderBody.value,
                    dateTime = reminderTimestamp.value,
                    attachmentUri = attachmentUri.value
                )
            )
            clearNoteFields()
        }
    }

    fun updateReminder(reminder: Reminder) = viewModelScope.launch {
        val updated = reminder.copy(
            title = reminderTitle.value,
            description = reminderBody.value,
            dateTime = reminderTimestamp.value,
            attachmentUri = attachmentUri.value
        )
        repository.update(updated)
        clearNoteFields()
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        repository.delete(reminder)
    }
}