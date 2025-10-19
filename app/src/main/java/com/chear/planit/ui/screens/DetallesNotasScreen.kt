package com.chear.planit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalleNota(idDeLaNota: String?, alNavegarAtras: () -> Unit) {
    val editando = idDeLaNota != null
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editando) "Editar Nota" else "Crear Nota") },
                navigationIcon = {
                    IconButton(onClick = alNavegarAtras) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    Button(onClick = alNavegarAtras) {
                        Text("Guardar")
                    }
                }
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val titulo = if (editando) "Título de la nota $idDeLaNota" else ""
            val cuerpo = if (editando) "Contenido de la nota $idDeLaNota..." else ""

            OutlinedTextField(
                value = titulo,
                onValueChange = {},
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = cuerpo,
                onValueChange = {},
                label = { Text("Cuerpo de la nota...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Button(onClick = {  }) {
                Icon(Icons.Default.Add, contentDescription = "Adjuntar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Adjuntar Archivo")
            }
        }
    }
}