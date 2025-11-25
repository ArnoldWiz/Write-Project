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
        // Usamos applicationContext para evitar referencias a contextos de Activity destruidos
        val appContext = context.applicationContext
        val alarm = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val dateString = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(triggerAtMillis))
        Log.d(TAG, "Scheduling alarm for ID: $reminderId at $dateString (millis: $triggerAtMillis)")

        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Trying to schedule alarm in the past. Ignoring exact alarm.")
            // No retornamos aquí porque aún podríamos querer programar el resumen diario si es futuro,
            // pero generalmente una alarma exacta en el pasado se debe disparar inmediatamente o ignorar.
        }

        // -- 1. Alarma para la Hora Exacta --
        val exactIntent = Intent(appContext, AlarmReceiver::class.java).apply {
            action = ALARM_ACTION
            putExtra("reminder_id", reminderId)
            putExtra("message", message)
            putExtra(AlarmReceiver.NOTIFICATION_TYPE_EXTRA, AlarmReceiver.TYPE_EXACT_TIME)
        }

        // Usamos FLAG_CANCEL_CURRENT para asegurar que cualquier pending intent viejo sea reemplazado
        val exactPending = PendingIntent.getBroadcast(
            appContext,
            reminderId, 
            exactIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
                Log.e(TAG, "Cannot schedule exact alarms. Permission missing.")
                // Aquí deberías notificar al usuario o degradar a alarma inexacta, 
                // pero idealmente la app ya pidió el permiso.
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
            alarm.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, exactPending)
        }

        // -- 2. Alarma para el Resumen Diario (a las 9 AM) --
        // ... (código existente para resumen diario)
    }

    fun cancel(context: Context, reminderId: Int) {
        Log.d(TAG, "Cancelling alarm for ID: $reminderId")
        val appContext = context.applicationContext
        val alarm = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val exactIntent = Intent(appContext, AlarmReceiver::class.java).apply { action = ALARM_ACTION }
        val exactPending = PendingIntent.getBroadcast(
            appContext,
            reminderId,
            exactIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarm.cancel(exactPending)
        exactPending.cancel() // Importante cancelar también el PendingIntent

        // Cancelar resumen diario...
    }
}
