package app.fridgedday.worker

import android.content.Context
import androidx.work.*
import app.fridgedday.data.pref.AppSettings
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object WorkScheduler {

    private const val WORK_NAME = "daily_expiry_check"

    fun scheduleDailyCheck(context: Context, settings: AppSettings) {
        val now = LocalDateTime.now()
        val delay = calculateInitialDelay(now, settings.notifyTime)

        val workRequest = OneTimeWorkRequestBuilder<ExpiryCheckWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelDailyCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    internal fun calculateInitialDelay(
        now: LocalDateTime,
        notifyTime: LocalTime
    ): Duration {
        val targetTime = now
            .withHour(notifyTime.hour)
            .withMinute(notifyTime.minute)
            .withSecond(0)
            .withNano(0)
        val nextRun = if (targetTime.isAfter(now)) {
            targetTime
        } else {
            targetTime.plusDays(1)
        }
        return Duration.between(now, nextRun)
    }
}
