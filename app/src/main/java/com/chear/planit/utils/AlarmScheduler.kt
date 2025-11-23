package com.chear.planit.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.chear.planit.alarm.AlarmReceiver

object AlarmScheduler {

    private const val ALARM_ACTION = "com.chear.planit.ALARM_TRIGGERED"

    fun schedule(context: Context, reminderId: Int, triggerAtMillis: Long, message: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ALARM_ACTION
            putExtra("reminder_id", reminderId)
            putExtra("message", message)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            reminderId, // Usar el ID del recordatorio como requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarm.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pending
        )
    }

    fun cancel(context: Context, reminderId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ALARM_ACTION
        }

        val pending = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pending)
    }
}
