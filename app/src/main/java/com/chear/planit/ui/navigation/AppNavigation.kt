package com.chear.planit.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chear.planit.ui.screens.NoteDetailScreen
import com.chear.planit.ui.screens.ReminderDetailScreen
import com.chear.planit.ui.screens.NotesScreen
import com.chear.planit.ui.screens.RemindersScreen

object Ruts {
    const val NOTES_SCREEN = "notes"
    const val REMINDERS_SCREEN = "reminders"
    const val DETAIL_NOTE_SCREEN = "detail_note"
    const val DETAIL_REMINDER_SCREEN = "detail_reminder"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanItApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    val isMainScreen = rutaActual == Ruts.NOTES_SCREEN || rutaActual == Ruts.REMINDERS_SCREEN

    Scaffold(
        topBar = {
            if (isMainScreen) {
                TopAppBar(
                    title = {
                        Text(
                            text = "PlanIt",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Left,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            }
        },
        floatingActionButton = {
            if (isMainScreen) {
                FloatingActionButton(
                    onClick = {
                        if (rutaActual == Ruts.NOTES_SCREEN) {
                            navController.navigate(Ruts.DETAIL_NOTE_SCREEN)
                        } else {
                            navController.navigate(Ruts.DETAIL_REMINDER_SCREEN)
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) {
                    Icon(Icons.Filled.Add, "Añadir")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            if (isMainScreen) {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    NavigationBarItem(
                        selected = rutaActual == Ruts.NOTES_SCREEN,
                        onClick = { navController.navigate(Ruts.NOTES_SCREEN) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Notas") },
                        label = { Text("Notas") }
                    )
                    NavigationBarItem(
                        selected = rutaActual == Ruts.REMINDERS_SCREEN,
                        onClick = { navController.navigate(Ruts.REMINDERS_SCREEN) },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Recordatorios") },
                        label = { Text("Recordatorios") }
                    )
                }
            }
        }
    ) { paddingInterno ->
        NavHost(
            navController = navController,
            startDestination = Ruts.NOTES_SCREEN,
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable(Ruts.NOTES_SCREEN) {
                NotesScreen(
                    onNoteClick = { idDeLaNota ->
                        navController.navigate("${Ruts.DETAIL_NOTE_SCREEN}/$idDeLaNota")
                    }
                )
            }
            composable(Ruts.REMINDERS_SCREEN) {
                RemindersScreen(
                    onReminderClick = { idDelRecordatorio ->
                        navController.navigate("${Ruts.DETAIL_REMINDER_SCREEN}/$idDelRecordatorio")
                    }
                )
            }
            composable(
                route = "${Ruts.DETAIL_NOTE_SCREEN}/{idNota}",
                arguments = listOf(navArgument("idNota") { type = NavType.StringType; nullable = true })
            ) { navBackStackEntry ->
                val idNota = navBackStackEntry.arguments?.getString("idNota")
                NoteDetailScreen(noteId = idNota, onNavigateBack = { navController.popBackStack() })
            }
            composable(Ruts.DETAIL_NOTE_SCREEN) {
                NoteDetailScreen(noteId = null, onNavigateBack = { navController.popBackStack() })
            }
            composable(
                route = "${Ruts.DETAIL_REMINDER_SCREEN}/{idRecordatorio}",
                arguments = listOf(navArgument("idRecordatorio") { type = NavType.StringType; nullable = true })
            ) { navBackStackEntry ->
                val idRecordatorio = navBackStackEntry.arguments?.getString("idRecordatorio")
                ReminderDetailScreen(reminderId = idRecordatorio, onNavigateBack = { navController.popBackStack() })
            }
            composable(Ruts.DETAIL_REMINDER_SCREEN) {
                ReminderDetailScreen(reminderId = null, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}