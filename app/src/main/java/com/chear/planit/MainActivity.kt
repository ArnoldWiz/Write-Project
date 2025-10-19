package com.chear.planit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.chear.planit.ui.screens.PantallaDetalleNota
import com.chear.planit.ui.screens.PantallaDetalleRecordatorio
import com.chear.planit.ui.theme.AppTheme
import com.chear.planit.ui.navigation.PlanItApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                PlanItApp()
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun VistaPreviaApp() {
    AppTheme {
        PlanItApp()
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaDetalleNota() {
    AppTheme {
        PantallaDetalleNota(idDeLaNota = "123", alNavegarAtras = {})
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaDetalleRecordatorio() {
    AppTheme {
        PantallaDetalleRecordatorio (idDelRecordatorio = null, alNavegarAtras = {})
    }
}
