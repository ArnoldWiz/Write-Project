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
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Usamos goAsync() porque vamos a realizar operaciones de base de datos (IO)
            // Esto evita que el sistema mate el proceso antes de terminar la reprogramación
            val pendingResult = goAsync()
            val scope = CoroutineScope(Dispatchers.IO)
            
            scope.launch {
                try {
                    val database = AppDatabase.getDatabase(context)
                    val reminderDao = database.reminderDao()

                    // Obtenemos los recordatorios pendientes de la base de datos
                    val pendingReminders = reminderDao.getPendingReminders().first()

                    // Volvemos a programar las alarmas
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
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    // Es CRÍTICO llamar a finish() para indicar que el receiver ha terminado
                    pendingResult.finish()
                }
            }
        }
    }
}
