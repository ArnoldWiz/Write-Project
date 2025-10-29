
package com.chear.planit.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chear.planit.R
import com.chear.planit.data.NoteRepository
import com.chear.planit.data.ReminderRepository
import com.chear.planit.ui.screens.*

object Ruts {
    const val NOTES_SCREEN = "notes"
    const val REMINDERS_SCREEN = "reminders"
    const val DETAIL_NOTE_SCREEN = "detail_note"
    const val DETAIL_REMINDER_SCREEN = "detail_reminder"
}

enum class NavigationType {
    BOTTOM_NAVIGATION, NAVIGATION_RAIL, PERMANENT_NAVIGATION_DRAWER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanItApp(
    noteRepository: NoteRepository,
    reminderRepository: ReminderRepository,
    windowSize: WindowSizeClass
){
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route ?: Ruts.NOTES_SCREEN

    val navigationType: NavigationType = when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> NavigationType.BOTTOM_NAVIGATION
        WindowWidthSizeClass.Medium -> NavigationType.NAVIGATION_RAIL
        WindowWidthSizeClass.Expanded -> NavigationType.PERMANENT_NAVIGATION_DRAWER
        else -> NavigationType.BOTTOM_NAVIGATION
    }

    val isMainScreen =
        rutaActual == Ruts.NOTES_SCREEN || rutaActual == Ruts.REMINDERS_SCREEN

    val noteViewModelFactory = NoteViewModelFactory(noteRepository)
    val reminderViewModelFactory = ReminderViewModelFactory(reminderRepository)

