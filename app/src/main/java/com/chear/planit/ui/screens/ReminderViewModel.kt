package com.chear.planit.ui.screens

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

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var reminderTitle = mutableStateOf("")
        private set
    var reminderDescription = mutableStateOf("")
        private set
    var reminderDateTime = mutableStateOf<Long?>(null)
        private set
    var reminderCompleted = mutableStateOf(false)
        private set
    var attachmentUri = mutableStateOf<String?>(null)
        private set

    fun onTitleChange(newTitle: String) {
        reminderTitle.value = newTitle
    }

    fun onDescriptionChange(newDescription: String) {
        reminderDescription.value = newDescription
    }

    fun onDateTimeChange(newDateTime: Long?) {
        reminderDateTime.value = newDateTime
    }

    fun onCompletedChange(isCompleted: Boolean) {
        reminderCompleted.value = isCompleted
    }

    fun onAttachmentChange(newUri: String?) {
        attachmentUri.value = newUri
    }

    fun addReminder() = viewModelScope.launch {
        if (reminderTitle.value.isNotBlank() || reminderDescription.value.isNotBlank()) {
            repository.insert(
                Reminder(
                    title = reminderTitle.value,
                    description = reminderDescription.value,
                    dateTime = reminderDateTime.value ?: System.currentTimeMillis(),
                    isCompleted = reminderCompleted.value,
                    attachmentUri = attachmentUri.value
                )
            )
            clearReminderFields()
        }
    }

    fun update(existingReminder: Reminder) = viewModelScope.launch {
        val updated = existingReminder.copy(
            title = reminderTitle.value,
            description = reminderDescription.value,
            dateTime = reminderDateTime.value ?: existingReminder.dateTime,
            isCompleted = reminderCompleted.value,
            attachmentUri = attachmentUri.value
        )
        repository.update(updated)
        clearReminderFields()
    }

    fun delete(reminder: Reminder) = viewModelScope.launch {
        repository.delete(reminder)
    }

    fun loadReminder(reminder: Reminder?) {
        if (reminder != null) {
            reminderTitle.value = reminder.title
            reminderDescription.value = reminder.description
            reminderDateTime.value = reminder.dateTime
            reminderCompleted.value = reminder.isCompleted
            attachmentUri.value = reminder.attachmentUri
        } else {
            clearReminderFields()
        }
    }

    fun clearReminderFields() {
        reminderTitle.value = ""
        reminderDescription.value = ""
        reminderDateTime.value = null
        reminderCompleted.value = false
        attachmentUri.value = null
    }
}
