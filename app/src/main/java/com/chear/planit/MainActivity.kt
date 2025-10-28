package com.chear.planit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.chear.planit.data.AppDatabase
import com.chear.planit.data.NoteRepository
import com.chear.planit.data.ReminderRepository
import com.chear.planit.ui.navigation.PlanItApp
import com.chear.planit.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val noteRepository = NoteRepository(db.noteDao())
        val reminderRepository = ReminderRepository(db.reminderDao())

        setContent {
            AppTheme {
                val windowSize = calculateWindowSizeClass(this)
                PlanItApp( noteRepository = noteRepository,
                    reminderRepository = reminderRepository,
                    windowSize = windowSize)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewPlanIt() {
    AppTheme {
        androidx.compose.material3.Text(
            text = androidx.compose.ui.res.stringResource(id = R.string.welcome_message)
        )
    }
}
