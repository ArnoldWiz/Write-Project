package com.chear.planit.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.ui.platform.LocalGraphicsContext
import androidx.core.app.NotificationCompat
import com.chear.planit.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showNotification(
            context,
            "Recordatorio",
            intent.getStringExtra("message") ?: "¡Tienes un recordatorio!"
        )
    }
}
