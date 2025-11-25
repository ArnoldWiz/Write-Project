package com.chear.planit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chear.planit.ui.components.ListElement

@Composable
fun RemindersScreen(
    reminderViewModel: ReminderViewModel,
    onReminderClick: (String) -> Unit
) {
    val reminders by reminderViewModel.reminders.collectAsState()
    val searchQuery by reminderViewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("RECORDATORIOS", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = reminderViewModel::onSearchQueryChange,
            label = { Text("Buscar recordatorios") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { reminderViewModel.onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (reminders.isEmpty()) {
                item {
                    Text("No hay recordatorios todavía", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                items(reminders, key = { it.id }) { reminder ->
                    ListElement(
                        note = reminder,
                        isReminder = true,
                        alHacerClick = { onReminderClick(reminder.id.toString()) },
                        onDeleteClick = { 
                            reminderViewModel.delete(reminder, context) 
                        },
                        onCheckedChange = { isChecked ->
                            reminderViewModel.loadReminder(reminder)
                            reminderViewModel.onCompletedChange(isChecked)
                            reminderViewModel.update(reminder, context)
                            reminderViewModel.clearReminderFields()
                        }
                    )
                }
            }
        }
    }
}
