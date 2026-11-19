package com.example.mahneshan.data.reminder

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mahneshan.R

class PeriodAlarmReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "period_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "یادآور شروع قاعدگی",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "اعلان‌های یادآوری ۲ روز قبل از شروع پریود"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("یادآوری ماه‌نشان 🌸")
            .setContentText("سلام همراه گرامی، طبق محاسبات، حدود ۲ روز تا موعد پیش‌بینی‌شده قاعدگی شما باقی مانده است.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("سلام همراه گرامی، طبق محاسبات برنامه ماه‌نشان، حدود ۲ روز تا موعد پیش‌بینی‌شده قاعدگی شما باقی مانده است. پیشنهاد می‌کنیم لوازم و مراقبت‌های لازم را آماده فرمایید.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
