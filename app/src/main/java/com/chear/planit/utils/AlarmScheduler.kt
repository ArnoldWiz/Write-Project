package com.chear.planit.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.chear.planit.alarm.AlarmReceiver

object AlarmScheduler {

    fun schedule(context: Context, triggerAtMillis: Long, message: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.PLANIT.ALARM_TRIGGERED"
            putExtra("message", message)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
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
}
