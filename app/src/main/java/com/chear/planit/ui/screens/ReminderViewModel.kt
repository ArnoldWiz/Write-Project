package com.chear.planit.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chear.planit.data.Reminder
import com.chear.planit.data.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow

class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addReminder(reminder: Reminder) = viewModelScope.launch {
        repository.insert(reminder)
    }

    fun updateReminder(reminder: Reminder) = viewModelScope.launch {
        repository.update(reminder)
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        repository.delete(reminder)
    }
}
