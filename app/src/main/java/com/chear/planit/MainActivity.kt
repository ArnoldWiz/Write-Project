package com.chear.planit

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
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

        // Crear canal de notificaciones
        createNotificationChannel()

        // Pedir permisos necesarios
        requestNotificationPermission()
        requestExactAlarmPermission()

        val db = AppDatabase.getDatabase(applicationContext)
        val noteRepository = NoteRepository(db.noteDao())
        val reminderRepository = ReminderRepository(db.reminderDao())

        setContent {
            AppTheme {
                val windowSize = calculateWindowSizeClass(this)
                PlanItApp(
                    noteRepository = noteRepository,
                    reminderRepository = reminderRepository,
                    windowSize = windowSize
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "alarm_channel",
                "Alarmas",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                200
            )
        }
    }

    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewPlanIt() {
    AppTheme {
        androidx.compose.material3.Text("Preview")
    }
}
