package com.chear.planit.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chear.planit.data.Reminder
import com.chear.planit.data.ReminderRepository
import com.chear.planit.utils.AlarmScheduler
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ReminderViewModel(private val repository: ReminderRepository) : ViewModel() {

    private val TAG = "PLANIT_DEBUG"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _reminders = repository.getAll()
    val reminders: StateFlow<List<Reminder>> = searchQuery
        .debounce(500)
        .combine(_reminders) { query, reminders ->
            if (query.isBlank()) {
                reminders
            } else {
                reminders.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            (it.description?.contains(query, ignoreCase = true) ?: false)
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
    private val _reminderTitle = mutableStateOf("")
    val reminderTitle: State<String> = _reminderTitle

    private val _reminderDescription = mutableStateOf("")
    val reminderDescription: State<String> = _reminderDescription

    private val _reminderDateTime = mutableStateOf<Long?>(null)
    val reminderDateTime: State<Long?> = _reminderDateTime

    private val _reminderCompleted = mutableStateOf(false)
    val reminderCompleted: State<Boolean> = _reminderCompleted

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

    fun addReminder(context: Context) = viewModelScope.launch {
        Log.d(TAG, "Iniciando guardado de recordatorio: ${_reminderTitle.value}")
        
        val finalDateTime = _reminderDateTime.value ?: System.currentTimeMillis()
        
        val newReminder = Reminder(
            title = _reminderTitle.value,
            description = _reminderDescription.value,
            dateTime = finalDateTime,
            isCompleted = _reminderCompleted.value,
            attachmentUris = _attachmentUris.value
        )
        val newId = repository.insert(newReminder)
        Log.d(TAG, "Recordatorio guardado en BD con ID: $newId y fecha: $finalDateTime")

        if (!newReminder.isCompleted) {
            val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(finalDateTime))
            val currentTime = System.currentTimeMillis()
            Log.d(TAG, "Fecha programada: $formattedTime ($finalDateTime). Actual: $currentTime")
            
            Log.d(TAG, "Llamando a AlarmScheduler para ID: $newId")
            
            AlarmScheduler.schedule(
                context = context,
                reminderId = newId.toInt(),
                triggerAtMillis = finalDateTime,
                message = _reminderTitle.value
            )
        } else {
            Log.d(TAG, "Recordatorio completado, no se programa alarma para ID: $newId")
        }
    }

    fun update(reminderToUpdate: Reminder, context: Context) = viewModelScope.launch {
        Log.d(TAG, "Actualizando recordatorio ID: ${reminderToUpdate.id}")
        
        val finalDateTime = _reminderDateTime.value ?: reminderToUpdate.dateTime
        
        val updatedReminder = reminderToUpdate.copy(
            title = _reminderTitle.value,
            description = _reminderDescription.value,
            dateTime = finalDateTime,
            isCompleted = _reminderCompleted.value,
            attachmentUris = _attachmentUris.value
        )
        repository.update(updatedReminder)

        if (updatedReminder.isCompleted) {
            Log.d(TAG, "Recordatorio completado, cancelando alarma para ID: ${updatedReminder.id}")
            AlarmScheduler.cancel(context, updatedReminder.id)
        } else {
            val formattedTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(finalDateTime))
            Log.d(TAG, "Reprogramando alarma para ID: ${updatedReminder.id} a las: $formattedTime")

            AlarmScheduler.schedule(
                context = context,
                reminderId = updatedReminder.id,
                triggerAtMillis = finalDateTime,
                message = _reminderTitle.value
            )
        }
    }

    fun delete(reminder: Reminder, context: Context) = viewModelScope.launch {
        Log.d(TAG, "Eliminando recordatorio ID: ${reminder.id}")
        repository.delete(reminder)
        AlarmScheduler.cancel(context, reminder.id)
    }
}
