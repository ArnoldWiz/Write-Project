package com.chear.readit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.chear.readit.data.Libro
import com.chear.readit.ui.theme.ReadItTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReadItTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable(route = "login") {
            Login(
                onEnterClick = { navController.navigate("PantallaPerfil") }
            )
        }
        composable(route = "PantallaPerfil") {
            PantallaPerfil()
        }
    }
}

val lista = listOf(
    Libro("Cien años de soledad", "Gabriel García Márquez", "Por leer"),
    Libro("1984", "George Orwell", "Por leer"),
    Libro("El principito", "Antoine de Saint-Exupéry", "Por leer")
)

@Composable
fun Login(
    onEnterClick: () -> Unit,
    modifier: Modifier = Modifier){
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Usa el padding del Scaffold
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Read It", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Make It Easy", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onEnterClick) {
                Text("Entrar")
            }
        }
    }
}

@Composable
fun PantallaPerfil() {
    Text("Perfil", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
}

@Composable
fun PantallaListaLibros(libros: List<Libro>, modifier: Modifier = Modifier) {
    // Le pasamos el modifier para que respete el padding del Scaffold
    LazyColumn(modifier = modifier.fillMaxSize().padding(16.dp)) {
        items(libros) { libro ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = libro.titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Autor: ${libro.autor}", fontSize = 16.sp)
                    Text(text = "Estado: ${libro.estado}", fontSize = 14.sp, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
fun PantallaPorLeer() {
    Text("📖 Por leer", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
}

@Composable
fun PantallaEnProceso() {
    Text("📘 En proceso", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
}

@Composable
fun PantallaTerminados() {
    Text("✅ Terminados", fontSize = 24.sp, modifier = Modifier.padding(16.dp))
}


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    ReadItTheme {
        Login(onEnterClick = {})
    }
}

