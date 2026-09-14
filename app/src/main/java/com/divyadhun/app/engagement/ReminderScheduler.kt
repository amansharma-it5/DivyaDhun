package com.divyadhun.app.engagement

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.divyadhun.app.MainActivity
import com.divyadhun.app.R
import com.divyadhun.app.domain.model.ListeningTime
import java.time.ZonedDateTime

object DailyReminderSchedule {
    const val REQUEST_CODE = 4201

    fun hourFor(time: ListeningTime): Int = when (time) {
        ListeningTime.MORNING -> 7
        ListeningTime.EVENING -> 18
        ListeningTime.BEFORE_SLEEP -> 21
        ListeningTime.ANYTIME -> 8
    }

    fun nextTriggerMillis(now: ZonedDateTime, time: ListeningTime): Long {
        val candidate = now.withHour(hourFor(time)).withMinute(0).withSecond(0).withNano(0)
        return (if (candidate.isAfter(now)) candidate else candidate.plusDays(1)).toInstant().toEpochMilli()
    }
}

/** Optional, inexact daily reminder. It never schedules anything until the user opts in. */
object ReminderScheduler {
    const val CHANNEL_ID = "daily_bhakti_reminders"
    private const val ACTION = "com.divyadhun.app.action.DAILY_BHAKTI_REMINDER"

    fun schedule(context: Context, listeningTime: ListeningTime) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val pendingIntent = pendingIntent(context)
        val trigger = DailyReminderSchedule.nextTriggerMillis(ZonedDateTime.now(), listeningTime)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        DailyReminderSchedule.REQUEST_CODE,
        Intent(context, DailyReminderReceiver::class.java).setAction(ACTION),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(NotificationChannel(ReminderScheduler.CHANNEL_ID, context.getString(R.string.reminder_channel_name), NotificationManager.IMPORTANCE_DEFAULT))
        val openApp = PendingIntent.getActivity(
            context,
            DailyReminderSchedule.REQUEST_CODE + 1,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = android.app.Notification.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_brand)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(context.getString(R.string.reminder_notification_body))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        manager.notify(DailyReminderSchedule.REQUEST_CODE, notification)
    }
}
