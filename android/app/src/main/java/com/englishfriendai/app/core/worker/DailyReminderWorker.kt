package com.englishfriendai.app.core.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.englishfriendai.app.MainActivity
import com.englishfriendai.app.R
import com.englishfriendai.app.core.util.Constants
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Posts a local "come practice today" notification. Scheduled by SettingsViewModel via
 * WorkManager's PeriodicWorkRequest (once-daily) when the user enables reminders.
 */
@HiltWorker
class DailyReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        createNotificationChannel()
        showNotification()
        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_REMINDERS,
                applicationContext.getString(R.string.daily_reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val launchIntent = androidx.core.app.TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(
                android.content.Intent(applicationContext, MainActivity::class.java)
            )
            getPendingIntent(
                0,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(applicationContext.getString(R.string.daily_reminder_title))
            .setContentText(applicationContext.getString(R.string.daily_reminder_text))
            .setAutoCancel(true)
            .setContentIntent(launchIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        androidx.core.app.NotificationManagerCompat.from(applicationContext)
            .notify(Constants.NOTIFICATION_ID_DAILY_REMINDER, notification)
    }
}
