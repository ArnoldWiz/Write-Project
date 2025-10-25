package com.chear.planit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.room.Room
import com.chear.planit.ui.navigation.PlanItApp
import com.chear.planit.ui.screens.NoteDetailScreen
import com.chear.planit.ui.screens.ReminderDetailScreen
import com.chear.planit.ui.theme.AppTheme
import com.chear.planit.data.AppDatabase
import com.chear.planit.data.NoteRepository
import com.chear.planit.data.ReminderRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val noteRepository = NoteRepository(db.noteDao())
        val reminderRepository = ReminderRepository(db.reminderDao())

        setContent {
            AppTheme {
                PlanItApp(noteRepository, reminderRepository)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun VistaPreviaPlanIt() {
    AppTheme {
        androidx.compose.material3.Text(
            text = androidx.compose.ui.res.stringResource(id = R.string.welcome_message)
        )
    }
}


//@Preview(showBackground = true)
//@Composable
//fun VistaPreviaPantallaDetalleNota() {
  //  val dummyViewModel = remember { NoteViewModel(noteRepository = null) }

    //AppTheme {
      //  NoteDetailScreen(
        //    noteId = null,
           // onNavigateBack = {},
            //noteViewModel = dummyViewModel
        //)
    //}
//}

/*@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaDetalleRecordatorio() {
    AppTheme {
        ReminderDetailScreen(reminderId = null, onNavigateBack = {})
    }
}
*/