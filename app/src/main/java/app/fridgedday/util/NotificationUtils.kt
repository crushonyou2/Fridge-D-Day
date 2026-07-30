package app.fridgedday.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import app.fridgedday.MainActivity
import app.fridgedday.R

object NotificationUtils {

    const val CHANNEL_EXPIRING = "expiring_soon"
    const val CHANNEL_EXPIRED = "expired"

    private const val NOTIFICATION_ID_EXPIRING = 1001
    private const val NOTIFICATION_ID_EXPIRED = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Expiring Soon Channel
            val expiringChannel = NotificationChannel(
                CHANNEL_EXPIRING,
                "임박 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "유통기한이 임박한 항목 알림"
            }

            // Expired Channel
            val expiredChannel = NotificationChannel(
                CHANNEL_EXPIRED,
                "만료 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "유통기한이 만료된 항목 알림"
            }

            notificationManager.createNotificationChannel(expiringChannel)
            notificationManager.createNotificationChannel(expiredChannel)
        }
    }

    fun sendExpiringSoonNotification(context: Context, count: Int) {
        if (count == 0 || !PermissionUtils.hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EXPIRING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("유통기한 임박")
            .setContentText("${count}개의 항목이 곧 만료됩니다")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_EXPIRING, notification)
    }

    fun sendExpiredNotification(context: Context, count: Int) {
        if (count == 0 || !PermissionUtils.hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_EXPIRED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("유통기한 만료")
            .setContentText("${count}개의 항목이 만료되었습니다")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_EXPIRED, notification)
    }
}
