package com.example.mahneshan.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.util.Calendar

object PeriodReminderManager {

    private const val REQUEST_CODE = 9001

    fun scheduleReminder2DaysBefore(context: Context, nextPeriodEpochDay: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PeriodAlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // محاسبه روز یادآوری: ۲ روز قبل از شروع موعد پریود
        val reminderEpochDay = nextPeriodEpochDay - 2
        val targetDate = LocalDate.ofEpochDay(reminderEpochDay)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, targetDate.year)
            set(Calendar.MONTH, targetDate.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, targetDate.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, 9) // ساعت ۹ صبح
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // اگر زمان گذشته باشد، تنظیم نمی‌کنیم
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PeriodAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
