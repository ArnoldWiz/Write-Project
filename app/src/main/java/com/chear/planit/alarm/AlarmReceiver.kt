package com.chear.planit.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chear.planit.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "reminders_channel"
        const val NOTIFICATION_TYPE_EXTRA = "notification_type"
        const val TYPE_DAILY_SUMMARY = "daily_summary"
        const val TYPE_EXACT_TIME = "exact_time"
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)

        val reminderId = intent.getIntExtra("reminder_id", 0)
        val message = intent.getStringExtra("message") ?: "Recordatorio"
        val notificationType = intent.getStringExtra(NOTIFICATION_TYPE_EXTRA)

        val notificationMessage = when (notificationType) {
            TYPE_DAILY_SUMMARY -> "Hoy tienes pendiente: $message"
            TYPE_EXACT_TIME -> "¡Es ahora! $message"
            else -> message
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener este icono
            .setContentTitle("Plan It - Recordatorio")
            .setContentText(notificationMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Comprobar permiso antes de mostrar
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(context)) {
                // El ID de la notificación es único para cada recordatorio y tipo
                val notificationId = if (notificationType == TYPE_DAILY_SUMMARY) reminderId + 1000000 else reminderId
                notify(notificationId, builder.build())
            }
        } 
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Recordatorios de Plan It"
        val descriptionText = "Canal para las notificaciones de recordatorios"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
