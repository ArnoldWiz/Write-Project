package com.chear.planit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val body: String,
    val date: Long = System.currentTimeMillis(),
    val attachments: List<Attachment> = emptyList() // Cambiado de attachmentUris a attachments
)
