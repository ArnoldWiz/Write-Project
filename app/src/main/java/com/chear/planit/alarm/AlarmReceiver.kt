package com.chear.planit.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.chear.planit.MainActivity
import com.chear.planit.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "reminders_channel"
        const val NOTIFICATION_TYPE_EXTRA = "notification_type"
        const val TYPE_DAILY_SUMMARY = "daily_summary"
        const val TYPE_EXACT_TIME = "exact_time"
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive triggered. Action: ${intent.action}")

        // Asegurar que el canal exista
        createNotificationChannel(context)

        val reminderId = intent.getIntExtra("reminder_id", 0)
        val message = intent.getStringExtra("message") ?: "Recordatorio"
        val notificationType = intent.getStringExtra(NOTIFICATION_TYPE_EXTRA)

        Log.d(TAG, "Processing reminder: ID=$reminderId, Type=$notificationType, Message=$message")

        // Personalización del Título y Texto de la notificación
        val contentTitle: String
        val contentText: String

        when (notificationType) {
            TYPE_EXACT_TIME -> {
                contentTitle = "¡ES HORA!"
                contentText = message // Aquí va el nombre del recordatorio
            }
            TYPE_DAILY_SUMMARY -> {
                contentTitle = "Recordatorio Diario"
                contentText = "Tienes pendiente: $message"
            }
            else -> {
                contentTitle = "Plan It"
                contentText = message
            }
        }

        // Intent para abrir la app al tocar la notificación
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle) // Usamos el título personalizado
            .setContentText(contentText)   // Usamos el mensaje personalizado
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)

        // Comprobar permiso antes de mostrar
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                with(NotificationManagerCompat.from(context)) {
                    val notificationId = if (notificationType == TYPE_DAILY_SUMMARY) reminderId + 1000000 else reminderId
                    notify(notificationId, builder.build())
                    Log.d(TAG, "Notification posted: ID=$notificationId, Title=$contentTitle")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error posting notification", e)
            }
        } else {
            Log.w(TAG, "Permission POST_NOTIFICATIONS not granted. Cannot show notification.")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Plan It"
            val descriptionText = "Canal para las notificaciones de recordatorios"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
