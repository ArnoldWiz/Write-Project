package com.chear.planit

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.chear.planit.alarm.AlarmReceiver
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

        createNotificationChannel()
        checkAndRequestPermissions()

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
        // Crear el canal de notificaciones para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Plan It"
            val descriptionText = "Canal para las notificaciones de recordatorios"
            val importance = NotificationManager.IMPORTANCE_HIGH
            // Usamos el MISMO ID que en AlarmReceiver
            val channel = NotificationChannel(AlarmReceiver.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        // Permisos multimedia y cámara
        permissionsToRequest.add(Manifest.permission.CAMERA)
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            // Permiso de notificaciones para Android 13+
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val notGrantedPermissions = permissionsToRequest.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                notGrantedPermissions.toTypedArray(),
                100
            )
        }

        // Permiso especial para Alarmas Exactas (Android 12+)
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
