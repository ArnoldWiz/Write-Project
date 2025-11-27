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
    val additionalDates: List<Long> = emptyList(),
    val isCompleted: Boolean = false,
    val attachments: List<Attachment> = emptyList() // Cambiado de attachmentUris a attachments
)
