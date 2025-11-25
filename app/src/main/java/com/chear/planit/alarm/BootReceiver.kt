package com.chear.planit.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chear.planit.data.AppDatabase
import com.chear.planit.utils.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Solo nos interesa el evento de arranque completado
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                // Obtenemos una instancia de la base de datos
                val database = AppDatabase.getDatabase(context)
                val reminderDao = database.reminderDao()

                // Obtenemos todos los recordatorios pendientes
                val pendingReminders = reminderDao.getPendingReminders().first()

                // Volvemos a programar las alarmas para cada uno
                pendingReminders.forEach { reminder ->
                    if (reminder.dateTime > System.currentTimeMillis()) {
                        AlarmScheduler.schedule(
                            context = context,
                            reminderId = reminder.id,
                            triggerAtMillis = reminder.dateTime,
                            message = reminder.title
                        )
                    }
                }
            }
        }
    }
}
