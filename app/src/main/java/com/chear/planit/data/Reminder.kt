package com.chear.planit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dateTime: Long,
    val isCompleted: Boolean = false,
    val attachmentUris: List<String> = emptyList() // Ahora es una lista
)
