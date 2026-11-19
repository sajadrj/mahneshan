package com.example.mahneshan.data.reminder

import android.R
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MedicationAlarmReceiver : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("MEDICATION_NAME") ?: "دارو"
        val medicationId = intent.getLongExtra("MEDICATION_ID", 0L)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "medication_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "یادآور دارو و مکمل",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های زمان مصرف دارو و مکمل‌ها"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("زمان مصرف دارو")
            .setContentText("وقت خوردن دارو/مکمل «$medicationName» است.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medicationId.toInt(), notification)
    }
}
