package com.chear.planit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chear.planit.data.Note
import com.chear.planit.data.Reminder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ListElement(
    note: Any,
    isReminder: Boolean = false,
    alHacerClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    var checked by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val title: String
    val body: String
    var dateTime: Long? = null

    when (note) {
        is Note -> {
            title = note.title
            body = note.body
        }
        is Reminder -> {
            title = note.title
            body = note.description
            dateTime = note.dateTime
        }
        else -> {
            title = "Elemento desconocido"
            body = ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = alHacerClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (isReminder) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = title.ifBlank { "Sin título" },
                    fontWeight = FontWeight.Bold
                )

                if (isReminder && dateTime != null) {
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = dateTime!!
                    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
                    Text(
                        text = dateFormatter.format(calendar.time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = body.ifBlank { "Sin contenido" },
                    style = MaterialTheme.typography.bodySmall
                )
            }

            onDeleteClick?.let {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este elemento?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick?.invoke()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