    if (navigationType == NavigationType.PERMANENT_NAVIGATION_DRAWER) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(Modifier.width(240.dp)) {
                    PlanItNavDrawerContent(
                        selectedDestination = rutaActual,
                        onTabPressed = { route -> navController.navigate(route) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ) {
            PlanItAppContent(
                navController = navController,
                rutaActual = rutaActual,
                isMainScreen = isMainScreen,
                navigationType = navigationType,
                noteViewModelFactory = noteViewModelFactory,
                reminderViewModelFactory = reminderViewModelFactory
            )
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            if (navigationType == NavigationType.NAVIGATION_RAIL) {
                PlanItNavigationRail(
                    selectedDestination = rutaActual,
                    onTabPressed = { route -> navController.navigate(route) }
                )
            }
            PlanItAppContent(
                navController = navController,
                rutaActual = rutaActual,
                isMainScreen = isMainScreen,
                navigationType = navigationType,
                noteViewModelFactory = noteViewModelFactory,
                reminderViewModelFactory = reminderViewModelFactory
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanItAppContent(
    navController: NavHostController,
    rutaActual: String,
    isMainScreen: Boolean,
    navigationType: NavigationType,
    noteViewModelFactory: NoteViewModelFactory,
    reminderViewModelFactory: ReminderViewModelFactory
) {
    Scaffold(
        topBar = {
            if (isMainScreen) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
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
                    Icon(Icons.Filled.Add, contentDescription = "Añadir")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            if (isMainScreen && navigationType == NavigationType.BOTTOM_NAVIGATION) {
                PlanItBottomNavigationBar(
                    selectedDestination = rutaActual,
                    onTabPressed = { route -> navController.navigate(route) }
                )
            }
        }
    ) { paddingInterno ->
        NavHost(
            navController = navController,
            startDestination = Ruts.NOTES_SCREEN,
            modifier = Modifier.padding(paddingInterno)
        ) {
            composable(Ruts.NOTES_SCREEN) {
                val noteViewModel: NoteViewModel = viewModel(factory = noteViewModelFactory)
                NotesScreen(
                    noteViewModel = noteViewModel,
                    onNoteClick = { idNota ->
                        navController.navigate("${Ruts.DETAIL_NOTE_SCREEN}/$idNota")
                    }
                )
            }
            composable(Ruts.REMINDERS_SCREEN) {
                val reminderViewModel: ReminderViewModel = viewModel(factory = reminderViewModelFactory)
                RemindersScreen(
                    reminderViewModel = reminderViewModel,
                    onReminderClick = { idRecordatorio ->
                        navController.navigate("${Ruts.DETAIL_REMINDER_SCREEN}/$idRecordatorio")
                    }
                )
            }
            composable(Ruts.DETAIL_NOTE_SCREEN) {
                val noteViewModel: NoteViewModel = viewModel(factory = noteViewModelFactory)
                NoteDetailScreen(
                    noteId = null,
                    onNavigateBack = { navController.popBackStack() },
                    noteViewModel = noteViewModel
                )
            }
            composable(
                route = "${Ruts.DETAIL_NOTE_SCREEN}/{idNota}",
                arguments = listOf(navArgument("idNota") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val idNota = backStackEntry.arguments?.getString("idNota")
                val noteViewModel: NoteViewModel = viewModel(factory = noteViewModelFactory)
                NoteDetailScreen(
                    noteId = idNota,
                    onNavigateBack = { navController.popBackStack() },
                    noteViewModel = noteViewModel
                )
            }
            composable(Ruts.DETAIL_REMINDER_SCREEN) {
                val reminderViewModel: ReminderViewModel = viewModel(factory = reminderViewModelFactory)
                ReminderDetailScreen(
                    reminderId = null,
                    onNavigateBack = { navController.popBackStack() },
                    reminderViewModel = reminderViewModel
                )
            }
            composable(
                route = "${Ruts.DETAIL_REMINDER_SCREEN}/{idRecordatorio}",
                arguments = listOf(navArgument("idRecordatorio") {
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val idRecordatorio = backStackEntry.arguments?.getString("idRecordatorio")
                val reminderViewModel: ReminderViewModel = viewModel(factory = reminderViewModelFactory)
                ReminderDetailScreen(
                    reminderId = idRecordatorio,
                    onNavigateBack = { navController.popBackStack() },
                    reminderViewModel = reminderViewModel
                )
            }
        }
    }
}

@Composable
fun PlanItBottomNavigationBar(
    selectedDestination: String,
    onTabPressed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BottomAppBar(containerColor = MaterialTheme.colorScheme.surface, modifier = modifier) {
        NavigationBarItem(
            selected = selectedDestination == Ruts.NOTES_SCREEN,
            onClick = { onTabPressed(Ruts.NOTES_SCREEN) },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Notas") },
            label = { Text("Notas") }
        )
        NavigationBarItem(
            selected = selectedDestination == Ruts.REMINDERS_SCREEN,
            onClick = { onTabPressed(Ruts.REMINDERS_SCREEN) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Recordatorios") },
            label = { Text("Recordatorios") }
        )
    }
}

@Composable
fun PlanItNavigationRail(
    selectedDestination: String,
    onTabPressed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier) {
        NavigationRailItem(
            selected = selectedDestination == Ruts.NOTES_SCREEN,
            onClick = { onTabPressed(Ruts.NOTES_SCREEN) },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Notas") },
            label = { Text("Notas") }
        )
        NavigationRailItem(
            selected = selectedDestination == Ruts.REMINDERS_SCREEN,
            onClick = { onTabPressed(Ruts.REMINDERS_SCREEN) },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Recordatorios") },
            label = { Text("Recordatorios") }
        )
    }
}

@Composable
fun PlanItNavDrawerContent(
    selectedDestination: String,
    onTabPressed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        NavigationDrawerItem(
            selected = selectedDestination == Ruts.NOTES_SCREEN,
            label = { Text("Notas") },
            icon = { Icon(Icons.Default.Edit, contentDescription = "Notas") },
            onClick = { onTabPressed(Ruts.NOTES_SCREEN) }
        )
        NavigationDrawerItem(
            selected = selectedDestination == Ruts.REMINDERS_SCREEN,
            label = { Text("Recordatorios") },
            icon = { Icon(Icons.Default.DateRange, contentDescription = "Recordatorios") },
            onClick = { onTabPressed(Ruts.REMINDERS_SCREEN) }
        )
    }
}
