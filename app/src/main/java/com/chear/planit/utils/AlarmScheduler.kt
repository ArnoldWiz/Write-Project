package com.chear.planit.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.chear.planit.alarm.AlarmReceiver
import java.util.Calendar

object AlarmScheduler {

    private const val ALARM_ACTION = "com.chear.planit.ALARM_TRIGGERED"

    fun schedule(context: Context, reminderId: Int, triggerAtMillis: Long, message: String) {
        // -- 1. Alarma para la Hora Exacta --
        val exactIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ALARM_ACTION
            putExtra("reminder_id", reminderId)
            putExtra("message", message)
            putExtra(AlarmReceiver.NOTIFICATION_TYPE_EXTRA, AlarmReceiver.TYPE_EXACT_TIME)
        }

        val exactPending = PendingIntent.getBroadcast(
            context,
            reminderId, // ID único para la alarma exacta
            exactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Programamos la alarma exacta
        alarm.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            exactPending
        )

        // -- 2. Alarma para el Resumen Diario (a las 9 AM) --
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerAtMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Si la hora de las 9 AM ya pasó hoy, no la programamos
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            val dailyIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = ALARM_ACTION
                putExtra("reminder_id", reminderId)
                putExtra("message", message)
                putExtra(AlarmReceiver.NOTIFICATION_TYPE_EXTRA, AlarmReceiver.TYPE_DAILY_SUMMARY)
            }

            val dailyPending = PendingIntent.getBroadcast(
                context,
                reminderId + 1000000, // ID único diferente para la alarma diaria
                dailyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarm.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                dailyPending
            )
        }
    }

    fun cancel(context: Context, reminderId: Int) {
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancelar alarma exacta
        val exactIntent = Intent(context, AlarmReceiver::class.java).apply { action = ALARM_ACTION }
        val exactPending = PendingIntent.getBroadcast(
            context,
            reminderId,
            exactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(exactPending)

        // Cancelar alarma diaria
        val dailyIntent = Intent(context, AlarmReceiver::class.java).apply { action = ALARM_ACTION }
        val dailyPending = PendingIntent.getBroadcast(
            context,
            reminderId + 1000000,
            dailyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(dailyPending)
    }
}
