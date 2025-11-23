package com.chear.planit.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Log para depuración. ¡Busca "ALARM_TEST" en el Logcat!
        Log.d("ALARM_TEST", "¡ALARMA RECIBIDA! ID: ${intent.getIntExtra("reminder_id", -1)}")

        val reminderId = intent.getIntExtra("reminder_id", -1)
        if (reminderId == -1) {
            Log.e("ALARM_TEST", "Error: No se recibió un ID de recordatorio válido.")
            return
        }

        val message = intent.getStringExtra("message") ?: "¡Tienes un recordatorio!"

        NotificationHelper.showNotification(
            context = context,
            reminderId = reminderId,
            title = "Recordatorio",
            message = message
        )
    }
}
