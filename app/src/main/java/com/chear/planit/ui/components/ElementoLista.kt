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

@Composable
fun ListElement(
    note: Note,                      // ✅ Recibimos la nota
    isReminder: Boolean = false,
    alHacerClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    var checked by remember { mutableStateOf(false) }

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
                    text = note.title.ifBlank { "Sin título" }, // ✅ Mostrar título real
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = note.body.ifBlank { "Sin contenido" }, // ✅ Mostrar cuerpo real
                    style = MaterialTheme.typography.bodySmall
                )
            }

            onDeleteClick?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar")
                }
            }
        }
    }
}
