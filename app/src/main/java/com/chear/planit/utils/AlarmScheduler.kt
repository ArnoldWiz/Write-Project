package com.chear.planit.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.chear.planit.MainActivity
import com.chear.planit.alarm.AlarmReceiver
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AlarmScheduler {

    private const val ALARM_ACTION = "com.chear.planit.ALARM_TRIGGERED"
    private const val TAG = "AlarmScheduler"

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(context: Context, reminderId: Int, triggerAtMillis: Long, message: String) {
        val appContext = context.applicationContext
        val alarm = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(triggerAtMillis))
        Log.d(TAG, "Scheduling alarm for ID: $reminderId at $dateString (millis: $triggerAtMillis)")

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Trying to schedule alarm in the past. Ignoring exact alarm.")
        }

        // -- 1. Alarma para la Hora Exacta --
        val exactIntent = Intent(appContext, AlarmReceiver::class.java).apply {
            action = ALARM_ACTION
            putExtra("reminder_id", reminderId)
            putExtra("message", message)
            putExtra(AlarmReceiver.NOTIFICATION_TYPE_EXTRA, AlarmReceiver.TYPE_EXACT_TIME)
        }

         val exactPending = PendingIntent.getBroadcast(
            appContext,
            reminderId,
            exactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val viewIntent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val viewPending = PendingIntent.getActivity(
            appContext,
            reminderId,
            viewIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarm.canScheduleExactAlarms()) {
                Log.e(TAG, "Cannot schedule exact alarms. Permission missing. Falling back to inexact alarm.")
                alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, exactPending)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, viewPending)
                alarm.setAlarmClock(alarmClockInfo, exactPending)
                Log.d(TAG, "Alarm scheduled using setAlarmClock")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    exactPending
                )
                Log.d(TAG, "Alarm scheduled using setExactAndAllowWhileIdle")
            } else {
                alarm.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    exactPending
                )
                Log.d(TAG, "Alarm scheduled using setExact")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm", e)
            // Fallback for weird OEM restrictions
            alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, exactPending)
        }

        // -- 2. Alarma para el Resumen Diario --
        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerAtMillis
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis > System.currentTimeMillis()) {
            val dailyIntent = Intent(appContext, AlarmReceiver::class.java).apply {
                action = ALARM_ACTION
                putExtra("reminder_id", reminderId)
                putExtra("message", message)
                putExtra(AlarmReceiver.NOTIFICATION_TYPE_EXTRA, AlarmReceiver.TYPE_DAILY_SUMMARY)
            }

            val dailyPending = PendingIntent.getBroadcast(
                appContext,
                reminderId + 1000000, // Offset ID para diferenciar del exacto
                dailyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                // Daily summaries don't need to be `setAlarmClock`, a regular exact alarm is fine.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarm.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        dailyPending
                    )
                } else {
                    alarm.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        dailyPending
                    )
                }
                Log.d(TAG, "Daily summary alarm scheduled")
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException scheduling daily alarm", e)
                alarm.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, dailyPending)
            }
        }
    }

    fun cancel(context: Context, reminderId: Int) {
        Log.d(TAG, "Cancelling alarm for ID: $reminderId")
        val appContext = context.applicationContext
        val alarm = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancelar alarma exacta
        val exactIntent = Intent(appContext, AlarmReceiver::class.java).apply { action = ALARM_ACTION }
        val exactPending = PendingIntent.getBroadcast(
            appContext,
            reminderId,
            exactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(exactPending)
        exactPending.cancel()

        // Cancelar alarma diaria
        val dailyIntent = Intent(appContext, AlarmReceiver::class.java).apply { action = ALARM_ACTION }
        val dailyPending = PendingIntent.getBroadcast(
            appContext,
            reminderId + 1000000,
            dailyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(dailyPending)
        dailyPending.cancel()
    }
}
